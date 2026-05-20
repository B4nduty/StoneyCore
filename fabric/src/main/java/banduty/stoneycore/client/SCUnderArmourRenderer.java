package banduty.stoneycore.client;

import banduty.stoneycore.items.custom.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.model.UnderArmourBootsModel;
import banduty.stoneycore.model.UnderArmourChestplateModel;
import banduty.stoneycore.model.UnderArmourHelmetModel;
import banduty.stoneycore.model.UnderArmourLeggingsModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SCUnderArmourRenderer implements ArmorRenderer {
    private UnderArmourHelmetModel helmetModel;
    private UnderArmourChestplateModel chestplateModel;
    private UnderArmourLeggingsModel leggingsModel;
    private UnderArmourBootsModel bootsModel;

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel) {
        ArmorItem armorItem = (ArmorItem) stack.getItem();
        HumanoidModel<LivingEntity> model = getModel(armorItem);
        if (model != null) {
            contextModel.copyPropertiesTo(model);

            var materialKey = armorItem.getMaterial().unwrapKey().orElse(null);
            if (materialKey == null) return;
            String namespace = materialKey.location().getNamespace();
            String path = materialKey.location().getPath();

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                    RenderType.armorCutoutNoCull(ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + ".png")));
            if (armorItem instanceof SCDyeableUnderArmor) {
                int color = DyedItemColor.getOrDefault(stack, -1);

                ResourceLocation textureOverlayPath = ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + "_overlay.png");

                model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);

                ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, textureOverlayPath);
            } else model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -1);
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
