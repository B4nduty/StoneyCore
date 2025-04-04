package banduty.stoneycore.client;

import banduty.stoneycore.event.custom.RenderOverlayAndAdditionsEvents;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.render.SCRenderLayers;
import banduty.stoneycore.util.itemdata.SCTags;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SCTrinketsItemRenderer implements TrinketRenderer {
    public SCTrinketsItemRenderer() {}

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!(stack.getItem() instanceof SCTrinketsItem scTrinketsItem)) return;
        BipedEntityModel<LivingEntity> model = scTrinketsItem.getModel();
        TrinketRenderer.followBodyRotations(entity, model);

        if (scTrinketsItem.unrenderCapeFeature()) {
            model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        }

        VertexConsumer baseConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(scTrinketsItem.getTexturePath()));
        float[] color = DyeUtil.getDyeColor(stack);

        model.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
        renderOverlayAndAdditions(entity, stack, matrices, vertexConsumers, light, model);
        if (stack.isIn(SCTags.BANNER_COMPATIBLE.getTag())) renderBannerPatterns(stack, matrices, vertexConsumers, light, model, scTrinketsItem);
    }

    private void renderOverlayAndAdditions(LivingEntity entity, ItemStack stack, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light,
                                           BipedEntityModel<LivingEntity> model) {
        RenderOverlayAndAdditionsEvents.EVENT.invoker().onRenderOverlayAndAdditionsEvents(entity, stack, matrices, vertexConsumers, light, model);
    }

    @Environment(EnvType.CLIENT)
    private void renderBannerPatterns(ItemStack stack, MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers, int light,
                                      BipedEntityModel<LivingEntity> model, SCTrinketsItem scTrinketsItem) {
        List<Identifier> bannerPatterns = scTrinketsItem.getBannerPatterns(stack);
        if (!bannerPatterns.isEmpty()) {
            for (Identifier pattern : bannerPatterns) {
                String path = pattern.getPath();
                String[] parts = path.split("/");
                String patternWithColor = parts[parts.length - 1];
                String[] patternParts = patternWithColor.split("_");

                String baseTextureName = patternParts[0];
                int colorIndex = Integer.parseInt(patternParts[patternParts.length - 1]);

                String newPath = path.replace(patternWithColor, baseTextureName + ".png");
                Identifier textureIdentifier = new Identifier(pattern.getNamespace(), newPath);

                DyeColor dyeColor = DyeColor.byId(colorIndex);
                if (dyeColor == null) {
                    dyeColor = DyeColor.WHITE;
                }

                float[] rgb = dyeColor.getColorComponents();

                VertexConsumer patternConsumer = vertexConsumers.getBuffer(SCRenderLayers.getArmorTranslucentNoCull(textureIdentifier));
                model.render(matrices, patternConsumer, light, OverlayTexture.DEFAULT_UV, rgb[0], rgb[1], rgb[2], 1.0F);
            }
        }
    }
}
