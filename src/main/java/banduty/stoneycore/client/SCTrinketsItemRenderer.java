package banduty.stoneycore.client;

import banduty.stoneycore.event.custom.RenderOverlayAndAdditionsEvents;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.patterns.PatternHelper;
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
import net.minecraft.util.Pair;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SCTrinketsItemRenderer implements TrinketRenderer {
    public SCTrinketsItemRenderer() {}

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!(stack.getItem() instanceof SCTrinketsItem scTrinketsItem)) return;
        BipedEntityModel<LivingEntity> model = scTrinketsItem.getModel();
        TrinketRenderer.followBodyRotations(entity, model);

        if (scTrinketsItem.hasCustomAngles()) model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

        VertexConsumer baseConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(scTrinketsItem.getTexturePath()));
        float[] color = DyeUtil.getFloatDyeColor(stack);
        if (stack.isIn(SCTags.BANNER_COMPATIBLE.getTag()))
            color = PatternHelper.getBannerDyeColor(stack);

        model.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
        renderOverlayAndAdditions(entity, stack, matrices, vertexConsumers, light, model);
        if (stack.isIn(SCTags.BANNER_COMPATIBLE.getTag())) renderBannerPatterns(stack, matrices, vertexConsumers, light, model);
    }

    private void renderOverlayAndAdditions(LivingEntity entity, ItemStack stack, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light,
                                           BipedEntityModel<LivingEntity> model) {
        RenderOverlayAndAdditionsEvents.EVENT.invoker().onRenderOverlayAndAdditionsEvents(entity, stack, matrices, vertexConsumers, light, model);
    }

    @Environment(EnvType.CLIENT)
    private void renderBannerPatterns(ItemStack stack, MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers, int light,
                                      BipedEntityModel<LivingEntity> model) {
        List<Pair<Identifier, DyeColor>> bannerPatterns = PatternHelper.getBannerPatterns(stack);
        if (!bannerPatterns.isEmpty()) {
            for (Pair<Identifier, DyeColor> patternPair : bannerPatterns) {
                Identifier pattern = patternPair.getLeft();
                DyeColor dyeColor = patternPair.getRight();

                float[] rgb = dyeColor.getColorComponents();

                VertexConsumer patternConsumer = vertexConsumers.getBuffer(SCRenderLayers.getArmorTranslucentNoCull(pattern));
                model.render(matrices, patternConsumer, light, OverlayTexture.DEFAULT_UV,
                        rgb[0], rgb[1], rgb[2], 1.0F);
            }
        }
    }
}
