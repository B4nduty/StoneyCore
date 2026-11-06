package banduty.stoneycore.entity.projectile;

import banduty.stoneycore.combat.damagetype.SCDamageApplier;
import banduty.stoneycore.combat.damagetype.SCDamageCalculator;
import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.entity.siege.AbstractSiegeEntity;
import banduty.stoneycore.entity.siege.SiegeTrigger;
import banduty.stoneycore.sounds.SCSounds;
import banduty.stoneycore.util.BlockDamageTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Base of every projectile fired by a siege engine.
 * Entity hits are fully handled here (damage is applied once per entity, then the ball ricochets);
 * block hits go through {@link #onSiegeImpact}, which is the only thing subclasses implement.
 */
public abstract class AbstractSiegeProjectile extends AbstractArrow {

    private SCDamageType damageType = SCDamageType.BLUDGEONING;
    private final Set<Integer> damagedEntities = new HashSet<>();

    protected AbstractSiegeProjectile(EntityType<? extends AbstractSiegeProjectile> type, @Nullable LivingEntity shooter, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
        if (shooter != null) this.setOwner(shooter);
    }

    public void setDamageType(SCDamageType damageType) {
        this.damageType = damageType;
    }

    /** What happens when this projectile hits a block fired by a siege engine (craters, fire, clouds...). */
    protected abstract void onSiegeImpact(ServerLevel level, BlockHitResult hit);

    // ---- who is responsible

    /** The player/mob operating the engine that fired this, else the engine, else the projectile itself. */
    protected Entity getResponsibleEntity() {
        Entity owner = getOwner();
        if (owner instanceof AbstractSiegeEntity siege) return siege.getOwnerOrSelf();
        return owner != null ? owner : this;
    }

    @Nullable
    private UUID getOperatorUUID() {
        return getResponsibleEntity() instanceof Player player ? player.getUUID() : null;
    }

    // ---- ticking

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) return;

        BlockDamageTracker.clean(serverLevel);
        if (tickCount % 10 == 0) SiegeTrigger.tryStart(serverLevel, getOperatorUUID(), getOnPos());
    }

    // ---- hits

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!(level() instanceof ServerLevel)) return;

        Entity entity = hit.getEntity();
        if (!damagedEntities.add(entity.getId())) return;

        if (entity instanceof LivingEntity target) {
            Entity responsible = getResponsibleEntity();
            DamageSource source = damageSources().arrow(this, responsible);
            double damage = SCDamageCalculator.applyArmor(target, (float) getBaseDamage(), damageType, source);
            SCDamageApplier.apply(target, responsible, ItemStack.EMPTY, damage);
        }

        setDeltaMovement(getDeltaMovement().scale(-0.9));
        setBaseDamage(getBaseDamage() * 0.9);
        if (getBaseDamage() <= 1) discard();
    }

    @Override
    protected final void onHitBlock(BlockHitResult hit) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        SiegeTrigger.tryStart(serverLevel, getOperatorUUID(), hit.getBlockPos());

        BlockState state = serverLevel.getBlockState(hit.getBlockPos());
        state.onProjectileHit(serverLevel, state, hit, this);
        playSound(getDefaultHitGroundSoundEvent(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));

        if (getOwner() instanceof AbstractSiegeEntity) onSiegeImpact(serverLevel, hit);
        discard();
    }

    // ---- boilerplate

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SCSounds.BULLET_CRACK.get();
    }
}
