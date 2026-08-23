package banduty.stoneycore.mixin;

import banduty.stoneycore.items.client.SCIconRendererProvider;
import banduty.stoneycore.platform.ClientPlatform;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Mixin(GeoItemRenderer.class)
public class GeoItemRendererMixin {
    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void stoneycore$renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (!(stack.getItem() instanceof SCIconRendererProvider)) return;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (displayContext == ItemDisplayContext.GUI) {
            BakedModel guiModel = ClientPlatform.getIclientPlatformHelper()
                    .getModel(ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath() + "_icon"));

            poseStack.pushPose();

            poseStack.translate(0.5D, 0.5D, 0.5D);

            // 1. Setup GUI Flat Lighting (disables 3D directional light shading)
            Lighting.setupForFlatItems();

            // 2. Render the GUI model flat
            itemRenderer.render(stack, displayContext, false, poseStack, bufferSource, LightTexture.FULL_BRIGHT, packedOverlay, guiModel);

            // 3. Flush the render buffer while flat lighting is active
            if (bufferSource instanceof MultiBufferSource.BufferSource impl) {
                impl.endBatch();
            }

            // 4. Restore standard 3D inventory lighting setup for subsequent models
            Lighting.setupFor3DItems();

            poseStack.popPose();
            ci.cancel();
        } else if (displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED) {
            poseStack.pushPose();
            BakedModel guiModel = ClientPlatform.getIclientPlatformHelper()
                    .getModel(ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath() + "_icon"));

            poseStack.translate(0.5D, 0.5D, 0.5D);

            itemRenderer.render(stack, displayContext, false, poseStack, bufferSource, packedLight, packedOverlay, guiModel);

            poseStack.popPose();
            ci.cancel();
        }
    }
}
