package banduty.stoneycore.entity.custom;

import banduty.stoneycore.entity.custom.siegeentity.RotationAndMovementUtil;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.item.SiegeSpawnerItem;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSiegeEntity extends LivingEntity {
    public final Map<BlockPos, Float> blockDamageMap = new HashMap<>();
    private Entity owner;

    // DataTracker fields
    protected static final TrackedData<Float> TRACKED_YAW =
            DataTracker.registerData(AbstractSiegeEntity.class, TrackedDataHandlerRegistry.FLOAT);
    protected static final TrackedData<Float> TRACKED_PITCH =
            DataTracker.registerData(AbstractSiegeEntity.class, TrackedDataHandlerRegistry.FLOAT);
    protected static final TrackedData<Integer> COOLDOWN =
            DataTracker.registerData(AbstractSiegeEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // State fields
    public float lastRiderYaw;
    public float lastRiderPitch;
    public float wheelRotation;
    protected final Set<UUID> playersNotified = new HashSet<>();

    public AbstractSiegeEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        setNoGravity(false);
        setStepHeight(1.0F);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TRACKED_YAW, 0.0f);
        this.dataTracker.startTracking(TRACKED_PITCH, 0.0f);
        this.dataTracker.startTracking(COOLDOWN, 0);
    }

    // Tracked yaw
    public void setTrackedYaw(float yaw) {
        this.dataTracker.set(TRACKED_YAW, normalizeYaw(yaw));
    }

    public float getTrackedYaw() {
        return this.dataTracker.get(TRACKED_YAW);
    }

    // Tracked pitch
    public void setTrackedPitch(float pitch) {
        this.dataTracker.set(TRACKED_PITCH, Math.min(10, Math.max(pitch, -20)));
    }

    public float getTrackedPitch() {
        return this.dataTracker.get(TRACKED_PITCH);
    }

    // Cooldown
    public void setCooldown(int cooldown) {
        this.dataTracker.set(COOLDOWN, Math.max(0, cooldown));
    }

    public int getCooldown() {
        return this.dataTracker.get(COOLDOWN);
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

        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        Entity entity = this.getFirstPassenger();
        if (entity instanceof Saddleable saddleable && !saddleable.isSaddled()) {
            entity.dismountVehicle();
        }

        if (this.getFirstPassenger() != null && SiegeManager.getPlayerSiege(serverWorld, this.getOwnerUUID()).isEmpty()) {
            Optional<Land> currentLand = LandState.get(serverWorld).getLandAt(this.getBlockPos());
            if (currentLand.isPresent() && !currentLand.get().getOwnerUUID().equals(this.getOwnerUUID())) {
                SiegeManager.startSiege(serverWorld, this.getOwnerUUID(), currentLand.get().getOwnerUUID());
            }
        }

        RotationAndMovementUtil.updateSiegeVelocity(this);

        this.setHeadYaw(this.getTrackedYaw());

        List<ServerPlayerEntity> players = serverWorld.getPlayers();
        for (ServerPlayerEntity player : players) {
            UUID playerId = player.getUuid();
            if (!playersNotified.contains(playerId)) {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeFloat(this.getYaw());
                buffer.writeFloat(this.getPitch());
                buffer.writeFloat(this.getWheelRotation());
                ServerPlayNetworking.send(player, ModMessages.SIEGE_YAW_PITCH_S2C_ID, buffer);
                playersNotified.add(playerId);
            }
        }

        Set<UUID> onlinePlayerIds = players.stream()
                .map(ServerPlayerEntity::getUuid)
                .collect(Collectors.toSet());
        playersNotified.removeIf(uuid -> !onlinePlayerIds.contains(uuid));

        this.setTrackedYaw(this.getYaw());
        this.setYaw(this.getYaw());
        this.setHeadYaw(this.getYaw());
        this.setBodyYaw(this.getYaw());
        this.lastRiderYaw = this.getYaw();

        this.setTrackedPitch(this.getPitch());
        this.setPitch(this.getPitch());
        this.lastRiderPitch = this.getPitch();

        this.wheelRotation = this.getWheelRotation();

        this.move(MovementType.SELF, this.getVelocity());
    }

    private UUID getOwnerUUID() {
        if (this.getFirstPassenger() instanceof PlayerEntity playerEntity) return playerEntity.getUuid();
        if (this.getFirstPassenger() instanceof TameableEntity tameableEntity &&
                tameableEntity.getFirstPassenger() instanceof PlayerEntity playerEntity) return playerEntity.getUuid();
        return null;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger.getWorld() instanceof ServerWorld) {
            float yaw = getTrackedYaw();
            this.setYaw(yaw);
            this.setHeadYaw(yaw);
            this.setBodyYaw(yaw);
            this.lastRiderYaw = yaw;

            float pitch = getTrackedPitch();
            this.setPitch(pitch);
            this.lastRiderPitch = pitch;
        }
        super.removePassenger(passenger);
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        RotationAndMovementUtil.updatePassengerPosition(this, passenger, positionUpdater);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity player) {
            if (player.getWorld() instanceof ServerWorld serverWorld) {
                UUID playerId = player.getUuid();
                Optional<SiegeManager.Siege> siegeOpt = SiegeManager.getPlayerSiege(serverWorld, playerId);
                if (siegeOpt.isPresent() && siegeOpt.get().isDisabled(playerId)) return false;
            }

            if (player.getMainHandStack().getItem() instanceof AxeItem) {
                return super.damage(source, amount);
            }

            if (player.getMainHandStack().isOf(SCItems.SMITHING_HAMMER.get()) && getCooldown() == 0 &&
                    this.getHealth() < this.getMaxHealth() && player.getWorld() instanceof ServerWorld serverWorld) {
                this.setHealth(player.isCreative() ? this.getMaxHealth() : Math.min(this.getMaxHealth(), this.getHealth() + 1F));
                if (!player.isCreative()) {
                    player.getMainHandStack().damage(1, player.getRandom(), (ServerPlayerEntity) player);
                    if (player.getMainHandStack().getDamage() >= player.getMainHandStack().getMaxDamage()) {
                        player.getMainHandStack().setCount(0);
                    }
                }
                serverWorld.spawnParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        5,
                        0.3, 0.5, 0.3,
                        0.1
                );
                return false;
            }
        }

        if (attacker instanceof AbstractSiegeEntity) {
            return super.damage(source, amount);
        }

        return false;
    }

    // Equipment handling
    @Override public Iterable<ItemStack> getArmorItems() { return List.of(); }
    @Override public ItemStack getEquippedStack(EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override public void equipStack(EquipmentSlot slot, ItemStack stack) {}
    @Override public Arm getMainArm() {return null;
    }

    // Mounting logic
    @Override public double getMountedHeightOffset() { return 1.0D; }
    @Override public boolean shouldDismountUnderwater() { return true; }
    @Override public abstract boolean canAddPassenger(Entity passenger);

    // Movement logic
    @Override public boolean isPushable() { return false; }
    @Override public boolean canMoveVoluntarily() { return false; }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld) || hand != Hand.MAIN_HAND || this.submergedInWater) return super.interact(player, hand);

        UUID playerId = player.getUuid();
        Optional<SiegeManager.Siege> siegeOpt = SiegeManager.getPlayerSiege(serverWorld, playerId);
        if (siegeOpt.isPresent() && siegeOpt.get().isDisabled(playerId)) return ActionResult.FAIL;

        LandState stateManager = LandState.get(serverWorld);
        Optional<Land> maybeLand = stateManager.getLandAt(this.getBlockPos());
        if (maybeLand.isPresent() && !(maybeLand.get().getOwnerUUID().equals(player.getUuid()) || maybeLand.get().isAlly(player.getUuid()) || player.isCreative())) {
            return ActionResult.FAIL;
        }

        ItemStack heldItem = player.getStackInHand(hand);

        // Dismount horse
        if (this.getFirstPassenger() instanceof MobEntity mobEntity) {
            if (heldItem.isOf(Items.SHEARS)) {
                mobEntity.stopRiding();

                double[] offset = calculateHorseDismountOffset(mobEntity);
                double dX = this.getX() + offset[0];
                double dY = this.getY() + 0.5;
                double dZ = this.getZ() + offset[1];
                mobEntity.setPos(dX, dY, dZ);

                ItemEntity leadEntity = new ItemEntity(this.getWorld(), dX, dY, dZ, new ItemStack(Items.LEAD));
                this.getWorld().spawnEntity(leadEntity);

                player.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }
            player.startRiding(mobEntity);
            return ActionResult.SUCCESS;
        }

        // Attach horse to siege
        if (this.getPassengerList().isEmpty()) {
            List<Entity> nearby = this.getWorld().getOtherEntities(player, this.getBoundingBox().expand(4.0),
                    entity -> entity instanceof MobEntity);

            for (Entity entity : nearby) {
                if (canAddPassenger(entity) && entity instanceof MobEntity mobEntity && mobEntity.getHoldingEntity() == player &&
                        !(mobEntity instanceof TameableEntity tameableEntity && !tameableEntity.isTamed()) &&
                        (!(entity instanceof Saddleable saddleable) || saddleable.isSaddled())) {
                    mobEntity.detachLeash(true, false);
                    mobEntity.stopRiding();
                    mobEntity.startRiding(this);
                    return ActionResult.SUCCESS;
                }
            }
        }

        if (this.getFirstPassenger() != null) return ActionResult.SUCCESS;

        if (canAddPassenger(player) && heldItem.isEmpty() && getCooldown() == 0 && !player.isSneaking()) {
            player.startRiding(this);
            return ActionResult.SUCCESS;
        }

        if (getCooldown() > 0) {
            player.sendMessage(Text.translatable("text.siege_machine." + Registries.ENTITY_TYPE.getId(this.getType()).getNamespace()
                    + ".cooling_down", this.getName().getString()), true);
        }

        return ActionResult.SUCCESS;
    }

    protected double[] calculateHorseDismountOffset(Entity passenger) {
        float yawRadians = (float) Math.toRadians(this.getBodyYaw());
        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);

        Vec3d offset = this.getPassengerOffset(passenger);
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

    public abstract Vec3d getPassengerOffset(Entity entity);

    public abstract double getVelocity(Entity entity);

    public abstract Vec3d getPlayerPOV();

    @Override
    public @Nullable ItemStack getPickBlockStack() {
        SiegeSpawnerItem siegeSpawnerItem = SiegeSpawnerItem.forEntity(this.getType());
        return siegeSpawnerItem == null ? null : new ItemStack(siegeSpawnerItem);
    }
}
