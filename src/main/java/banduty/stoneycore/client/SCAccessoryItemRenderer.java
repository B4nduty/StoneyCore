package banduty.stoneycore.client;

import banduty.stoneycore.event.custom.RenderOverlayAndAdditionsEvents;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import banduty.stoneycore.util.render.SCRenderLayers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class SCAccessoryItemRenderer implements SimpleAccessoryRenderer {
    private static final RenderPhase.ShaderProgram RENDER_TYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER = new RenderPhase.ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentEmissiveProgram);
    private static final RenderPhase.Transparency TRANSLUCENT_TRANSPARENCY = new RenderPhase.Transparency("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderPhase.WriteMaskState COLOR_MASK = new RenderPhase.WriteMaskState(true, true);
    private static final Function<Identifier, RenderLayer> RENDER_TYPE_FUNCTION = Util.memoize(texture -> {
        RenderPhase.TextureBase textureState = new RenderPhase.Texture(texture, false, false);
        return RenderLayer.of("sc_glowing_layer", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RENDER_TYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                        .texture(textureState)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .writeMaskState(COLOR_MASK)
                        .build(false));
    });

    public SCAccessoryItemRenderer() {}

    @Override
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, MatrixStack matrices, EntityModel<M> model, VertexConsumerProvider multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(stack.getItem() instanceof SCAccessoryItem scAccessoryItem)) return;
        Optional<BipedEntityModel<LivingEntity>> optionalModel = scAccessoryItem.getModels(stack).base();
        if (NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) optionalModel = scAccessoryItem.getModels(stack).visorOpen();
        if (optionalModel.isEmpty()) return;
        BipedEntityModel<LivingEntity> accessoryModel = optionalModel.get();

        if (model instanceof BipedEntityModel bipedEntityModel) bipedEntityModel.copyBipedStateTo(accessoryModel);

        if (scAccessoryItem.getRenderSettings(stack).customAngles()) accessoryModel.setAngles(reference.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        VertexConsumer baseConsumer = multiBufferSource.getBuffer(RenderLayer.getArmorCutoutNoCull(scAccessoryItem.getTexturePath(stack)));
        float[] color = DyeUtil.getFloatDyeColor(stack);

        accessoryModel.render(matrices, baseConsumer, light, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);

        renderEmissiveTexture(scAccessoryItem, stack, matrices, multiBufferSource, accessoryModel, color);

        renderOverlayAndAdditions(reference.entity(), stack, matrices, multiBufferSource, light, accessoryModel);
        if (stack.isIn(SCTags.BANNER_COMPATIBLE.getTag())) renderBannerPatterns(stack, matrices, multiBufferSource, light, accessoryModel);
    }

    private void renderEmissiveTexture(SCAccessoryItem scAccessoryItem, ItemStack stack, MatrixStack matrices,
                                       VertexConsumerProvider multiBufferSource, BipedEntityModel<LivingEntity> model, float[] color) {
        if (scAccessoryItem.getEmissiveTexturePath(stack).isPresent()) {
            Optional<Identifier> emissiveTexture = scAccessoryItem.getEmissiveTexturePath(stack);
            if (emissiveTexture.isPresent()) {
                VertexConsumer emissiveConsumer = multiBufferSource.getBuffer(RENDER_TYPE_FUNCTION.apply(emissiveTexture.get()));
                model.render(matrices, emissiveConsumer, LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, color[0], color[1], color[2], 1.0F);
            }
        }
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


    @Override
    public <M extends LivingEntity> void align(ItemStack itemStack, SlotReference slotReference, EntityModel<M> entityModel, MatrixStack matrixStack) {
    }
}
