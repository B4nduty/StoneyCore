package banduty.stoneycore.mixin;

import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
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
        if (SCMeleeWeaponDefinitionsLoader.containsItem(entity.getMainHandStack().getItem())) {
            var reachDistance = SCWeaponUtil.getMaxDistance(entity.getMainHandStack().getItem());
            cir.setReturnValue(reachDistance);
        }
    }
}
