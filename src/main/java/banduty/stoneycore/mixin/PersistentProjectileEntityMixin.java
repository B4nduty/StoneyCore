package banduty.stoneycore.mixin;

import banduty.stoneycore.util.DeflectChanceHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {
    @Shadow protected abstract ItemStack asItemStack();

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    private void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (entityHitResult.getEntity().getWorld().isClient()) return;
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
                ci.cancel();
            }
        }
    }
}
