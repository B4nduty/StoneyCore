package banduty.stoneycore.mixin;

import banduty.stoneycore.util.itemdata.SCTags;
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
        if (entity.getMainHandStack().isIn(SCTags.MELEE_COMBAT_MECHANICS.getTag())) {
            var reachDistance = SCWeaponUtil.getMaxDistance(entity.getMainHandStack().getItem());
            cir.setReturnValue(reachDistance);
        }
    }
}
