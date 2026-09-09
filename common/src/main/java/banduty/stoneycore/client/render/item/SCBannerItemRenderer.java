package banduty.stoneycore.client.render.item;

import banduty.stoneycore.mixin.ItemRendererAccessor;
import banduty.stoneycore.platform.ClientPlatform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        ResourceLocation baseModelLocation = ResourceLocation.fromNamespaceAndPath(
                itemId.getNamespace(),
                "item/" + itemId.getPath() + "_base"
        );

        BakedModel baseModel = ClientPlatform.getIclientPlatformHelper().getModel(baseModelLocation);

        if (baseModel == null) {
            baseModel = itemRenderer.getModel(stack, minecraft.level, null, 0);
        }

        BakedModel resolvedModel = baseModel.getOverrides().resolve(
                baseModel,
                stack,
                minecraft.level,
                null,
                0
        );

        if (resolvedModel == null) {
            resolvedModel = baseModel;
        }

        ItemRendererAccessor accessor = (ItemRendererAccessor) itemRenderer;

        poseStack.pushPose();

        boolean thirdPerson = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        double x = 0D;
        double y = 0D;
        double z = 0D;
        if (thirdPerson) { x = 0.25D; y = 0.25D; z = 0.25D; }
        if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) { x = 0.25D; y = 0.25D; z = 1.0D; }
        if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) { x = 0.5D; y = 0D; z = 0.25D; }
        poseStack.translate(x, y, z);

        resolvedModel.getTransforms().getTransform(displayContext).apply(!leftHand(displayContext), poseStack);

        accessor.invokeRenderModelLists(
                resolvedModel,
                stack,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource.getBuffer(RenderType.cutout())
        );

        BannerPatternLayers patterns = stack.get(DataComponents.BANNER_PATTERNS);
        if (patterns != null && !patterns.layers().isEmpty()) {
            renderPatterns(itemId, patterns, poseStack, bufferSource, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private boolean leftHand(ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    private void renderPatterns(ResourceLocation itemId,
                                BannerPatternLayers patterns,
                                PoseStack poseStack,
                                MultiBufferSource bufferSource,
                                int light,
                                int overlay) {

        poseStack.pushPose();

        /*
         * minecraft:item/generated uses 0..1 model coordinates.
         *
         * Its generated item plane is centered around Z = 0.5.
         *
         * Therefore the overlay must use the same coordinate system:
         *
         * X = 0..1
         * Y = 0..1
         * Z ~= 0.5
         *
         * Do NOT translate by 0.5 here.
         */
        int layerIndex = 0;

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
                    itemId.getNamespace(),
                    "textures/item/" + itemId.getPath() + "/" + patternNamespace + "/" + patternPath + ".png"
            );

            VertexConsumer consumer = bufferSource.getBuffer(
                    RenderType.entityCutoutNoCull(textureLocation)
            );

            int colorValue = color.getTextureDiffuseColor();

            /*
             * The generated model is approximately:
             *
             * front = Z 0.46875
             * back  = Z 0.53125
             *
             * Put the overlay just in front of those surfaces.
             */
            float zOffset = 0.001F + (0.0001F * layerIndex);

            renderQuad(
                    poseStack,
                    consumer,
                    colorValue,
                    light,
                    overlay,
                    zOffset
            );

            layerIndex++;
        }

        poseStack.popPose();
    }

    private void renderQuad(PoseStack poseStack,
                            VertexConsumer consumer,
                            int color,
                            int light,
                            int overlay,
                            float zOffset) {

        PoseStack.Pose pose = poseStack.last();

        /*
         * Front face.
         *
         * Generated item front surface:
         * Z ~= 0.46875
         */
        float frontZ = 0.46875F - zOffset;

        consumer.addVertex(pose, 0.0F, 0.0F, frontZ)
                .setColor(color)
                .setUv(0.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, 1.0F, 0.0F, frontZ)
                .setColor(color)
                .setUv(1.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, 1.0F, 1.0F, frontZ)
                .setColor(color)
                .setUv(1.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        consumer.addVertex(pose, 0.0F, 1.0F, frontZ)
                .setColor(color)
                .setUv(0.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);

        /*
         * Back face.
         *
         * Generated item back surface:
         * Z ~= 0.53125
         */
        float backZ = 0.53125F + zOffset;

        consumer.addVertex(pose, 1.0F, 0.0F, backZ)
                .setColor(color)
                .setUv(1.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 0.0F, 0.0F, backZ)
                .setColor(color)
                .setUv(0.0F, 1.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 0.0F, 1.0F, backZ)
                .setColor(color)
                .setUv(0.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);

        consumer.addVertex(pose, 1.0F, 1.0F, backZ)
                .setColor(color)
                .setUv(1.0F, 0.0F)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}