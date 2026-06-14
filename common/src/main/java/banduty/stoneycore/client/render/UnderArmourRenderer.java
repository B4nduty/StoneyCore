package banduty.stoneycore.client.render;

import banduty.stoneycore.items.custom.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.model.UnderArmourBootsModel;
import banduty.stoneycore.model.UnderArmourChestplateModel;
import banduty.stoneycore.model.UnderArmourHelmetModel;
import banduty.stoneycore.model.UnderArmourLeggingsModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UnderArmourRenderer {

    private final UnderArmourHelmetModel helmetModel;
    private final UnderArmourChestplateModel chestplateModel;
    private final UnderArmourLeggingsModel leggingsModel;
    private final UnderArmourBootsModel bootsModel;

    public static final UnderArmourRenderer INSTANCE = new UnderArmourRenderer();

    private UnderArmourRenderer() {
        this.helmetModel = new UnderArmourHelmetModel(UnderArmourHelmetModel.getTexturedModelData().bakeRoot());
        this.chestplateModel = new UnderArmourChestplateModel(UnderArmourChestplateModel.getTexturedModelData().bakeRoot());
        this.leggingsModel = new UnderArmourLeggingsModel(UnderArmourLeggingsModel.getTexturedModelData().bakeRoot());
        this.bootsModel = new UnderArmourBootsModel(UnderArmourBootsModel.getTexturedModelData().bakeRoot());
    }

    public void renderBaseArmor(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack stack,
                                LivingEntity livingEntity, int packedLight, HumanoidModel<LivingEntity> contextModel,
                                float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                float netHeadYaw, float headPitch) {
        if (!(stack.getItem() instanceof SCUnderArmor scUnderArmor)) return;

        HumanoidModel<LivingEntity> model = getModel(scUnderArmor);
        if (model == null) return;

        contextModel.copyPropertiesTo(model);

        var materialKey = scUnderArmor.getMaterial().unwrapKey().orElse(null);
        if (materialKey == null) return;

        String namespace = materialKey.location().getNamespace();
        String path = materialKey.location().getPath();

        ResourceLocation baseTex = ArmorTextureCache.getBaseTexture(namespace, path);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(baseTex));

        if (scUnderArmor instanceof SCDyeableUnderArmor scDyeableUnderArmor) {
            int color = DyedItemColor.getOrDefault(stack, scDyeableUnderArmor.getDefaultColor());
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

            ResourceLocation overlayTex = ArmorTextureCache.getOverlayTexture(namespace, path);
            VertexConsumer overlayConsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(overlayTex));
            model.renderToBuffer(poseStack, overlayConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        } else {
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        }
    }

    public void renderAttachments(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack stack,
                                  LivingEntity entity, int packedLight, HumanoidModel<LivingEntity> contextModel,
                                  float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                  float netHeadYaw, float headPitch) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SCUnderArmor)) return;

        List<ItemStack> attachments = SCUnderArmor.getArmorAttachments(stack);
        for (ItemStack itemStack : attachments) {
            ArmorAttachmentRenderer renderer = ArmorAttachmentRenderManager.getRenderer(itemStack.getItem());
            if (renderer != null) {
                renderer.render(poseStack, bufferSource, packedLight, entity, itemStack, contextModel,
                        limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }
    }

    public @Nullable HumanoidModel<LivingEntity> getModel(ArmorItem armorItem) {
        return switch (armorItem.getType()) {
            case HELMET -> helmetModel;
            case CHESTPLATE -> chestplateModel;
            case LEGGINGS -> leggingsModel;
            case BOOTS -> bootsModel;
            default -> null;
        };
    }

    public <T extends LivingEntity, A extends HumanoidModel<T>> void renderBaseArmor(
            PoseStack poseStack, MultiBufferSource buffer, ItemStack itemStack, T livingEntity, int packedLight, A a) {
    }
}