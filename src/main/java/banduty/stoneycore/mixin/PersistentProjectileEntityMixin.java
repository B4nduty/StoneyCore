package banduty.stoneycore.mixin;

import banduty.stoneycore.util.DeflectChanceHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {
    @Shadow protected abstract ItemStack asItemStack();

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    private void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!(entityHitResult.getEntity().getWorld() instanceof ServerWorld serverWorld)) return;
        PersistentProjectileEntity projectileEntity = (PersistentProjectileEntity) (Object) this;

        if (asItemStack() == null || asItemStack().isEmpty()) {
            return;
        }

        if (projectileEntity != null) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && DeflectChanceHelper.shouldDeflect(livingEntity, asItemStack())) {
                projectileEntity.setVelocity(projectileEntity.getVelocity().multiply(0.5).multiply(-1));
                projectileEntity.setDamage(projectileEntity.getDamage() * 0.5);
                projectileEntity.setYaw(projectileEntity.getYaw() + 180);
                if (projectileEntity.getDamage() <= 1) projectileEntity.discard();
                for (PlayerEntity player : serverWorld.getPlayers()) {
                    if (player != null) {
                        Vec3d playerPos = player.getPos();
                        Vec3d impactPos = entityHitResult.getPos();
                        double distance = playerPos.distanceTo(impactPos);

                        float volume = (float) Math.max(0, 1 - (distance * 0.1));

                        projectileEntity.playSound(SoundEvents.BLOCK_ANVIL_PLACE, volume, 2.5F / (new Random().nextFloat() + 0.5F));
                    }
                }
                ci.cancel();
            }
        }
    }
}
