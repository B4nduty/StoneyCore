package banduty.stoneycore.mixin;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseModel.class)
public abstract class HorseModelMixin<T extends AbstractHorse> extends AgeableListModel<T> {
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

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/horse/AbstractHorse;FFFFF)V", at = @At("TAIL"), cancellable = true)
    private void kingdomsieges$setAngles(T abstractHorse, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (abstractHorse instanceof Horse horseEntity && horseEntity.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {

            float movement = (float) Math.sqrt(abstractSiegeEntity.getDeltaMovement().x * abstractSiegeEntity.getDeltaMovement().x + abstractSiegeEntity.getDeltaMovement().z * abstractSiegeEntity.getDeltaMovement().z);
            float stepSpeed = 0.1F; // Lower = slower steps
            float stepWidth = 10.0F; // Higher = bigger leg swing
            float swing = Mth.cos(h * stepSpeed);

            rightFrontLeg.xRot = swing * stepWidth * movement;
            leftFrontLeg.xRot = -swing * stepWidth * movement;
            rightHindLeg.xRot = -swing * stepWidth * movement;
            leftHindLeg.xRot = swing * stepWidth * movement;

            ci.cancel();
        }
    }
}
