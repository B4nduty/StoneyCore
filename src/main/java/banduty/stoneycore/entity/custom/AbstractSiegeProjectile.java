package banduty.stoneycore.entity.custom;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.SCDamageCalculator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import java.util.Optional;

public abstract class AbstractSiegeProjectile extends PersistentProjectileEntity {
    private SCDamageCalculator.DamageType damageType;

    public AbstractSiegeProjectile(EntityType<? extends PersistentProjectileEntity> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    public AbstractSiegeProjectile(EntityType<? extends AbstractSiegeProjectile> scSiegeProjectile, LivingEntity shooter, World world) {
        super(scSiegeProjectile, shooter, world);
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (entityHitResult.getEntity().getWorld().isClient()) return;
        if (entityHitResult.getEntity() instanceof LivingEntity target) {
            float damage = SCDamageCalculator.getSCDamage(target, (float) this.getDamage(), this.damageType);
            if (this.getOwner() instanceof AbstractSiegeEntity abstractSiegeEntity) SCDamageCalculator.applyDamage(target, (LivingEntity) abstractSiegeEntity.getOwner(), ItemStack.EMPTY, damage);
        }
        setVelocity(getVelocity().multiply(-0.9));
        setDamage(getDamage() * 0.9);
        if (getDamage() <= 1) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        if (this.getOwner() instanceof AbstractSiegeEntity abstractSiegeEntity && abstractSiegeEntity.getOwner() instanceof PlayerEntity playerEntity
                && SiegeManager.getPlayerSiege(serverWorld, playerEntity.getUuid()).isEmpty()) {
            Optional<Land> currentLand = LandState.get(serverWorld).getLandAt(this.getBlockPos());
            if (currentLand.isPresent() && !currentLand.get().getOwnerUUID().equals(playerEntity.getUuid())) {
                SiegeManager.startSiege(serverWorld, playerEntity.getUuid(), currentLand.get().getOwnerUUID());
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        World world = this.getWorld();
        if (!(world instanceof ServerWorld serverWorld) || !(this.getOwner() instanceof AbstractSiegeEntity abstractSiegeEntity)) return;

        if (abstractSiegeEntity.getOwner() instanceof PlayerEntity playerEntity && SiegeManager.getPlayerSiege(serverWorld, playerEntity.getUuid()).isEmpty()) {
            Optional<Land> currentLand = LandState.get(serverWorld).getLandAt(blockHitResult.getBlockPos());
            if (currentLand.isPresent() && !currentLand.get().getOwnerUUID().equals(playerEntity.getUuid())) {
                SiegeManager.startSiege(serverWorld, playerEntity.getUuid(), currentLand.get().getOwnerUUID());
            }
        }
    }

    @Override
    protected SoundEvent getHitSound() {
        return ModSounds.BULLET_CRACK.get();
    }

    public void setDamageType(SCDamageCalculator.DamageType damageType) {
        this.damageType = damageType;
    }
}
