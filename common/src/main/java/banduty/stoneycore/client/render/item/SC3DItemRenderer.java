package banduty.stoneycore.client.render.item;

import banduty.stoneycore.platform.ClientPlatform;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SC3DItemRenderer extends BlockEntityWithoutLevelRenderer {

    public SC3DItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        String modelSuffix;

        if (displayContext == ItemDisplayContext.GUI
                || displayContext == ItemDisplayContext.GROUND
                || displayContext == ItemDisplayContext.FIXED) {
            modelSuffix = "_gui";
        } else {
            modelSuffix = "_3d";
        }

        ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath(
                itemId.getNamespace(),
                "item/" + itemId.getPath() + modelSuffix
        );

        BakedModel baseModel = ClientPlatform.getIclientPlatformHelper()
                .getModel(modelLocation);

        /*
         * Get the entity currently rendering/holding the item.
         *
         * This is important for predicates such as:
         * - pull
         * - pulling
         * - charged
         * - any predicate that uses LivingEntity
         */
        LivingEntity entity = null;

        if (minecraft.cameraEntity instanceof LivingEntity livingEntity) {
            entity = livingEntity;
        }

        /*
         * Resolve ItemOverrides.
         *
         * This is the part that was missing from the previous renderer.
         */
        BakedModel resolvedModel = baseModel.getOverrides().resolve(
                baseModel,
                stack,
                minecraft.level,
                entity,
                0
        );

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.5D, 0.5D);

        if (displayContext == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();

            itemRenderer.render(
                    stack,
                    displayContext,
                    false,
                    poseStack,
                    bufferSource,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay,
                    resolvedModel
            );

            if (bufferSource instanceof MultiBufferSource.BufferSource buffer) {
                buffer.endBatch();
            }

            Lighting.setupFor3DItems();
        } else {
            itemRenderer.render(
                    stack,
                    displayContext,
                    false,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay,
                    resolvedModel
            );
        }

        poseStack.popPose();
    }
}