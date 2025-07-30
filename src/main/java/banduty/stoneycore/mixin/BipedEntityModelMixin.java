package banduty.stoneycore.mixin;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import banduty.stoneycore.event.custom.BipedEntityModelAnglesEvents;
import banduty.stoneycore.lands.util.LandClientState;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {
    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), cancellable = true)
    private void kingdomsieges$setAnglesHead(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, CallbackInfo ci) {
        BipedEntityModel<?> model = (BipedEntityModel<?>) (Object) this;
        if (entity.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
            BipedEntityModelAnglesEvents.BEFORE.invoker().beforeSetAngles(model, entity, abstractSiegeEntity, limbAngle, limbDistance, age, headYaw, headPitch, ci);
        }

        if (entity instanceof PlayerEntity) {
            LandClientState state = LandClientState.get(entity.getUuid());
            if (state.isUnderSiege() && !state.isParticipant()) {
                float shakeSpeed = 0.5F;
                float shakeAmount = 0.4F;
                float time = age * shakeSpeed;

                model.rightArm.pitch = MathHelper.cos(time) * shakeAmount;
                model.leftArm.pitch = MathHelper.cos(time) * shakeAmount;
                model.rightArm.roll = 2.3561945F;
                model.leftArm.roll = -2.3561945F;
                model.rightArm.yaw = 0.1F;
                model.leftArm.yaw = -0.1F;
            }
        }
    }
}
