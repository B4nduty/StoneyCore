package banduty.stoneycore.entity.custom;

import banduty.stoneycore.entity.custom.siegeentity.RotationAndMovementUtil;
import banduty.stoneycore.entity.custom.siegeentity.SiegeProperties;
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
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class AbstractSiegeEntity extends LivingEntity {

    protected static final EntityDataAccessor<Float> TRACKED_YAW =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> TRACKED_PITCH =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> COOLDOWN =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> LOAD_STAGE =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<String> AMMO_LOADED =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Integer> RELOAD_TIME =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> IS_PICKED =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> ATTACK_HAPPENED =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.BOOLEAN);

    public float lastRiderYaw;
    public float lastRiderPitch;
    public float wheelRotation;
    protected int moveTick;
    protected final Random random = new Random();
    protected final Set<UUID> playersNotified = new HashSet<>();
    protected Entity owner;

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
        this.entityData.define(LOAD_STAGE, 0);
        this.entityData.define(AMMO_LOADED, "");
        this.entityData.define(RELOAD_TIME, 0);
        this.entityData.define(IS_PICKED, false);
        this.entityData.define(ATTACK_HAPPENED, true);
    }

    public void setTrackedYaw(float yaw) {
        this.entityData.set(TRACKED_YAW, normalizeYaw(yaw));
    }

    public float getTrackedYaw() {
        return this.entityData.get(TRACKED_YAW);
    }

    public void setTrackedPitch(float pitch) {
        this.entityData.set(TRACKED_PITCH, Math.min(10, Math.max(pitch, -20)));
    }

    public float getTrackedPitch() {
        return this.entityData.get(TRACKED_PITCH);
    }

    public void setCooldown(int cooldown) {
        this.entityData.set(COOLDOWN, Math.max(0, cooldown));
    }

    public int getCooldown() {
        return this.entityData.get(COOLDOWN);
    }

    public void setLoadStage(int stage) {
        this.entityData.set(LOAD_STAGE, Math.max(0, stage));
    }

    public int getLoadStage() {
        return this.entityData.get(LOAD_STAGE);
    }

    public void setAmmoLoaded(String ammo) {
        this.entityData.set(AMMO_LOADED, ammo == null ? "" : ammo);
    }

    public String getAmmoLoaded() {
        return this.entityData.get(AMMO_LOADED);
    }

    public boolean hasAmmoLoaded() {
        return !getAmmoLoaded().isEmpty();
    }

    public void setReloadTime(int time) {
        this.entityData.set(RELOAD_TIME, Math.max(0, time));
    }

    public int getReloadTime() {
        return this.entityData.get(RELOAD_TIME);
    }

    public boolean isReloadComplete() {
        return getReloadTime() <= 0;
    }

    public void setPicked(boolean picked) {
        this.entityData.set(IS_PICKED, picked);
    }

    public boolean isPicked() {
        return this.entityData.get(IS_PICKED);
    }

    public void setAttackHappened(boolean happened) {
        this.entityData.set(ATTACK_HAPPENED, happened);
    }

    public boolean hasAttackHappened() {
        return this.entityData.get(ATTACK_HAPPENED);
    }

    public Entity getOwner() {
        return owner;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    public float getWheelRotation() {
        return wheelRotation;
    }

    public abstract SiegeProperties getProperties();

    public abstract Vec3 getPassengerOffset(Entity entity);

    public abstract Vec3 getPlayerPOV();

    public boolean canAddPassenger(Entity entity) {
        return getPassengers().isEmpty() && (entity instanceof Player || entity instanceof Horse);
    }

    public void onSiegeTick(ServerLevel serverLevel) {
    }

    public void onAttack(ServerLevel serverLevel) {
    }

    public void onReloadStart(ServerLevel serverLevel) {
    }

    public void onLoadComplete(ServerLevel serverLevel) {
    }

    public SoundEvent getMoveSound() {
        return getProperties().moveSound();
    }

    public SoundEvent getReloadSound() {
        return getProperties().reloadSound();
    }

    public SoundEvent getShootSound() {
        return getProperties().shootSound();
    }

    public SoundEvent getAttackSound() {
        return getProperties().attackSound();
    }

    public abstract void triggerAnimation(String animationName);

    public abstract void stopAnimation(String animationName);

    @Override
    public void tick() {
        super.tick();

        RotationAndMovementUtil.updateWheelRotation(this);

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        handleSiegeInitiation(serverLevel);
        RotationAndMovementUtil.updateSiegeVelocity(this);
        Services.ABSTRACT_SIEGE_ENTITY.updateSiegeNetworkData(serverLevel, this);

        updateEntityRotation();
        updateTimers();
        handleMovementSounds(serverLevel);

        onSiegeTick(serverLevel);
        setPicked(getFirstPassenger() != null);
    }

    private void handleSiegeInitiation(ServerLevel serverLevel) {
        if (getFirstPassenger() != null && SiegeManager.getPlayerSiege(serverLevel, getOwnerUUID()).isEmpty()) {
            Optional<Land> currentLand = LandState.get(serverLevel).getLandAt(this.getOnPos());
            if (currentLand.isPresent() && !currentLand.get().getOwnerUUID().equals(getOwnerUUID())) {
                SiegeManager.startSiege(serverLevel, getOwnerUUID(), currentLand.get().getOwnerUUID());
            }
        }
    }

    private void updateEntityRotation() {
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

    private void updateTimers() {
        setCooldown(getCooldown() - 1);
        setReloadTime(getReloadTime() - 1);
    }

    protected void handleMovementSounds(ServerLevel serverLevel) {
        boolean isMoving = this.getDeltaMovement().x != 0 || this.getDeltaMovement().z != 0;

        if (isMoving && isAlive()) {
            if (this.moveTick >= getProperties().moveSoundDelay() || this.moveTick == 0) {
                playSoundToNearbyPlayers(serverLevel, getMoveSound(), getProperties().moveSoundRange(), 1.0f);
                if (this.moveTick != 0) this.moveTick = 0;
            }
            moveTick++;
        } else if (this.moveTick != 0 || !isAlive()) {
            this.moveTick = 0;
            stopSoundForNearbyPlayers(serverLevel, getMoveSound());
        }
    }

    protected void playSoundToNearbyPlayers(ServerLevel serverLevel, SoundEvent sound, double maxDistance, float baseVolume) {
        serverLevel.players().forEach(p -> {
            double distance = p.position().distanceTo(this.position());
            if (distance <= maxDistance) {
                float volume = (float) (baseVolume * (1.0 - distance / maxDistance));
                if (volume > 0f) {
                    p.playNotifySound(sound, SoundSource.AMBIENT, volume,
                            random.nextFloat(0.75f, 1.25f));
                }
            }
        });
    }

    protected void stopSoundForNearbyPlayers(ServerLevel serverLevel, SoundEvent sound) {
        serverLevel.players().forEach(p -> {
            p.connection.send(new ClientboundStopSoundPacket(sound.getLocation(), SoundSource.AMBIENT));
        });
    }

    protected void playReloadSound(ServerLevel serverLevel) {
        playSoundToNearbyPlayers(serverLevel, getReloadSound(), getProperties().reloadSoundRange(), 1.0f);
    }

    protected void playShootSound(ServerLevel serverLevel) {
        playSoundToNearbyPlayers(serverLevel, getShootSound(), getProperties().shootSoundRange(), 1.0f);
    }

    protected void playAttackSound(ServerLevel serverLevel) {
        playSoundToNearbyPlayers(serverLevel, getAttackSound(), getProperties().attackSoundRange(), 1.0f);
    }

    private UUID getOwnerUUID() {
        if (getFirstPassenger() instanceof Player player) return player.getUUID();
        if (getFirstPassenger() instanceof TamableAnimal tameable &&
                tameable.getFirstPassenger() instanceof Player player) return player.getUUID();
        return null;
    }

    private float normalizeYaw(float yaw) {
        yaw %= 360.0f;
        if (yaw >= 180.0f) yaw -= 360.0f;
        if (yaw < -180.0f) yaw += 360.0f;
        return yaw;
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

        if (handleRepair(attacker, serverLevel)) return false;

        if (attacker instanceof Player player && player.isCreative()) {
            this.discard();
            return true;
        }

        if (isDamageAllowed(attacker, source, damageConfig)) {
            return super.hurt(source, amount);
        }

        return false;
    }

    private boolean handleRepair(Entity attacker, ServerLevel serverLevel) {
        if (!(attacker instanceof Player player)) return false;

        UUID playerId = player.getUUID();
        Optional<SiegeManager.Siege> siegeOpt = SiegeManager.getPlayerSiege(serverLevel, playerId);
        if (siegeOpt.isPresent() && siegeOpt.get().isDisabled(playerId)) return true;

        if (player.getMainHandItem().getItem() instanceof SmithingHammer &&
                getCooldown() == 0 && this.getHealth() < this.getMaxHealth()) {

            this.setHealth(player.isCreative() ? this.getMaxHealth() :
                    Math.min(this.getMaxHealth(), this.getHealth() + 1F));

            if (!player.isCreative()) {
                player.getMainHandItem().hurt(1, player.getRandom(), (ServerPlayer) player);
                if (player.getMainHandItem().getDamageValue() >= player.getMainHandItem().getMaxDamage()) {
                    player.getMainHandItem().setCount(0);
                }
            }

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    5, 0.3, 0.5, 0.3, 0.1);
            return true;
        }
        return false;
    }

    private boolean isDamageAllowed(Entity attacker, DamageSource source, DamageSourceConfig damageConfig) {
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType()).toString();
        if (damageConfig.canEntityDamage(entityId)) return true;

        if (attacker instanceof AbstractArrow && damageConfig.canDamageTypeDamage("projectile")) return true;

        if ((source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) &&
                damageConfig.canDamageTypeDamage("explosion")) return true;

        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack heldItem = livingAttacker.getMainHandItem();
            if (!heldItem.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
                return damageConfig.canItemDamage(itemId);
            }
        }

        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!(this.level() instanceof ServerLevel serverLevel) || hand != InteractionHand.MAIN_HAND) {
            return super.interact(player, hand);
        }

        if (!canInteract(player, serverLevel)) {
            return InteractionResult.FAIL;
        }

        InteractionResult horseResult = handleHorseInteraction(player, serverLevel);
        if (horseResult != null) return horseResult;

        return handleSiegeInteraction(player, hand, serverLevel);
    }

    private boolean canInteract(Player player, ServerLevel serverLevel) {
        UUID playerId = player.getUUID();

        Optional<SiegeManager.Siege> siegeOpt = SiegeManager.getPlayerSiege(serverLevel, playerId);
        if (siegeOpt.map(siege -> siege.isDisabled(playerId)).orElse(false)) {
            return false;
        }

        LandState stateManager = LandState.get(serverLevel);
        Optional<Land> maybeLand = stateManager.getLandAt(this.getOnPos());
        return maybeLand.map(land -> land.getOwnerUUID().equals(playerId) ||
                        land.isAlly(playerId) || player.isCreative())
                .orElse(true);
    }

    private InteractionResult handleHorseInteraction(Player player, ServerLevel serverLevel) {
        if (getFirstPassenger() instanceof Mob mob) {
            if (player.getMainHandItem().is(Items.SHEARS)) {
                mob.stopRiding();
                double[] offset = calculateHorseDismountOffset(mob);
                mob.setPos(this.getX() + offset[0], this.getY() + 0.5, this.getZ() + offset[1]);

                ItemEntity leadEntity = new ItemEntity(this.level(),
                        this.getX() + offset[0], this.getY() + 0.5, this.getZ() + offset[1],
                        new ItemStack(Items.LEAD));
                this.level().addFreshEntity(leadEntity);

                player.playNotifySound(SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            player.startRiding(mob);
            return InteractionResult.SUCCESS;
        }

        if (getPassengers().isEmpty()) {
            List<Entity> nearby = this.level().getEntities(player, this.getBoundingBox().inflate(4.0),
                    entity -> entity instanceof Mob);

            for (Entity entity : nearby) {
                if (canAddPassenger(entity) && entity instanceof Mob mob &&
                        mob.getLeashHolder() == player && canAttachHorse(mob)) {
                    mob.dropLeash(true, false);
                    mob.stopRiding();
                    mob.startRiding(this);
                    stopAnimation("set_up");
                    triggerAnimation("pick_up");
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return null;
    }

    private boolean canAttachHorse(Mob mob) {
        if (mob instanceof TamableAnimal tameable && !tameable.isTame()) return false;
        return !(mob instanceof Saddleable saddleable) || saddleable.isSaddled();
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

    protected abstract InteractionResult handleSiegeInteraction(Player player, InteractionHand hand, ServerLevel serverLevel);

    @Override
    public double getPassengersRidingOffset() {
        return 1.0D;
    }

    @Override
    public boolean dismountsUnderwater() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isEffectiveAi() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public ItemStack getPickResult() {
        Item siegeSpawnerItem = SiegeSpawnerItem.byId(this.getType());
        return siegeSpawnerItem == null ? ItemStack.EMPTY : new ItemStack(siegeSpawnerItem);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        float yawRadians = (float) Math.toRadians(getVisualRotationYInDegrees());
        Vec3 offset = getPassengerOffset(passenger);

        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);

        double offsetX = rightX * offset.x - forwardX * offset.z;
        double offsetZ = rightZ * offset.x - forwardZ * offset.z;

        return new Vec3(getX() + offsetX, getY() + offset.y, getZ() + offsetZ);
    }

    public double getVelocity(Entity entity) {
        SiegeEngineDefinitionData data = SiegeEngineDefinitionsStorage.getData(this.getType());
        return entity instanceof Horse ? data.horseSpeed() : data.playerSpeed();
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