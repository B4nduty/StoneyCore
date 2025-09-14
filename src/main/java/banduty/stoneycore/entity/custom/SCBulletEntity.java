package banduty.stoneycore.entity.custom;

import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.SCDamageCalculator;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class SCBulletEntity extends PersistentProjectileEntity {
    private SCDamageCalculator.DamageType damageType;
    private double damage;

    public SCBulletEntity(EntityType<? extends PersistentProjectileEntity> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    public SCBulletEntity(LivingEntity shooter, World world) {
        super(ModEntities.SC_BULLET.get(), shooter, world);
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getWorld().isClient()) return;

        if (entityHitResult.getEntity() instanceof LivingEntity target && this.getOwner() instanceof LivingEntity livingEntity) {
            damage = SCDamageCalculator.getSCDamage(target, this.damage, this.damageType);
            SCDamageCalculator.applyDamage(target, livingEntity, ItemStack.EMPTY, damage);
            if (this.isOnFire()) target.setOnFireFor(5);
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (this.getWorld().isClient()) return;
        World world = this.getWorld();
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(world instanceof ServerWorld serverWorld)) return;

        if ((state.getBlock() == Blocks.POINTED_DRIPSTONE || state.getBlock() instanceof AbstractGlassBlock)) {
            serverWorld.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));

            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

            world.emitGameEvent(null, GameEvent.BLOCK_DESTROY, pos);
        }
        BlockState blockState = this.getWorld().getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.getWorld(), blockState, blockHitResult, this);
        Vec3d vec3d = blockHitResult.getPos().subtract(this.getX(), this.getY(), this.getZ());
        this.setVelocity(vec3d);
        Vec3d vec3d2 = vec3d.normalize().multiply(0.05F);
        this.setPos(this.getX() - vec3d2.x, this.getY() - vec3d2.y, this.getZ() - vec3d2.z);


        for (PlayerEntity player : serverWorld.getPlayers()) {
            if (player != null) {
                Vec3d playerPos = player.getPos();
                Vec3d impactPos = blockHitResult.getPos();
                double distance = playerPos.distanceTo(impactPos);

                float volume = (float) Math.max(0, 1 - (distance * 0.1));

                this.playSound(this.getSound(), volume, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            }
        }

        this.inGround = true;
        this.shake = 7;
        this.setCritical(false);
        this.setPierceLevel((byte) 0);
        this.setSound(SoundEvents.ENTITY_ARROW_HIT);
        this.setShotFromCrossbow(false);
        this.discard();
    }

    @Override
    protected SoundEvent getHitSound() {
        return ModSounds.BULLET_CRACK.get();
    }

    public void setDamageAmount(float damage) {
        this.damage = damage;
    }

    public void setDamageType(SCDamageCalculator.DamageType damageType) {
        this.damageType = damageType;
    }
}