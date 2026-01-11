package banduty.stoneycore.entity.custom;

import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.SCDamageCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SCBulletEntity extends AbstractArrow {
    private SCDamageCalculator.DamageType damageType;
    private double damage;

    public SCBulletEntity(EntityType<? extends AbstractArrow> entityEntityType, Level level) {
        super(entityEntityType, level);
    }

    public SCBulletEntity(LivingEntity shooter, Level level) {
        super(Services.SC_BULLET_ENTITY.getBulletEntity(), shooter, level);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level().isClientSide()) return;

        if (entityHitResult.getEntity() instanceof LivingEntity target && this.getOwner() instanceof LivingEntity livingEntity) {
            damage = SCDamageCalculator.getSCDamage(target, this.damage, this.damageType);
            SCDamageCalculator.applyDamage(target, livingEntity, ItemStack.EMPTY, damage);
            if (this.isOnFire()) target.setRemainingFireTicks(5);
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (this.level().isClientSide()) return;
        Level level = this.level();
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(level instanceof ServerLevel serverLevel)) return;

        if ((state.getBlock() == Blocks.POINTED_DRIPSTONE || state.getBlock() instanceof AbstractGlassBlock)) {
            serverLevel.levelEvent(2001, pos, Block.getId(state));

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

            level.gameEvent(this, GameEvent.BLOCK_DESTROY, pos);
        }
        BlockState blockState = this.level().getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.level(), blockState, blockHitResult, this);
        Vec3 vec3 = blockHitResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec3d2 = vec3.normalize().scale(0.05F);
        this.setPos(this.getX() - vec3d2.x, this.getY() - vec3d2.y, this.getZ() - vec3d2.z);

        for (Player player : serverLevel.players()) {
            if (player != null) {
                Vec3 playerPos = player.position();
                Vec3 impactPos = blockHitResult.getLocation();
                double distance = playerPos.distanceTo(impactPos);

                float volume = (float) Math.max(0, 1 - (distance * 0.1));

                this.playSound(this.getDefaultHitGroundSoundEvent(), volume, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            }
        }

        this.inGround = true;
        this.shakeTime = 7;
        this.setCritArrow(false);
        this.setPierceLevel((byte) 0);
        this.setSoundEvent(SoundEvents.ARROW_HIT);
        this.setShotFromCrossbow(false);
        this.discard();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return Services.ABSTRACT_SIEGE_ENTITY.getDefaultHitGroundSoundEvent();
    }

    public void setDamageAmount(float damage) {
        this.damage = damage;
    }

    public void setDamageType(SCDamageCalculator.DamageType damageType) {
        this.damageType = damageType;
    }
}