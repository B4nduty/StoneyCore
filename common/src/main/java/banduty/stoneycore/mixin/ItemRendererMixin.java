package banduty.stoneycore.mixin;

import banduty.stoneycore.items.custom.manuscript.Manuscript;
import banduty.stoneycore.util.data.itemdata.SCTags;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Shadow
    public abstract ItemModelShaper getItemModelShaper();

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "HEAD"),
            argsOnly = true
    )
    public BakedModel stoneycore$renderItem(BakedModel bakedModel, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) ItemDisplayContext renderMode) {
        if (stack.getItem() instanceof Manuscript && Manuscript.hasTargetStack(stack)) {
            String modelPath = "manuscript_" + Manuscript.getTargetItemPath(stack);
            ModelResourceLocation manuscriptModelId = ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath(Manuscript.getTargetItemNamespace(stack), modelPath));
            return getItemModelShaper().getModelManager().getModel(manuscriptModelId);
        }

        if (stack.is(SCTags.GEO_2D_ITEMS.getTag())) {
            if (renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                ModelResourceLocation flatModelId = ModelResourceLocation.inventory(
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_icon"));
                return getItemModelShaper().getModelManager().getModel(flatModelId);
            }
        }

        if (stack.is(SCTags.WEAPONS_3D.getTag())) {
            if (renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                ModelResourceLocation model3dId = ModelResourceLocation.inventory(
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath()));
                BakedModel base3dModel = this.itemModelShaper.getModelManager().getModel(model3dId);

                BakedModel overriddenModel = base3dModel.getOverrides().resolve(base3dModel, stack, null, null, 0);
                return Objects.requireNonNullElse(overriddenModel, base3dModel);

            }
        }

        return bakedModel;
    }

    @ModifyVariable(
            method = "getModel",
            at = @At(value = "STORE"),
            ordinal = 1
    )
    public BakedModel getHeldItemModelMixin(BakedModel bakedModel, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) LivingEntity entity) {
        if (stack.is(SCTags.WEAPONS_3D.getTag())) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            ModelResourceLocation model3dId = ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_3d"));
            BakedModel base3dModel = this.itemModelShaper.getModelManager().getModel(model3dId);

            ClientLevel level = entity != null ? (ClientLevel) entity.level() : null;
            BakedModel overriddenModel = base3dModel.getOverrides().resolve(base3dModel, stack, level, entity, 0);
            return Objects.requireNonNullElse(overriddenModel, base3dModel);

        }

        return bakedModel;
    }

    @Inject(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void stoneycore$renderLayeredBannerPatterns(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (itemStack.isEmpty()) return;
        if (!itemStack.is(SCTags.BANNER_COMPATIBLE.getTag())) return;

        // Fetch patterns and dyed color
        BannerPatternLayers patterns = itemStack.get(DataComponents.BANNER_PATTERNS);
        DyedItemColor dyedColor = itemStack.get(DataComponents.DYED_COLOR);

        // If there are no patterns AND no dyed color, let vanilla handle it
        if ((patterns == null || patterns.layers().isEmpty()) && dyedColor == null) {
            return;
        }

        // If there's a dyed color but no patterns, we still need to render with the tint
        boolean hasPatterns = patterns != null && !patterns.layers().isEmpty();

        poseStack.pushPose();

        // Determine base model variant
        BakedModel baseModel = model;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());

        if (itemStack.is(SCTags.GEO_2D_ITEMS.getTag()) && (displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND)) {
            baseModel = itemModelShaper.getModelManager().getModel(ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_icon")));
        } else if (itemStack.is(SCTags.WEAPONS_3D.getTag()) && !(displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED)) {
            baseModel = itemModelShaper.getModelManager().getModel(ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_3d")));
        }

        // Apply positional orientations
        baseModel.getTransforms().getTransform(displayContext).apply(leftHand, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // Setup rendering consumers
        RenderType rendertype = ItemBlockRenderTypes.getRenderType(itemStack, true);
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(bufferSource, rendertype, true, itemStack.hasFoil());

        // Render the Base Layer WITH the dyed color
        float[] baseRGB = new float[]{1.0F, 1.0F, 1.0F};
        if (dyedColor != null) {
            baseRGB = stoneycore$unpackIntColor(dyedColor.rgb());
        }
        stoneycore$renderBakedQuadsDirect(baseModel, combinedLight, combinedOverlay, poseStack, vertexconsumer, baseRGB);

        // If there are patterns, render them on top
        if (hasPatterns) {
            ModelManager modelManager = this.itemModelShaper.getModelManager();

            for (BannerPatternLayers.Layer layer : patterns.layers()) {
                ResourceLocation patternId = layer.pattern().unwrapKey().map(ResourceKey::location).orElse(null);
                if (patternId == null) continue;

                // Isolate short-code string name matching your ModelBakeryMixin setup (e.g. "ss", "tl")
                String patternName = patternId.getPath();
                if (patternName.contains("/")) {
                    patternName = patternName.substring(patternName.lastIndexOf('/') + 1);
                }

                // Path format mapping: "namespace:item_path/pattern_shortname"
                String compositePath = itemId.getPath() + "/" + patternName;
                ModelResourceLocation layerModelId = ModelResourceLocation.inventory(
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), compositePath)
                );

                BakedModel layerModel = modelManager.getModel(layerModelId);

                if (layerModel != modelManager.getMissingModel()) {
                    float[] patternRGB = stoneycore$unpackIntColor(layer.color().getTextureDiffuseColor());

                    // Get fresh foil consumer layer to stack cleanly
                    VertexConsumer patternConsumer = ItemRenderer.getFoilBufferDirect(bufferSource, rendertype, true, itemStack.hasFoil());
                    stoneycore$renderBakedQuadsDirect(layerModel, combinedLight, combinedOverlay, poseStack, patternConsumer, patternRGB);
                }
            }
        }

        poseStack.popPose();

        // Cancel standard execution to use our custom rendering
        ci.cancel();
    }

    @Unique
    private float[] stoneycore$unpackIntColor(int packedColor) {
        float r = (float) (packedColor >> 16 & 255) / 255.0F;
        float g = (float) (packedColor >> 8 & 255) / 255.0F;
        float b = (float) (packedColor & 255) / 255.0F;
        return new float[]{r, g, b};
    }

    @Unique
    private void stoneycore$renderBakedQuadsDirect(BakedModel model, int light, int overlay, PoseStack poseStack, VertexConsumer vertices, float[] color) {
        RandomSource random = RandomSource.create();
        long seed = 42L;

        // Write directional face quads
        for (Direction direction : Direction.values()) {
            random.setSeed(seed);
            stoneycore$buildQuadBuffer(poseStack, vertices, model.getQuads(null, direction, random), light, overlay, color);
        }

        // Write non-directional face quads
        random.setSeed(seed);
        stoneycore$buildQuadBuffer(poseStack, vertices, model.getQuads(null, null, random), light, overlay, color);
    }

    @Unique
    private void stoneycore$buildQuadBuffer(PoseStack poseStack, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, float[] color) {
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad bakedQuad : quads) {
            float r = color[0];
            float g = color[1];
            float b = color[2];
            vertices.putBulkData(pose, bakedQuad, r, g, b, 1, light, overlay);
        }
    }
}