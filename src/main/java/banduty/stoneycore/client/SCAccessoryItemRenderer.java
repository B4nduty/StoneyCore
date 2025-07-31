package banduty.stoneycore.client;

import banduty.stoneycore.event.custom.RenderOverlayAndAdditionsEvents;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.patterns.PatternHelper;
import banduty.stoneycore.util.render.SCRenderLayers;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
public class SCAccessoryItemRenderer implements SimpleAccessoryRenderer {
    public SCAccessoryItemRenderer() {}

    @Override
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, MatrixStack matrices, EntityModel<M> model, VertexConsumerProvider multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(stack.getItem() instanceof SCAccessoryItem scAccessoryItem)) return;
        BipedEntityModel<LivingEntity> accessoryItemModel = scAccessoryItem.getModel(stack);
        if (model instanceof BipedEntityModel bipedEntityModel) bipedEntityModel.copyBipedStateTo(accessoryItemModel);

        if (scAccessoryItem.hasCustomAngles(stack)) accessoryItemModel.setAngles(reference.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        VertexConsumer baseConsumer = multiBufferSource.getBuffer(RenderLayer.getArmorCutoutNoCull(scAccessoryItem.getTexturePath(stack)));
        float[] color = DyeUtil.getFloatDyeColor(stack);
        if (stack.isIn(SCTags.BANNER_COMPATIBLE.getTag()))
            color = PatternHelper.getBannerDyeColor(stack);

        accessoryItemModel.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
        renderOverlayAndAdditions(reference.entity(), stack, matrices, multiBufferSource, light, accessoryItemModel);
        if (stack.isIn(SCTags.BANNER_COMPATIBLE.getTag())) renderBannerPatterns(stack, matrices, multiBufferSource, light, accessoryItemModel);
    }

    private void renderOverlayAndAdditions(LivingEntity entity, ItemStack stack, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light,
                                           BipedEntityModel<LivingEntity> model) {
        if (stack.getItem() instanceof SCAccessoryItem scAccessoryItem && scAccessoryItem.shouldNotRenderOnHeadInFirstPerson() &&
                entity == MinecraftClient.getInstance().player && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            return;
        }
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


    @Override
    public <M extends LivingEntity> void align(ItemStack itemStack, SlotReference slotReference, EntityModel<M> entityModel, MatrixStack matrixStack) {
    }
}
