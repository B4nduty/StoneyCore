package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.RenderFirstPersonAccesoryArmorEvents;
import banduty.stoneycore.items.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.model.UnderArmourArmModel;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Unique
    private final Minecraft client = Minecraft.getInstance();

    @Inject(method = "renderMapHand", at = @At("TAIL"))
    private void stoneycore$renderArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, HumanoidArm arm, CallbackInfo ci) {
        poseStack.pushPose();
        float f = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
        poseStack.translate(f * 0.3F, -1.1F, 0.45F);
        modelLoader(poseStack, multiBufferSource, light, arm);
        poseStack.popPose();
    }

    @Inject(method = "renderPlayerArm", at = @At("TAIL"))
    private void stoneycore$renderArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
        modelLoader(poseStack, multiBufferSource, light, arm);
    }

    @Unique
    private void modelLoader(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, HumanoidArm arm) {
        LocalPlayer player = this.client.player;
        if (player == null) return;

        ItemStack chestStack = player.getInventory().getArmor(2); // Chestplate
        if (chestStack.getItem() instanceof ArmorItem armorItem &&
                ArmorDefinitionsLoader.containsItem(armorItem) &&
                armorItem.getEquipmentSlot() == ArmorItem.Type.CHESTPLATE.getSlot()) {

            // Load your custom model
            UnderArmourArmModel model = new UnderArmourArmModel(UnderArmourArmModel.getTexturedModelData().bakeRoot());

            // Get structureId for the base layer
            ResourceLocation baseTexture = new ResourceLocation(
                    BuiltInRegistries.ITEM.getKey(armorItem).getNamespace(),
                    "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png"
            );
            VertexConsumer baseConsumer = multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(baseTexture));

            // Handle color if it's dyeable
            float[] color = new float[] { 1f, 1f, 1f };
            if (chestStack.getItem() instanceof SCDyeableUnderArmor) {
                color = DyeUtil.getFloatDyeColor(chestStack);
            }

            // Render the arm part based on left/right
            PlayerModel<?> playerModel = ((PlayerModel<?>) ((LivingEntityRenderer<?, ?>)
                    Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player)).getModel());

            float armOffset = 0.0625f; // This is 1px in model units

            poseStack.pushPose();
            if (arm == HumanoidArm.RIGHT) {
                poseStack.translate(-armOffset, 0.0F, 0.0F);
                model.armorRightArm.copyFrom(playerModel.rightArm);
                model.armorRightArm.render(poseStack, baseConsumer, light, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1.0F);
            } else {
                poseStack.translate(armOffset, 0.0F, 0.0F);
                model.armorLeftArm.copyFrom(playerModel.leftArm);
                model.armorLeftArm.render(poseStack, baseConsumer, light, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1.0F);
            }
            poseStack.popPose();

            // Render overlay if available
            ResourceLocation overlayTexture = getOverlayIdentifier(armorItem);
            if (!overlayTexture.getPath().isEmpty()) {
                ArmorRenderer.renderPart(poseStack, multiBufferSource, light, chestStack, model, overlayTexture);
            }
        }

        // Accessories rendering support
        AccessoriesCapability.getOptionally(player).ifPresent(accessories -> {
            for (SlotEntryReference equipped : accessories.getAllEquipped()) {
                ItemStack itemStack = equipped.stack();
                RenderFirstPersonAccesoryArmorEvents.EVENT.invoker().onRenderInFirstPerson(player, itemStack, poseStack, multiBufferSource, light, arm);
            }
        });
    }

    @Unique
    private @NotNull ResourceLocation getOverlayIdentifier(ArmorItem armorItem) {
        ResourceLocation originalIdentifier = new ResourceLocation(BuiltInRegistries.ITEM.getKey(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png");

        String textureOverlayString = originalIdentifier.getPath();

        if (textureOverlayString.endsWith(".png") && armorItem instanceof DyeableArmorItem) {
            textureOverlayString = textureOverlayString.substring(0, textureOverlayString.length() - 4);
        }

        else return new ResourceLocation("");

        return new ResourceLocation(originalIdentifier.getNamespace(), textureOverlayString);
    }
}