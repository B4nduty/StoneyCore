package banduty.stoneycore.mixin;

import banduty.stoneycore.util.data.itemdata.SCTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin<T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>> {

    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD")
    )
    private void adjustThirdPersonItem(
            LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci
    ) {
        if (!itemStack.is(SCTags.WEAPONS_SHIELD.getTag())) {
            return;
        }

        if (!livingEntity.isUsingItem() || livingEntity.getUseItem() != itemStack) {
            return;
        }

        if (arm == HumanoidArm.RIGHT) {
            poseStack.translate(-0.1F, 0, 0.1F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-20.0F));
        } else if (arm == HumanoidArm.LEFT) {
            poseStack.translate(0.1F, 0, 0.1F);
            poseStack.mulPose(Axis.YP.rotationDegrees(20.0F));
        }
    }
}