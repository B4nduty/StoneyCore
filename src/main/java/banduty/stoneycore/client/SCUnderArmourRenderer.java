package banduty.stoneycore.client;

import banduty.stoneycore.items.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.model.UnderArmourBootsModel;
import banduty.stoneycore.model.UnderArmourChestplateModel;
import banduty.stoneycore.model.UnderArmourHelmetModel;
import banduty.stoneycore.model.UnderArmourLeggingsModel;
import banduty.stoneycore.util.DyeUtil;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SCUnderArmourRenderer implements ArmorRenderer {
    private BipedEntityModel<LivingEntity> model;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
        ArmorItem armorItem = (ArmorItem) stack.getItem();
        BipedEntityModel<LivingEntity> model = getModel(armorItem);
        if (model != null) {
            TrinketRenderer.followBodyRotations(entity, model);

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                    RenderLayer.getArmorCutoutNoCull(new Identifier(Registries.ITEM.getId(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png")));
            if (armorItem instanceof SCDyeableUnderArmor) {
                float[] color = DyeUtil.getDyeColor(stack);

                Identifier textureOverlayPath = getOverlayIdentifier(armorItem);

                model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);

                ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, textureOverlayPath);
            } else model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        }
    }

    private static @NotNull Identifier getOverlayIdentifier(ArmorItem armorItem) {
        Identifier originalIdentifier = new Identifier(Registries.ITEM.getId(armorItem).getNamespace(), "textures/models/armor/" + armorItem.getMaterial().toString().toLowerCase() + ".png");

        String textureOverlayString = originalIdentifier.getPath();

        if (textureOverlayString.endsWith(".png")) {
            textureOverlayString = textureOverlayString.substring(0, textureOverlayString.length() - 4);
        }

        textureOverlayString += "_overlay.png";

        return new Identifier(originalIdentifier.getNamespace(), textureOverlayString);
    }

    public @Nullable BipedEntityModel<LivingEntity> getModel(ArmorItem armorItem) {
        if (this.model == null && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.model = switch (armorItem.getSlotType()) {
                case HEAD -> new UnderArmourHelmetModel(UnderArmourHelmetModel.getTexturedModelData().createModel());
                case CHEST -> new UnderArmourChestplateModel(UnderArmourChestplateModel.getTexturedModelData().createModel());
                case LEGS -> new UnderArmourLeggingsModel(UnderArmourLeggingsModel.getTexturedModelData().createModel());
                case FEET -> new UnderArmourBootsModel(UnderArmourBootsModel.getTexturedModelData().createModel());
                default -> throw new IllegalArgumentException("Unsupported slot type: " + armorItem.getSlotType());
            };
        }
        return this.model;
    }
}
