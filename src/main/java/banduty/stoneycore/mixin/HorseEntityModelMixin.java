package banduty.stoneycore.mixin;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseEntityModel.class)
public abstract class HorseEntityModelMixin<T extends AbstractHorseEntity> extends AnimalModel<T> {
    @Final
    @Shadow
    private ModelPart rightFrontLeg;
    @Final
    @Shadow
    private ModelPart leftFrontLeg;
    @Final
    @Shadow
    private ModelPart rightHindLeg;
    @Final
    @Shadow
    private ModelPart leftHindLeg;

    @Inject(method = "setAngles(Lnet/minecraft/entity/passive/AbstractHorseEntity;FFFFF)V", at = @At("TAIL"), cancellable = true)
    private void kingdomsieges$setAngles(T abstractHorseEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (abstractHorseEntity instanceof HorseEntity horseEntity && horseEntity.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {

            float movement = (float) Math.sqrt(abstractSiegeEntity.getVelocity().x * abstractSiegeEntity.getVelocity().x + abstractSiegeEntity.getVelocity().z * abstractSiegeEntity.getVelocity().z);
            float stepSpeed = 0.1F; // Lower = slower steps
            float stepWidth = 10.0F; // Higher = bigger leg swing
            float swing = MathHelper.cos(h * stepSpeed);

            rightFrontLeg.pitch = swing * stepWidth * movement;
            leftFrontLeg.pitch = -swing * stepWidth * movement;
            rightHindLeg.pitch = -swing * stepWidth * movement;
            leftHindLeg.pitch = swing * stepWidth * movement;

            ci.cancel();
        }
    }
}
