package banduty.stoneycore.mixin;

import banduty.stoneycore.lands.util.LandClientState;
import banduty.stoneycore.platform.ClientPlatform;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), cancellable = true)
    private void stoneycore$setAfterAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

        ClientPlatform.getHumanoidModelSetupAnimHelper().afterSetAngles(model, entity, limbAngle, limbDistance, age, headYaw, headPitch, ci);

        if (entity instanceof Player) {
            LandClientState state = LandClientState.get(entity.getUUID());
            if (state.isUnderSiege() && !state.isParticipant()) {
                float shakeSpeed = 0.5F;
                float shakeAmount = 0.4F;
                float time = age * shakeSpeed;

                model.rightArm.xRot = Mth.cos(time) * shakeAmount;
                model.leftArm.xRot = Mth.cos(time) * shakeAmount;
                model.rightArm.z = 2.3561945F;
                model.leftArm.z = -2.3561945F;
                model.rightArm.yRot = 0.1F;
                model.leftArm.yRot = -0.1F;
            }
        }
    }
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
    private void stoneycore$setBeforeAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

        ClientPlatform.getHumanoidModelSetupAnimHelper().beforeSetAngles(model, entity, limbAngle, limbDistance, age, headYaw, headPitch, ci);
    }
}
