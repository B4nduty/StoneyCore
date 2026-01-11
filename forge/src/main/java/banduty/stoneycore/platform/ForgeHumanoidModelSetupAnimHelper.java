package banduty.stoneycore.platform;

import banduty.stoneycore.event.custom.HumanoidModelSetupAnimEvents;
import banduty.stoneycore.platform.services.HumanoidModelSetupAnimHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ForgeHumanoidModelSetupAnimHelper implements HumanoidModelSetupAnimHelper {
    @Override
    public void afterSetAngles(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new HumanoidModelSetupAnimEvents.After(model, entity, limbAngle, limbDistance, age, headYaw, headPitch, ci));
    }

    @Override
    public void beforeSetAngles(HumanoidModel<?> model, LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new HumanoidModelSetupAnimEvents.Before(model, entity, limbAngle, limbDistance, age, headYaw, headPitch, ci));
    }
}