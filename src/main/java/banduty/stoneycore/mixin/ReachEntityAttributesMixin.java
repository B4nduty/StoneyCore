package banduty.stoneycore.mixin;

import banduty.stoneycore.items.item.SCWeapon;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReachEntityAttributes.class)
public class ReachEntityAttributesMixin  {
    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private static void stoneycore$getReachDistance(LivingEntity entity, double baseReachDistance, CallbackInfoReturnable<Double> cir) {
        if (entity.getMainHandStack().getItem() instanceof SCWeapon scWeapon) {
            var reachDistance = SCWeaponUtil.getMaxDistance(scWeapon);
            cir.setReturnValue(reachDistance);
        }
    }
}
