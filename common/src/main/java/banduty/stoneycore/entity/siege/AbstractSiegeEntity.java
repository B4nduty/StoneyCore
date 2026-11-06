package banduty.stoneycore.entity.siege;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.definitions.SiegeEngineDefinitionData;
import banduty.stoneycore.definitions.SiegeEngineDefinitionsStorage;
import banduty.stoneycore.entity.projectile.AbstractSiegeProjectile;
import banduty.stoneycore.items.custom.SiegeSpawnerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Base of every siege engine. It owns: synced state, owner, mounting, movement/facing, sounds, damage rules.
 * Subclasses only describe WHAT the engine is ({@link #getProperties()}, seats) and WHAT it does
 * ({@link #operate}); loading is provided by {@link StagedLoadingSiegeEntity} / {@link AmmoLoadingSiegeEntity}.
 */
public abstract class AbstractSiegeEntity extends LivingEntity {

    public static final float MIN_PITCH = -20f;
    public static final float MAX_PITCH = 10f;

    protected static final EntityDataAccessor<Float> TRACKED_YAW =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> TRACKED_PITCH =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> COOLDOWN =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> IS_PICKED =
            SynchedEntityData.defineId(AbstractSiegeEntity.class, EntityDataSerializers.BOOLEAN);

    public float lastRiderYaw;
    public float lastRiderPitch;
    public float wheelRotation;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private SiegeSounds sounds;

    protected AbstractSiegeEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    // ------------------------------------------------------------------ definition

    public abstract SiegeProperties getProperties();

    /** Seat position in local space (x = right, y = up, z = backward). */
    public abstract Vec3 getPassengerOffset(Entity entity);

    public abstract Vec3 getPlayerPOV();

    /** What a player does with this engine (load, fire, rotate...). Mounting is handled before this is called. */
    protected abstract InteractionResult operate(Player player, ItemStack stack, ServerLevel level);

    /** Called every server tick after movement, timers and sounds. */
    protected void onSiegeTick(ServerLevel level) {}

    /** True while the engine's "reload" sound loop should be playing. */
    protected boolean isReloading() {
        return false;
    }

    protected SCDamageType getDamageType() {
        return SCDamageType.BLUDGEONING;
    }

    // ------------------------------------------------------------------ datapack stats

    protected SiegeEngineDefinitionData stats() {
        return SiegeEngineDefinitionsStorage.getData(this.getType());
    }

    public double getVelocity(Entity passenger) {
        SiegeEngineDefinitionData data = stats();
        return passenger instanceof Horse ? data.horseSpeed() : data.playerSpeed();
    }

    public double getKnockback() { return stats().knockback(); }
    public double getBaseDamage() { return stats().baseDamage(); }
    public int getBaseReload() { return stats().baseReload(); }
    public float getProjectileSpeed() { return stats().projectileSpeed(); }
    public float getAccuracyMultiplier() { return stats().accuracyMultiplier(); }

    // ------------------------------------------------------------------ synced state

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TRACKED_YAW, 0.0f);
        builder.define(TRACKED_PITCH, 0.0f);
        builder.define(COOLDOWN, 0);
        builder.define(IS_PICKED, false);
    }

    public void setTrackedYaw(float yaw) { this.entityData.set(TRACKED_YAW, Mth.wrapDegrees(yaw)); }
    public float getTrackedYaw() { return this.entityData.get(TRACKED_YAW); }

    public void setTrackedPitch(float pitch) { this.entityData.set(TRACKED_PITCH, Mth.clamp(pitch, MIN_PITCH, MAX_PITCH)); }
    public float getTrackedPitch() { return this.entityData.get(TRACKED_PITCH); }

    public void setCooldown(int ticks) { this.entityData.set(COOLDOWN, Math.max(0, ticks)); }
    public int getCooldown() { return this.entityData.get(COOLDOWN); }

    /** Starts the engine's standard post-fire lock. */
    protected void startCooldown() { setCooldown(getProperties().cooldownTicks()); }

    public void setPicked(boolean picked) { this.entityData.set(IS_PICKED, picked); }
    public boolean isPicked() { return this.entityData.get(IS_PICKED); }

    public float getWheelRotation() { return wheelRotation; }

    // ------------------------------------------------------------------ owner (persistent, UUID based)

    public void setOwner(@Nullable Entity owner) {
        this.ownerUUID = owner == null ? null : owner.getUUID();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /** The player/mob currently responsible for this engine, or null if unknown or not loaded. */
    @Nullable
    public LivingEntity getOwner() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getEntity(ownerUUID) instanceof LivingEntity living ? living : null;
    }

    public Entity getOwnerOrSelf() {
        LivingEntity owner = getOwner();
        return owner != null ? owner : this;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("SiegeOwner", ownerUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUUID = tag.hasUUID("SiegeOwner") ? tag.getUUID("SiegeOwner") : null;
    }

    // ------------------------------------------------------------------ facing

    /** Single authority for horizontal facing: entity rotation and the synced copy always agree. */
    public void setFacing(float yaw) {
        setYRot(yaw);
        setYHeadRot(yaw);
        setYBodyRot(yaw);
        lastRiderYaw = yaw;
        entityData.set(TRACKED_YAW, Mth.wrapDegrees(yaw));
    }

    public void setAimPitch(float pitch) {
        float clamped = Mth.clamp(pitch, MIN_PITCH, MAX_PITCH);
        setXRot(clamped);
        lastRiderPitch = clamped;
        entityData.set(TRACKED_PITCH, clamped);
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();

        // LivingEntity#tick lets the body rotation drift towards the movement direction; undo that.
        setFacing(getYRot());
        setAimPitch(getXRot());
        SiegeMovement.updateWheelRotation(this);

        if (!(level() instanceof ServerLevel serverLevel)) return;

        SiegeMovement.tickMovement(this);

        if (getCooldown() > 0) setCooldown(getCooldown() - 1);
        tickTimers();

        Vec3 velocity = getDeltaMovement();
        sounds().tick(serverLevel, velocity.x != 0 || velocity.z != 0, isReloading());

        Player rider = SiegeMovement.getRider(this);
        if (rider != null && tickCount % 20 == 0) {
            SiegeTrigger.tryStart(serverLevel, rider.getUUID(), getOnPos());
        }

        setPicked(!getPassengers().isEmpty());
        onSiegeTick(serverLevel);
    }

    /** Extra per-tick timers (reload countdown etc). */
    protected void tickTimers() {}

    @Override
    public void remove(RemovalReason reason) {
        if (level() instanceof ServerLevel serverLevel) sounds().stopAll(serverLevel);
        super.remove(reason);
    }

    // ------------------------------------------------------------------ sounds / animations

    protected SiegeSounds sounds() {
        if (sounds == null) sounds = new SiegeSounds(this);
        return sounds;
    }

    protected void playShootSound(ServerLevel level) { sounds().playShoot(level); }

    protected void playAttackSound(ServerLevel level) { sounds().playAttack(level); }

    public void playAnimation(SiegeAnimation animation) {
        if (this instanceof SiegeAnimatable animatable) animatable.triggerSiegeAnimation(animation);
    }

    // ------------------------------------------------------------------ aiming / projectiles

    /** Random aim error in degrees, scaled by the datapack accuracy. */
    protected float spread() {
        return (random.nextFloat() - 0.5f) * 4 * getAccuracyMultiplier();
    }

    protected Vec3 aimDirection(float yawOffset) {
        return SiegeMovement.direction(getYRot() + yawOffset + spread(), getXRot() + spread());
    }

    protected Vec3 flatAimDirection() {
        return SiegeMovement.direction(getYRot() + spread(), 0);
    }

    protected Vec3 muzzle(float yawDegrees, double forward, double height) {
        double yaw = Math.toRadians(yawDegrees);
        return new Vec3(
                getX() - Math.sin(yaw) * forward,
                getY() + height - Math.sin(Math.toRadians(getXRot())),
                getZ() + Math.cos(yaw) * forward);
    }

    /** Applies the standard engine stats to a projectile. The caller adds it to the level. */
    protected void configure(AbstractSiegeProjectile projectile, Vec3 pos, Vec3 direction) {
        projectile.setPos(pos);
        projectile.setDeltaMovement(direction.scale(getProjectileSpeed() / 20.0));
        projectile.setBaseDamage(getBaseDamage());
        projectile.setDamageType(getDamageType());
    }

    // ------------------------------------------------------------------ damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker == null || !(attacker.level() instanceof ServerLevel serverLevel)) {
            return super.hurt(source, amount);
        }

        if (attacker instanceof Player player) {
            if (SiegeDamageRules.isDisabled(serverLevel, player)) return false;
            if (SiegeDamageRules.tryRepair(this, player, serverLevel)) return false;
            if (player.isCreative()) {
                this.discard();
                return true;
            }
        }

        if (SiegeDamageRules.isAllowed(attacker, source, stats().damageConfig())) {
            return super.hurt(source, amount);
        }
        return false;
    }

    // ------------------------------------------------------------------ interaction

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!(level() instanceof ServerLevel serverLevel) || hand != InteractionHand.MAIN_HAND) {
            return super.interact(player, hand);
        }

        if (!SiegeDamageRules.canOperate(serverLevel, player, getOnPos())) return InteractionResult.FAIL;

        InteractionResult horseResult = handleHorseInteraction(player);
        if (horseResult != null) return horseResult;

        if (!getPassengers().isEmpty()) return InteractionResult.FAIL;

        ItemStack stack = player.getItemInHand(hand);
        if (shouldMount(player, stack)) {
            player.startRiding(this);
            setOwner(player);
            return InteractionResult.SUCCESS;
        }

        return operate(player, stack, serverLevel);
    }

    protected boolean shouldMount(Player player, ItemStack heldItem) {
        return heldItem.isEmpty() && !player.isShiftKeyDown() && canAddPassenger(player);
    }

    @Nullable
    private InteractionResult handleHorseInteraction(Player player) {
        if (getFirstPassenger() instanceof Mob mob) {
            if (player.getMainHandItem().is(Items.SHEARS)) {
                mob.stopRiding();
                Vec3 pos = position().add(SiegeMovement.localToWorld(getYRot(), getPassengerOffset(mob)));
                mob.setPos(pos.x, getY() + 0.5, pos.z);
                level().addFreshEntity(new ItemEntity(level(), pos.x, getY() + 0.5, pos.z, new ItemStack(Items.LEAD)));
                player.playNotifySound(SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            player.startRiding(mob);
            return InteractionResult.SUCCESS;
        }

        if (getPassengers().isEmpty()) {
            List<Entity> nearby = level().getEntities(player, getBoundingBox().inflate(4.0), e -> e instanceof Mob);
            for (Entity entity : nearby) {
                if (entity instanceof Mob mob && canAddPassenger(mob)
                        && mob.getLeashHolder() == player && canAttachHorse(mob)) {
                    mob.dropLeash(true, false);
                    mob.stopRiding();
                    mob.startRiding(this);
                    setOwner(player);
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

    // ------------------------------------------------------------------ passengers

    public boolean canAddPassenger(Entity entity) {
        return getPassengers().isEmpty() && (entity instanceof Player || entity instanceof Horse);
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction moveFunction) {
        SiegeMovement.positionPassenger(this, entity, moveFunction);
    }

    @Override
    public Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, 1.0, 0.0);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 offset = SiegeMovement.localToWorld(getYRot(), getPassengerOffset(passenger));
        return position().add(offset);
    }

    @Override
    public boolean dismountsUnderwater() { return true; }

    // ------------------------------------------------------------------ boilerplate LivingEntity overrides

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean isEffectiveAi() { return false; }

    @Override
    public Iterable<ItemStack> getArmorSlots() { return List.of(); }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }

    @Override
    public ItemStack getPickResult() {
        Item spawner = SiegeSpawnerItem.byId(this.getType());
        return spawner == null ? ItemStack.EMPTY : new ItemStack(spawner);
    }
}
