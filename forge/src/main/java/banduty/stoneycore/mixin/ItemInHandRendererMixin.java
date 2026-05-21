package banduty.stoneycore.mixin;

import banduty.stoneycore.util.data.itemdata.SCTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(" +
                            "Lnet/minecraft/world/entity/LivingEntity;" +
                            "Lnet/minecraft/world/item/ItemStack;" +
                            "Lnet/minecraft/world/item/ItemDisplayContext;" +
                            "Z" +
                            "Lcom/mojang/blaze3d/vertex/PoseStack;" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                            "I)V"
            )
    )
    private void stoneycore$shieldBlockTransform(
            AbstractClientPlayer player,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equipProgress,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {

        if (!stack.is(SCTags.WEAPONS_SHIELD.getTag())) {
            return;
        }

        if (!player.isUsingItem() || player.getUseItem() != stack) {
            return;
        }

        HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();

        boolean rightArm = arm == HumanoidArm.RIGHT;

        // Vanilla-like sword blocking pose
        if (rightArm) {
            poseStack.translate(-0.1F, 0.1F, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(5.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(60.0F));
        } else {
            poseStack.translate(0.1F, 0.1F, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-5.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-60.0F));
        }
    }
}