package banduty.stoneycore.mixin;

import banduty.stoneycore.combat.DeflectChanceHelper;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true)
    private void stoneycore$deflect(HitResult hitResult, CallbackInfoReturnable<ProjectileDeflection> cir) {
        if (!(hitResult instanceof EntityHitResult entityHit)) return;

        Projectile projectile = (Projectile) (Object) this;
        if (DeflectChanceHelper.tryDeflect(projectile, entityHit)) {
            cir.setReturnValue(ProjectileDeflection.NONE);
        }
    }
}
