package banduty.stoneycore.client;

import banduty.stoneycore.event.custom.RenderOverlayAndAdditionsEvents;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.DyeUtil;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SCAccessoryItemRenderer implements SimpleAccessoryRenderer {
    private static final RenderStateShard.ShaderStateShard RENDER_TYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentEmissiveShader);
    private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderStateShard.WriteMaskStateShard COLOR_MASK = new RenderStateShard.WriteMaskStateShard(true, true);
    private static final Function<ResourceLocation, RenderType> RENDER_TYPE_FUNCTION = Util.memoize(texture -> {
        RenderStateShard.EmptyTextureStateShard textureState = new RenderStateShard.TextureStateShard(texture, false, false);
        return RenderType.create("sc_glowing_layer", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDER_TYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                        .setTextureState(textureState)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_MASK)
                        .createCompositeState(false));
    });

    public SCAccessoryItemRenderer() {}

    @Override
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack poseStack, EntityModel<M> model, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(stack.getItem() instanceof SCAccessoryItem scAccessoryItem)) return;
        Optional<HumanoidModel<LivingEntity>> optionalModel = scAccessoryItem.getModels(stack).base();
        if (NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) optionalModel = scAccessoryItem.getModels(stack).visorOpen();
        if (optionalModel.isEmpty()) return;
        HumanoidModel<LivingEntity> accessoryModel = optionalModel.get();

        if (model instanceof HumanoidModel humanoidModel) humanoidModel.copyPropertiesTo(accessoryModel);

        if (scAccessoryItem.getRenderSettings(stack).customAngles()) accessoryModel.setupAnim(reference.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        VertexConsumer baseConsumer = multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(scAccessoryItem.getTexturePath(stack)));
        float[] color = DyeUtil.getFloatDyeColor(stack);

        accessoryModel.renderToBuffer(poseStack, baseConsumer, light, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1.0F);

        renderEmissiveTexture(scAccessoryItem, stack, poseStack, multiBufferSource, accessoryModel, color);

        renderOverlayAndAdditions(reference.entity(), stack, poseStack, multiBufferSource, light, accessoryModel);
        if (stack.is(SCTags.BANNER_COMPATIBLE.getTag())) renderBannerPatterns(stack, poseStack, multiBufferSource, light, accessoryModel);
    }

    private void renderEmissiveTexture(SCAccessoryItem scAccessoryItem, ItemStack stack, PoseStack poseStack,
                                       MultiBufferSource multiBufferSource, HumanoidModel<LivingEntity> model, float[] color) {
        if (scAccessoryItem.getEmissiveTexturePath(stack).isPresent()) {
            Optional<ResourceLocation> emissiveTexture = scAccessoryItem.getEmissiveTexturePath(stack);
            if (emissiveTexture.isPresent()) {
                VertexConsumer emissiveConsumer = multiBufferSource.getBuffer(RENDER_TYPE_FUNCTION.apply(emissiveTexture.get()));
                model.renderToBuffer(poseStack, emissiveConsumer, LightTexture.FULL_SKY, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1.0F);
            }
        }
    }

    private void renderOverlayAndAdditions(LivingEntity entity, ItemStack stack, PoseStack poseStack,
                                           MultiBufferSource multiBufferSource, int light,
                                           HumanoidModel<LivingEntity> model) {
        MinecraftForge.EVENT_BUS.post(new RenderOverlayAndAdditionsEvents(entity, stack, poseStack, multiBufferSource, light, model));
    }

    private void renderBannerPatterns(ItemStack stack, PoseStack poseStack,
                                      MultiBufferSource multiBufferSource, int light,
                                      HumanoidModel<LivingEntity> model) {
        List<Tuple<ResourceLocation, DyeColor>> bannerPatterns = PatternHelper.getBannerPatterns(stack);
        if (!bannerPatterns.isEmpty()) {
            for (Tuple<ResourceLocation, DyeColor> patternPair : bannerPatterns) {
                ResourceLocation pattern = patternPair.getA();
                DyeColor dyeColor = patternPair.getB();

                float[] rgb = dyeColor.getTextureDiffuseColors();

                VertexConsumer patternConsumer = multiBufferSource.getBuffer(ClientPlatform.getSCRenderTypeHelper().getArmorTranslucentNoCull(pattern));
                model.renderToBuffer(poseStack, patternConsumer, light, OverlayTexture.NO_OVERLAY,
                        rgb[0], rgb[1], rgb[2], 1.0F);
            }
        }
    }

    @Override
    public <M extends LivingEntity> void align(ItemStack itemStack, SlotReference slotReference, EntityModel<M> entityModel, PoseStack poseStack) {

    }
}
