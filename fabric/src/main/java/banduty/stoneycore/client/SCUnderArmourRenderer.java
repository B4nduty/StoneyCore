package banduty.stoneycore.client;

import banduty.stoneycore.items.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.model.UnderArmourBootsModel;
import banduty.stoneycore.model.UnderArmourChestplateModel;
import banduty.stoneycore.model.UnderArmourHelmetModel;
import banduty.stoneycore.model.UnderArmourLeggingsModel;
import banduty.stoneycore.util.DyeUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SCUnderArmourRenderer implements ArmorRenderer {
    private HumanoidModel<LivingEntity> model;

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel) {
        ArmorItem armorItem = (ArmorItem) stack.getItem();
        HumanoidModel<LivingEntity> model = getModel(armorItem);
        if (model != null) {
            contextModel.copyPropertiesTo(model);

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                    RenderType.armorCutoutNoCull(new ResourceLocation(BuiltInRegistries.ITEM.getKey(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png")));
            if (armorItem instanceof SCDyeableUnderArmor) {
                float[] color = DyeUtil.getFloatDyeColor(stack);

                ResourceLocation textureOverlayPath = getOverlayIdentifier(armorItem);

                model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1.0F);

                ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, textureOverlayPath);
            } else model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        }
    }

    private static @NotNull ResourceLocation getOverlayIdentifier(ArmorItem armorItem) {
        ResourceLocation originalIdentifier = new ResourceLocation(BuiltInRegistries.ITEM.getKey(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png");

        String textureOverlayString = originalIdentifier.getPath();

        if (textureOverlayString.endsWith(".png")) {
            textureOverlayString = textureOverlayString.substring(0, textureOverlayString.length() - 4);
        }

        textureOverlayString += "_overlay.png";

        return new ResourceLocation(originalIdentifier.getNamespace(), textureOverlayString);
    }

    public @Nullable HumanoidModel<LivingEntity> getModel(ArmorItem armorItem) {
        if (this.model == null && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.model = switch (armorItem.getType()) {
                case HELMET -> new UnderArmourHelmetModel(UnderArmourHelmetModel.getTexturedModelData().bakeRoot());
                case CHESTPLATE -> new UnderArmourChestplateModel(UnderArmourChestplateModel.getTexturedModelData().bakeRoot());
                case LEGGINGS -> new UnderArmourLeggingsModel(UnderArmourLeggingsModel.getTexturedModelData().bakeRoot());
                case BOOTS -> new UnderArmourBootsModel(UnderArmourBootsModel.getTexturedModelData().bakeRoot());
            };
        }
        return this.model;
    }
}
