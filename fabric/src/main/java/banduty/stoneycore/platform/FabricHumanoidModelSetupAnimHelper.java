package banduty.stoneycore.platform;

import banduty.stoneycore.event.custom.HumanoidModelSetupAnimEvents;
import banduty.stoneycore.platform.services.HumanoidModelSetupAnimHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class FabricHumanoidModelSetupAnimHelper implements HumanoidModelSetupAnimHelper {
    @Override
    public void afterSetAngles(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
        HumanoidModelSetupAnimEvents.AFTER.invoker().afterSetAngles(model, entity, limbAngle, limbDistance, age, headYaw, headPitch, ci);
    }

    @Override
    public void beforeSetAngles(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
        HumanoidModelSetupAnimEvents.BEFORE.invoker().beforeSetAngles(model, entity, limbAngle, limbDistance, age, headYaw, headPitch, ci);

    }
}