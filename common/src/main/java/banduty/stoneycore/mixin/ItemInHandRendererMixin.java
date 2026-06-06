package banduty.stoneycore.mixin;

import banduty.stoneycore.client.render.ArmorAttachmentRenderManager;
import banduty.stoneycore.items.custom.armor.deco.Deco;
import banduty.stoneycore.items.custom.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.model.UnderArmourArmModel;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private final Minecraft stoneyCore$client = Minecraft.getInstance();

    @Inject(method = "renderMapHand", at = @At("TAIL"))
    private void stoneycore$renderArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, HumanoidArm arm, CallbackInfo ci) {
        poseStack.pushPose();
        float f = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
        poseStack.translate(f * 0.3F, -1.1F, 0.45F);
        stoneyCore$modelLoader(poseStack, multiBufferSource, light, arm);
        poseStack.popPose();
    }

    @Inject(method = "renderPlayerArm", at = @At("TAIL"))
    private void stoneycore$renderArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
        stoneyCore$modelLoader(poseStack, multiBufferSource, light, arm);
    }

    @Unique
    private void stoneyCore$modelLoader(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, HumanoidArm arm) {
        LocalPlayer player = this.stoneyCore$client.player;
        if (player == null) return;

        ItemStack chestStack = player.getInventory().getArmor(2);
        if (chestStack.getItem() instanceof ArmorItem armorItem &&
                ArmorDefinitionsStorage.containsItem(armorItem) &&
                armorItem.getEquipmentSlot() == ArmorItem.Type.CHESTPLATE.getSlot()) {

            UnderArmourArmModel model = new UnderArmourArmModel(UnderArmourArmModel.getTexturedModelData().bakeRoot());

            var materialKey = armorItem.getMaterial().unwrapKey().orElse(null);
            if (materialKey == null) return;
            String namespace = materialKey.location().getNamespace();
            String path = materialKey.location().getPath();

            ResourceLocation baseTexture = ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + ".png");
            VertexConsumer baseConsumer = multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(baseTexture));

            int color = -1;
            if (chestStack.getItem() instanceof SCDyeableUnderArmor scDyeableUnderArmor) {
                color = DyedItemColor.getOrDefault(chestStack, scDyeableUnderArmor.getDefaultColor());
            }

            PlayerModel<?> playerModel = ((PlayerModel<?>) ((LivingEntityRenderer<?, ?>)
                    Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player)).getModel());

            float armOffset = 0.0625f;

            poseStack.pushPose();
            if (arm == HumanoidArm.RIGHT) {
                poseStack.translate(-armOffset, 0.0F, 0.0F);
                model.armorRightArm.copyFrom(playerModel.rightArm);
                model.armorRightArm.render(poseStack, baseConsumer, light, OverlayTexture.NO_OVERLAY, color);
            } else {
                poseStack.translate(armOffset, 0.0F, 0.0F);
                model.armorLeftArm.copyFrom(playerModel.leftArm);
                model.armorLeftArm.render(poseStack, baseConsumer, light, OverlayTexture.NO_OVERLAY, color);
            }
            poseStack.popPose();

            if (chestStack.getItem() instanceof SCDyeableUnderArmor) {
                ResourceLocation overlayTexture = ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + "_overlay.png");
                VertexConsumer overlayConsumer = multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(overlayTexture));
                if (arm == HumanoidArm.RIGHT) {
                    poseStack.translate(-armOffset, 0.0F, 0.0F);
                    model.armorRightArm.copyFrom(playerModel.rightArm);
                    model.armorRightArm.render(poseStack, overlayConsumer, light, OverlayTexture.NO_OVERLAY, -1);
                } else {
                    poseStack.translate(armOffset, 0.0F, 0.0F);
                    model.armorLeftArm.copyFrom(playerModel.leftArm);
                    model.armorLeftArm.render(poseStack, overlayConsumer, light, OverlayTexture.NO_OVERLAY, -1);
                }
            }
        }

        for (ItemStack itemStack : player.getArmorSlots()) {
            for (ItemStack armorAttachments : SCUnderArmor.getArmorAttachments(itemStack)) {
                ArmorAttachmentRenderManager.getOrLookUp(armorAttachments.getItem())
                        .ifPresent(renderer -> {
                            renderer.onRenderInFirstPerson(player, armorAttachments, poseStack, multiBufferSource, light, arm);
                        });

                for (ItemStack subDecoStack : Deco.getDeco(itemStack)) {
                    if (!subDecoStack.isEmpty()) {
                        ArmorAttachmentRenderManager.getOrLookUp(subDecoStack.getItem()).ifPresent(render ->
                                render.onRenderInFirstPerson(player, subDecoStack, poseStack, multiBufferSource, light, arm)
                        );
                    }
                }
            }
        }
    }
}