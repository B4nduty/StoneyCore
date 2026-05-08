package banduty.stoneycore.entity.custom;

import banduty.stoneycore.combat.damagetype.SCDamageApplier;
import banduty.stoneycore.combat.damagetype.SCDamageCalculator;
import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.sounds.SCSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

public abstract class AbstractSiegeProjectile extends AbstractArrow {
    private SCDamageType damageType;

    public AbstractSiegeProjectile(EntityType<? extends AbstractSiegeProjectile> type, LivingEntity shooter, Level level) {
        super(type, level);
        this.setOwner(shooter);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (entityHitResult.getEntity().level().isClientSide()) return;
        if (entityHitResult.getEntity() instanceof LivingEntity target) {
            double damage = SCDamageCalculator.applyArmor(target, (float) this.getBaseDamage(), this.damageType);
            if (this.getOwner() instanceof AbstractSiegeEntity abstractSiegeEntity) SCDamageApplier.apply(target, abstractSiegeEntity.getOwner(), ItemStack.EMPTY, damage);
        }
        setDeltaMovement(getDeltaMovement().scale(-0.9));
        setBaseDamage(getBaseDamage() * 0.9);
        if (getBaseDamage() <= 1) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (this.getOwner() instanceof AbstractSiegeEntity abstractSiegeEntity && abstractSiegeEntity.getOwner() instanceof Player player
                && SiegeManager.getPlayerSiege(serverLevel, player.getUUID()).isEmpty()) {
            Optional<Land> currentLand = LandState.get(serverLevel).getLandAt(this.getOnPos());
            if (currentLand.isPresent() && !currentLand.get().getOwnerUUID().equals(player.getUUID())) {
                SiegeManager.startSiege(serverLevel, player.getUUID(), currentLand.get().getOwnerUUID());
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);

        Level world = this.level();
        if (!(world instanceof ServerLevel serverLevel) || !(this.getOwner() instanceof AbstractSiegeEntity abstractSiegeEntity)) return;

        if (abstractSiegeEntity.getOwner() instanceof Player player && SiegeManager.getPlayerSiege(serverLevel, player.getUUID()).isEmpty()) {
            Optional<Land> currentLand = LandState.get(serverLevel).getLandAt(blockHitResult.getBlockPos());
            if (currentLand.isPresent() && !currentLand.get().getOwnerUUID().equals(player.getUUID())) {
                SiegeManager.startSiege(serverLevel, player.getUUID(), currentLand.get().getOwnerUUID());
            }
        }
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SCSounds.BULLET_CRACK.get();
    }

    public void setDamageType(SCDamageType damageType) {
        this.damageType = damageType;
    }
}
