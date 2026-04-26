package banduty.stoneycore.mixin;

import banduty.stoneycore.client.overlay.StoneyCoreOverlayRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    private static final StoneyCoreOverlayRenderer OVERLAY_RENDERER = new StoneyCoreOverlayRenderer();

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor3DItems()V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderSCThings(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        GameRenderer gameRenderer = (GameRenderer) (Object) this;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        RenderBuffers buffers = ((GameRendererAccessor) gameRenderer).getBuffers();
        GuiGraphics guiGraphics = new GuiGraphics(gameRenderer.getMinecraft(), buffers.bufferSource());

        Window window = gameRenderer.getMinecraft().getWindow();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), (float)((double)window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, 21000.0F);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.translate(0.0F, 0.0F, -11000.0F);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();

        OVERLAY_RENDERER.render(guiGraphics, tickDelta);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        guiGraphics.flush();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
