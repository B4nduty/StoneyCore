package banduty.stoneycore.client.render.item;

import banduty.stoneycore.platform.ClientPlatform;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import static banduty.stoneycore.StoneyCore.LOG;

public class SCBannerItemRenderer extends BlockEntityWithoutLevelRenderer {

    public SCBannerItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        LivingEntity entity = minecraft.cameraEntity instanceof LivingEntity livingEntity
                ? livingEntity
                : null;

        if (displayContext == ItemDisplayContext.GUI) {
            packedLight = LightTexture.FULL_BRIGHT;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        ResourceLocation baseModelLocation = ResourceLocation.fromNamespaceAndPath(
                itemId.getNamespace(),
                "item/" + itemId.getPath() + "_base"
        );

        BakedModel baseModel = ClientPlatform.getIclientPlatformHelper()
                .getModel(baseModelLocation);

        if (baseModel == null) {
            LOG.warn("Missing or unregistered base model: {}", baseModelLocation);
        } else {
            BakedModel resolvedBase = baseModel.getOverrides().resolve(
                    baseModel,
                    stack,
                    minecraft.level,
                    entity,
                    0
            );

            // ---- BASE TINT ----
            // Uses DYED_COLOR if present, otherwise white (no tint).
            int baseTint = 0xFFFFFFFF;
            DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
            if (dyed != null) {
                baseTint = 0xFF000000 | (dyed.rgb() & 0xFFFFFF);
            }

            renderModel(
                    itemRenderer,
                    stack,
                    displayContext,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay,
                    resolvedBase != null ? resolvedBase : baseModel,
                    baseTint
            );
        }

        BannerPatternLayers patterns = stack.get(DataComponents.BANNER_PATTERNS);
        if (patterns != null) {
            int layerIndex = 0;

            for (BannerPatternLayers.Layer layer : patterns.layers()) {
                Holder<BannerPattern> pattern = layer.pattern();

                // ---- PATTERN TINT uses layer.color() ----
                DyeColor color = layer.color();
                int patternTint = 0xFF000000 | color.getTextureDiffuseColor();

                ResourceLocation patternId = pattern.unwrapKey()
                        .map(key -> key.location())
                        .orElse(null);

                if (patternId == null) {
                    layerIndex++;
                    continue;
                }

                ResourceLocation patternModelId = itemId.withPath(path ->
                        "item/" + path + "/" + patternId.getNamespace() + "/" + patternId.getPath());

                BakedModel patternModel = ClientPlatform.getIclientPlatformHelper()
                        .getModel(patternModelId);

                if (patternModel == null) {
                    layerIndex++;
                    continue;
                }

                patternModel = patternModel.getOverrides().resolve(
                        patternModel,
                        stack,
                        minecraft.level,
                        entity,
                        0
                );
                float scale = 1 + 0.00005F * (layerIndex + 1);
                float offset = 0.00005F * (layerIndex + 1);
                boolean thirdPerson = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                // Front side
                poseStack.pushPose();
                if (thirdPerson || displayContext == ItemDisplayContext.GROUND)
                    poseStack.translate(0F, 0.0F, offset);
                else {
                    poseStack.translate(-offset, 0, 0);
                }
                poseStack.scale(scale, scale, scale);
                renderModel(
                        itemRenderer,
                        stack,
                        displayContext,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay,
                        patternModel,
                        patternTint
                );
                poseStack.popPose();

                // Back side
                poseStack.pushPose();
                if (thirdPerson || displayContext == ItemDisplayContext.GROUND)
                    poseStack.translate(0F, 0.0F, -offset);
                else {
                    poseStack.translate(offset, 0, 0);
                }
                poseStack.scale(scale, scale, scale);
                renderModel(
                        itemRenderer,
                        stack,
                        displayContext,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay,
                        patternModel,
                        patternTint
                );
                poseStack.popPose();

                layerIndex++;
            }
        }

        if (displayContext == ItemDisplayContext.GUI) {
            if (bufferSource instanceof MultiBufferSource.BufferSource buffer) {
                buffer.endBatch();
            }
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
    }


    private void renderModel(
            ItemRenderer itemRenderer,
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            BakedModel model,
            int argbTint
    ) {
        MultiBufferSource tintedSource = (argbTint == 0xFFFFFFFF)
                ? bufferSource
                : new TintedBufferSource(bufferSource, argbTint);

        poseStack.pushPose();
        try {
            if (displayContext == ItemDisplayContext.GUI) {
                Lighting.setupForFlatItems();
            }

            itemRenderer.render(
                    stack,
                    displayContext,
                    false,
                    poseStack,
                    tintedSource,
                    packedLight,
                    packedOverlay,
                    model
            );
        } finally {
            poseStack.popPose();
        }
    }


    private static final class TintedBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final float r, g, b, a;

        TintedBufferSource(MultiBufferSource delegate, int argb) {
            this.delegate = delegate;
            this.a = ((argb >> 24) & 0xFF) / 255f;
            this.r = ((argb >> 16) & 0xFF) / 255f;
            this.g = ((argb >> 8) & 0xFF) / 255f;
            this.b = (argb & 0xFF) / 255f;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return new TintedVertexConsumer(delegate.getBuffer(type), r, g, b, a);
        }
    }

    private static final class TintedVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float r, g, b, a;

        TintedVertexConsumer(VertexConsumer delegate, float r, float g, float b, float a) {
            this.delegate = delegate;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(
                    (int) (red * r),
                    (int) (green * g),
                    (int) (blue * b),
                    (int) (alpha * a)
            );
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}