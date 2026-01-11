package banduty.stoneycore.mixin;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vindicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vindicator.class)
public class VindicatorEntityMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void stoneycore$registerGoals(CallbackInfo ci) {
        Vindicator vindicator = (Vindicator) (Object) this;
        GoalSelector targetSelector = ((MobEntityAccessor) vindicator).getTargetSelector();
        targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(vindicator, AbstractSiegeEntity.class, true));
    }
}
