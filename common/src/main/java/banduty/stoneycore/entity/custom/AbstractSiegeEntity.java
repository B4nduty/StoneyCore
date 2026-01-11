package banduty.stoneycore.entity.custom;

import banduty.stoneycore.entity.custom.siegeentity.RotationAndMovementUtil;
import banduty.stoneycore.items.SiegeSpawnerItem;
import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.util.definitionsloader.DamageSourceConfig;
import banduty.stoneycore.util.definitionsloader.SiegeEngineDefinitionData;
import banduty.stoneycore.util.definitionsloader.SiegeEngineDefinitionsStorage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class AbstractSiegeEntity extends LivingEntity {
    private Entity owner;

    // DataTracker fields
    protected static final EntityDataAccessor<Float> TRACKED_YAW =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> TRACKED_PITCH =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> COOLDOWN =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.INT);

    // State fields
    public float lastRiderYaw;
    public float lastRiderPitch;
    public float wheelRotation;
    protected final Set<UUID> playersNotified = new HashSet<>();

    public AbstractSiegeEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        setNoGravity(false);
        setMaxUpStep(1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TRACKED_YAW, 0.0f);
        this.entityData.define(TRACKED_PITCH, 0.0f);
        this.entityData.define(COOLDOWN, 0);
    }

    // Tracked yaw
    public void setTrackedYaw(float yaw) {
        this.entityData.set(TRACKED_YAW, normalizeYaw(yaw));
    }

    public float getTrackedYaw() {
        return this.entityData.get(TRACKED_YAW);
    }

    // Tracked pitch
    public void setTrackedPitch(float pitch) {
        this.entityData.set(TRACKED_PITCH, Math.min(10, Math.max(pitch, -20)));
    }

    public float getTrackedPitch() {
        return this.entityData.get(TRACKED_PITCH);
    }

    // Cooldown
    public void setCooldown(int cooldown) {
        this.entityData.set(COOLDOWN, Math.max(0, cooldown));
    }

    public int getCooldown() {
        return this.entityData.get(COOLDOWN);
    }

    public float getWheelRotation() {
        return wheelRotation;
    }

    public Entity getOwner() {
        return owner;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    @Override
    public void tick() {
        super.tick();
        RotationAndMovementUtil.updateWheelRotation(this);

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Entity entity = this.getFirstPassenger();
        if (entity instanceof Saddleable saddleable && !saddleable.isSaddled()) {
            entity.removeVehicle();
        }

        if (this.getFirstPassenger() != null && SiegeManager.getPlayerSiege(serverLevel, this.getOwnerUUID()).isEmpty()) {
            Optional<Land> currentLand = LandState.get(serverLevel).getLandAt(this.getOnPos());
            if (currentLand.isPresent() && !currentLand.get().getOwnerUUID().equals(this.getOwnerUUID())) {
                SiegeManager.startSiege(serverLevel, this.getOwnerUUID(), currentLand.get().getOwnerUUID());
            }
        }

        RotationAndMovementUtil.updateSiegeVelocity(this);

        Services.ABSTRACT_SIEGE_ENTITY.updateSiegeNetworkData(serverLevel, this);

        this.setYHeadRot(this.getTrackedYaw());

        this.setTrackedYaw(this.getYRot());
        this.setYRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
        this.setYBodyRot(this.getYRot());
        this.lastRiderYaw = this.getYRot();

        this.setTrackedPitch(this.getXRot());
        this.setXRot(this.getXRot());
        this.lastRiderPitch = this.getXRot();

        this.wheelRotation = this.getWheelRotation();

        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private UUID getOwnerUUID() {
        if (this.getFirstPassenger() instanceof Player playerEntity) return playerEntity.getUUID();
        if (this.getFirstPassenger() instanceof TamableAnimal tameableEntity &&
                tameableEntity.getFirstPassenger() instanceof Player playerEntity) return playerEntity.getUUID();
        return null;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger.level() instanceof ServerLevel) {
            float yaw = getTrackedYaw();
            this.setYRot(yaw);
            this.setYHeadRot(yaw);
            this.setYBodyRot(yaw);
            this.lastRiderYaw = yaw;

            float pitch = getTrackedPitch();
            this.setXRot(pitch);
            this.lastRiderPitch = pitch;
        }
        super.removePassenger(passenger);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction moveFunction) {
        RotationAndMovementUtil.updatePassengerPosition(this, entity, moveFunction);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        SiegeEngineDefinitionData siegeData = SiegeEngineDefinitionsStorage.getData(this.getType());
        DamageSourceConfig damageConfig = siegeData.damageConfig();

        if (attacker == null || !(attacker.level() instanceof ServerLevel serverLevel)) {
            return super.hurt(source, amount);
        }

        // Smithing hammer repair
        if (attacker instanceof Player player) {
            UUID playerId = player.getUUID();
            Optional<SiegeManager.Siege> siegeOpt = SiegeManager.getPlayerSiege(serverLevel, playerId);
            if (siegeOpt.isPresent() && siegeOpt.get().isDisabled(playerId)) return false;

            if (player.getMainHandItem().getItem() instanceof SmithingHammer &&
                    getCooldown() == 0 &&
                    this.getHealth() < this.getMaxHealth()) {
                this.setHealth(player.isCreative() ? this.getMaxHealth() : Math.min(this.getMaxHealth(), this.getHealth() + 1F));
                if (!player.isCreative()) {
                    player.getMainHandItem().hurt(1, player.getRandom(), (ServerPlayer) player);
                    if (player.getMainHandItem().getDamageValue() >= player.getMainHandItem().getMaxDamage()) {
                        player.getMainHandItem().setCount(0);
                    }
                }
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        5,
                        0.3, 0.5, 0.3,
                        0.1
                );
                return false;
            }
        }

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType()).toString();

        // Check if entity can damage
        boolean canEntityDamage = damageConfig.canEntityDamage(entityId);

        if (canEntityDamage) return super.hurt(source, amount);

        // Check damage type
        boolean canDamageType = false;
        if (attacker instanceof AbstractArrow) {
            canDamageType = damageConfig.canDamageTypeDamage("projectile");
        } else if (source.is(DamageTypes.EXPLOSION) ||
                source.is(DamageTypes.PLAYER_EXPLOSION)) {
            canDamageType = damageConfig.canDamageTypeDamage("explosion");
        }

        if (canDamageType) return super.hurt(source, amount);

        // Check held item if attacker is LivingEntity
        boolean canItemDamage = false;
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack heldItem = livingAttacker.getMainHandItem();
            if (!heldItem.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
                canItemDamage = damageConfig.canItemDamage(itemId);
            }
        }

        if (canItemDamage) return super.hurt(source, amount);

        return false;
    }

    // Equipment handling
    @Override public Iterable<ItemStack> getArmorSlots() { return List.of(); }
    @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}
    @Override public HumanoidArm getMainArm() {return HumanoidArm.RIGHT;}

    // Mounting logic
    @Override public double getPassengersRidingOffset() { return 1.0D; }
    @Override public boolean dismountsUnderwater() { return true; }
    @Override public abstract boolean canAddPassenger(Entity passenger);

    // Movement logic
    @Override public boolean isPushable() { return false; }
    @Override public boolean isEffectiveAi() { return false; }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        float yawRadians = (float) Math.toRadians(getVisualRotationYInDegrees());
        Vec3 offset = getPassengerOffset(passenger);
        double leftOffset = offset.x;
        double backOffset = offset.z;

        double forwardX = -Math.sin(yawRadians);
        double forwardZ =  Math.cos(yawRadians);

        double rightX =  Math.cos(yawRadians);
        double rightZ =  Math.sin(yawRadians);

        double offsetX = rightX * leftOffset - forwardX * backOffset;
        double offsetZ = rightZ * leftOffset - forwardZ * backOffset;

        double x = getX() + offsetX;
        double y = getY() + offset.y;
        double z = getZ() + offsetZ;

        return new Vec3(x, y, z);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!(this.level() instanceof ServerLevel serverLevel) || hand != InteractionHand.MAIN_HAND || this.wasEyeInWater) return super.interact(player, hand);

        UUID playerId = player.getUUID();
        Optional<SiegeManager.Siege> siegeOpt = SiegeManager.getPlayerSiege(serverLevel, playerId);
        if (siegeOpt.isPresent() && siegeOpt.get().isDisabled(playerId)) return InteractionResult.FAIL;

        LandState stateManager = LandState.get(serverLevel);
        Optional<Land> maybeLand = stateManager.getLandAt(this.getOnPos());
        if (maybeLand.isPresent() && !(maybeLand.get().getOwnerUUID().equals(player.getUUID()) || maybeLand.get().isAlly(player.getUUID()) || player.isCreative())) {
            return InteractionResult.FAIL;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // Dismount horse
        if (this.getFirstPassenger() instanceof Mob mob) {
            if (heldItem.is(Items.SHEARS)) {
                mob.stopRiding();

                double[] offset = calculateHorseDismountOffset(mob);
                double dX = this.getX() + offset[0];
                double dY = this.getY() + 0.5;
                double dZ = this.getZ() + offset[1];
                mob.setPos(dX, dY, dZ);

                ItemEntity leadEntity = new ItemEntity(this.level(), dX, dY, dZ, new ItemStack(Items.LEAD));
                this.level().addFreshEntity(leadEntity);

                player.playNotifySound(SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            player.startRiding(mob);
            return InteractionResult.SUCCESS;
        }

        // Attach horse to siege
        if (this.getPassengers().isEmpty()) {
            List<Entity> nearby = this.level().getEntities(player, this.getBoundingBox().inflate(4.0),
                    entity -> entity instanceof Mob);

            for (Entity entity : nearby) {
                if (canAddPassenger(entity) && entity instanceof Mob mob && mob.getLeashHolder() == player &&
                        !(mob instanceof TamableAnimal tameableEntity && !tameableEntity.isTame()) &&
                        (!(entity instanceof Saddleable saddleable) || saddleable.isSaddled())) {
                    mob.dropLeash(true, false);
                    mob.stopRiding();
                    mob.startRiding(this);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (this.getFirstPassenger() != null) return InteractionResult.SUCCESS;

        if (canAddPassenger(player) && heldItem.isEmpty() && getCooldown() == 0 && !player.isShiftKeyDown()) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }

        if (getCooldown() > 0) {
            player.displayClientMessage(Component.translatable("component.siege_engine." + BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getNamespace()
                    + ".cooling_down", this.getName().getString()), true);
        }

        return InteractionResult.SUCCESS;
    }

    protected double[] calculateHorseDismountOffset(Entity passenger) {
        float yawRadians = (float) Math.toRadians(this.getVisualRotationYInDegrees());
        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);

        Vec3 offset = this.getPassengerOffset(passenger);
        double leftOffset = offset.x;
        double backOffset = offset.z;

        double offsetX = rightX * leftOffset - forwardX * backOffset;
        double offsetZ = rightZ * leftOffset - forwardZ * backOffset;

        return new double[]{offsetX, offsetZ};
    }

    private float normalizeYaw(float yaw) {
        yaw %= 360.0f;
        if (yaw >= 180.0f) yaw -= 360.0f;
        if (yaw < -180.0f) yaw += 360.0f;
        return yaw;
    }

    public abstract Vec3 getPassengerOffset(Entity entity);

    public double getVelocity(Entity entity) {
        if (entity instanceof Horse) {
            return SiegeEngineDefinitionsStorage.getData(this.getType()).horseSpeed();
        }
        return SiegeEngineDefinitionsStorage.getData(this.getType()).playerSpeed();
    }

    public abstract Vec3 getPlayerPOV();

    @Override
    public ItemStack getPickResult() {
        SiegeSpawnerItem siegeSpawnerItem = SiegeSpawnerItem.forEntity(this.getType());
        return siegeSpawnerItem == null ? null : new ItemStack(siegeSpawnerItem);
    }

    public double getKnockback() {
        return SiegeEngineDefinitionsStorage.getData(this.getType()).knockback();
    }

    public double getBaseDamage() {
        return SiegeEngineDefinitionsStorage.getData(this.getType()).baseDamage();
    }

    public int getBaseReload() {
        return SiegeEngineDefinitionsStorage.getData(this.getType()).baseReload();
    }

    public float getProjectileSpeed() {
        return SiegeEngineDefinitionsStorage.getData(this.getType()).projectileSpeed();
    }

    public float getAccuracyMultiplier() {
        return SiegeEngineDefinitionsStorage.getData(this.getType()).accuracyMultiplier();
    }
}
