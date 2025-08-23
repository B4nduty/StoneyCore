package banduty.stoneycore.mixin;

import banduty.stoneycore.goals.TargetSiegeEntitiesGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.VindicatorEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VindicatorEntity.class)
public class VindicatorEntityMixin {
    @Inject(method = "initGoals", at = @At("TAIL"))
    private void initGoals(CallbackInfo ci) {
        VindicatorEntity vindicator = (VindicatorEntity) (Object) this;
        GoalSelector targetSelector = ((MobEntityAccessor) vindicator).getTargetSelector();
        targetSelector.add(1, new TargetSiegeEntitiesGoal(vindicator));
    }
}
