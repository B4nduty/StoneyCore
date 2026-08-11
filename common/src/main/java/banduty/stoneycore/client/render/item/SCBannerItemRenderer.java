package banduty.stoneycore.client.render.item;

import banduty.stoneycore.mixin.ItemRendererAccessor;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class SCBannerItemRenderer extends BlockEntityWithoutLevelRenderer {

    public SCBannerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ResourceLocation baseLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());

        // 1. Fetch base model
        BakedModel baseModel = ClientPlatform.getIclientPlatformHelper().getModel(
                ResourceLocation.fromNamespaceAndPath(baseLocation.getNamespace(), "item/" + baseLocation.getPath() + "_base")
        );

        if (baseModel == null) {
            baseModel = itemRenderer.getModel(stack, null, null, 0);
        }

        ItemRendererAccessor accessor = (ItemRendererAccessor) itemRenderer;
        boolean isGui = (displayContext == ItemDisplayContext.GUI);

        // 2. Render Base Item Model
        poseStack.pushPose();

        Lighting.setupFor3DItems();

        // Render base model
        accessor.invokeRenderModelLists(
                baseModel,
                stack,
                isGui ? LightTexture.FULL_BRIGHT : packedLight,
                packedOverlay,
                poseStack,
                bufferSource.getBuffer(RenderType.cutout())
        );

        if (bufferSource instanceof MultiBufferSource.BufferSource impl) {
            impl.endBatch();
        }

        // 3. Render Banner Pattern Layers
        BannerPatternLayers patterns = stack.get(DataComponents.BANNER_PATTERNS);

        if (patterns != null && !patterns.layers().isEmpty()) {
            int layerIndex = 0;

            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);

            for (BannerPatternLayers.Layer layer : patterns.layers()) {
                Holder<BannerPattern> patternHolder = layer.pattern();
                DyeColor color = layer.color();

                String patternPath = patternHolder.unwrapKey()
                        .map(key -> key.location().getPath())
                        .orElse("base");

                String patternNamespace = patternHolder.unwrapKey()
                        .map(key -> key.location().getNamespace())
                        .orElse("minecraft");

                ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                        baseLocation.getNamespace(),
                        "textures/item/" + baseLocation.getPath() + "/" + patternNamespace + "/" + patternPath + ".png"
                );

                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(textureLocation));
                int iColor = color.getTextureDiffuseColor();

                float zOffset = (1F / 32F) + 0.001F * layerIndex;

                renderQuadCentered(poseStack, vertexConsumer, iColor, isGui ? LightTexture.FULL_BRIGHT : packedLight, packedOverlay, zOffset);

                if (bufferSource instanceof MultiBufferSource.BufferSource impl) {
                    impl.endBatch();
                }

                layerIndex++;
            }
            poseStack.popPose();
        }

        Lighting.setupFor3DItems();

        poseStack.popPose();
    }

    private void renderQuadCentered(PoseStack poseStack, VertexConsumer consumer, int color, int light, int overlay, float zOffset) {
        PoseStack.Pose pose = poseStack.last();

        // Front Face
        consumer.addVertex(pose, -0.5F, -0.5F, -zOffset)
                .setColor(color)
                .setUv(0.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 0.5F, -0.5F, -zOffset)
                .setColor(color)
                .setUv(1.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 0.5F, 0.5F, -zOffset)
                .setColor(color)
                .setUv(1.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, -0.5F, 0.5F, -zOffset)
                .setColor(color)
                .setUv(0.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        // Back Face
        consumer.addVertex(pose, 0.5F, -0.5F, zOffset)
                .setColor(color)
                .setUv(1.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, -0.5F, -0.5F, zOffset)
                .setColor(color)
                .setUv(0.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, -0.5F, 0.5F, zOffset)
                .setColor(color)
                .setUv(0.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, 0.5F, 0.5F, zOffset)
                .setColor(color)
                .setUv(1.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}