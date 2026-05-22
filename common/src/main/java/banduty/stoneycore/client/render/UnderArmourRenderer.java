package banduty.stoneycore.client.render;

import banduty.stoneycore.items.custom.armor.SCAccessory;
import banduty.stoneycore.items.custom.armor.deco.Deco;
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

public class UnderArmourRenderer {
    private UnderArmourHelmetModel helmetModel;
    private UnderArmourChestplateModel chestplateModel;
    private UnderArmourLeggingsModel leggingsModel;
    private UnderArmourBootsModel bootsModel;

    public static final UnderArmourRenderer INSTANCE = new UnderArmourRenderer();

    /**
     * Renders the base UnderArmor armor layer.
     */
    public void renderBaseArmor(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack stack,
                                int packedLight, HumanoidModel<LivingEntity> contextModel) {
        if (!(stack.getItem() instanceof SCUnderArmor scUnderArmor)) return;

        HumanoidModel<LivingEntity> model = getModel(scUnderArmor);
        if (model == null) return;

        contextModel.copyPropertiesTo(model);

        var materialKey = scUnderArmor.getMaterial().unwrapKey().orElse(null);
        if (materialKey == null) return;
        String namespace = materialKey.location().getNamespace();
        String path = materialKey.location().getPath();

        VertexConsumer vertexConsumer = bufferSource.getBuffer(
                RenderType.armorCutoutNoCull(ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + ".png")));

        if (scUnderArmor instanceof SCDyeableUnderArmor) {
            int color = DyedItemColor.getOrDefault(stack, -1);
            ResourceLocation textureOverlayPath = ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + "_overlay.png");

            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

            VertexConsumer overlayConsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(textureOverlayPath));
            model.renderToBuffer(poseStack, overlayConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        } else {
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        }
    }

    public void renderAccessories(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack stack,
                                  LivingEntity entity, int packedLight, HumanoidModel<LivingEntity> contextModel) {
        for (ItemStack itemStack : SCUnderArmor.getAccessories(stack)) {
            if (itemStack.getItem() instanceof SCAccessory) {
                AccessoryRenderManager.getOrLookUp(itemStack.getItem())
                        .ifPresent(renderer ->
                                renderer.render(poseStack, bufferSource, packedLight, entity, itemStack, contextModel));

                for (ItemStack subDecoStack : Deco.getDeco(itemStack)) {
                    if (!subDecoStack.isEmpty()) {
                        AccessoryRenderManager.getOrLookUp(subDecoStack.getItem()).ifPresent(render ->
                                render.render(poseStack, bufferSource, packedLight, entity, subDecoStack, contextModel));
                    }
                }
            }
        }
    }

    public @Nullable HumanoidModel<LivingEntity> getModel(ArmorItem armorItem) {
        return switch (armorItem.getType()) {
            case HELMET -> {
                if (this.helmetModel == null) {
                    this.helmetModel = new UnderArmourHelmetModel(UnderArmourHelmetModel.getTexturedModelData().bakeRoot());
                }
                yield this.helmetModel;
            }
            case CHESTPLATE -> {
                if (this.chestplateModel == null) {
                    this.chestplateModel = new UnderArmourChestplateModel(UnderArmourChestplateModel.getTexturedModelData().bakeRoot());
                }
                yield this.chestplateModel;
            }
            case LEGGINGS -> {
                if (this.leggingsModel == null) {
                    this.leggingsModel = new UnderArmourLeggingsModel(UnderArmourLeggingsModel.getTexturedModelData().bakeRoot());
                }
                yield this.leggingsModel;
            }
            case BOOTS -> {
                if (this.bootsModel == null) {
                    this.bootsModel = new UnderArmourBootsModel(UnderArmourBootsModel.getTexturedModelData().bakeRoot());
                }
                yield this.bootsModel;
            }
            default -> null;
        };
    }
}