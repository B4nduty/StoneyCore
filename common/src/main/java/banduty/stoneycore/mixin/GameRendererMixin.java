package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Surely there's a better way to do this, but it works, so I don't care
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

        if (!tick || gameRenderer.getMinecraft().level == null) return;

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

        // Just move outside the code, because I know I will somehow make this thing crash the NASA
        stoneyCore$dontMessItNow(guiGraphics, tickDelta);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        guiGraphics.flush();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Unique
    private void stoneyCore$dontMessItNow(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || player.isSpectator()) return;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (ItemStack itemStack : Services.PLATFORM.getEquippedAccessories(player)) {
            ResourceLocation visorId = AccessoriesDefinitionsStorage.getData(itemStack).visoredHelmet();

            if (!NBTDataHelper.get(itemStack, INBTKeys.VISOR_OPEN, false) && !(visorId.getPath().isEmpty() || visorId.getPath().equals("empty")) && StoneyCore.getConfig().visualOptions().getVisoredHelmet()) {

                String namespace = visorId.getNamespace();
                if (visorId.getNamespace().isEmpty()) namespace = "stoneycore";

                ResourceLocation visorTexture = new ResourceLocation(namespace, "textures/overlay/visor/" + visorId.getPath() + ".png");

                RenderSystem.setShaderTexture(0, visorTexture);
                guiGraphics.setColor(1f, 1f, 1f, player.isCreative() ? StoneyCore.getConfig().visualOptions().getVisoredHelmetAlpha() : 1f);
                guiGraphics.blit(visorTexture, 0, 0, 0, 0, width, height, width, height);
                break;
            }
        }
        guiGraphics.setColor(1f, 1f, 1f, 1f);

        double stamina = StaminaData.getStamina(player);
        double secondLevel = player.getAttributeBaseValue(Services.ATTRIBUTES.getMaxStamina()) * 0.15d;

        if (!player.isCreative() && StaminaData.isStaminaBlocked((IEntityDataSaver) player) && StoneyCore.getConfig().visualOptions().getLowStaminaIndicator()) {
            double staminaPercentage = stamina / secondLevel;
            if (StoneyCore.getConfig().combatOptions().getRealisticCombat()) {
                if (noiseTextures == null) stoneyCore$initNoiseTextures();
                stoneyCore$renderBlurEffect(guiGraphics, width, height, staminaPercentage);
            } else {
                int opacity = (int)((Math.max(0, 0.4f - staminaPercentage) * 255));
                int green = StaminaData.isStaminaBlocked((IEntityDataSaver) player) ? 0 : (int) (stamina / secondLevel);
                int gradientColorEnd = opacity << 24 | green | 0x00FF0000;

                guiGraphics.fillGradient(0, 0, width, height, 0x00FFFFFF, gradientColorEnd);
            }
        }

        stoneyCore$renderVisorToggleProgress(guiGraphics, tickDelta);

        StoneyCoreClient.LAND_TITLE_RENDERER.render(guiGraphics);
    }

    @Unique
    private ResourceLocation[] noiseTextures;
    @Unique
    private int currentNoiseTexture = 0;
    @Unique
    private int currentNoiseTextureTime = 0;

    @Unique
    private void stoneyCore$initNoiseTextures() {
        noiseTextures = new ResourceLocation[12];
        for (int i = 0; i < noiseTextures.length; i++) {
            noiseTextures[i] = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/noise/noise_" + i + ".png");
        }
    }

    @Unique
    private void stoneyCore$renderBlurEffect(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        stoneyCore$renderBlur(guiGraphics, width, height, staminaPercentage);
        stoneyCore$renderTunnelVision(guiGraphics, width, height, staminaPercentage);
        if (StoneyCore.getConfig().visualOptions().getNoiseEffect()) stoneyCore$renderNoise(guiGraphics, width, height, staminaPercentage);
    }

    @Unique
    private void stoneyCore$renderBlur(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = guiGraphics.pose().last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(matrix, 0, 0, 0).endVertex();
        buffer.vertex(matrix, 0, height, 0).endVertex();
        buffer.vertex(matrix, width, height, 0).endVertex();
        buffer.vertex(matrix, width, 0, 0).endVertex();

        float blur = (float) (Math.max(0.1f, 1.0f - staminaPercentage) * 12.0f);

        ClientPlatform.getClientPlaformHelper().startBlurService(blur);

        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Unique
    private void stoneyCore$renderNoise(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        ResourceLocation noiseTexture = noiseTextures[currentNoiseTexture];
        float alpha = (float) (-0.001f * currentNoiseTextureTime * (currentNoiseTextureTime - 10) + Math.max(0, 1.0f - staminaPercentage) * 0.2f);
        if (currentNoiseTextureTime-- <= 0 && !Minecraft.getInstance().isPaused()) {
            currentNoiseTexture = (currentNoiseTexture + 1) % noiseTextures.length;
            currentNoiseTextureTime = 10;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, alpha);
        guiGraphics.blit(noiseTexture, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    @Unique
    private void stoneyCore$renderTunnelVision(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        int opacity = (int)((Math.max(0, 0.2f - staminaPercentage) * 255));
        int gradientColor = opacity << 24;

        int opacity2 = (int)((Math.max(0, 0.6f - staminaPercentage) * 255));
        int gradientColor2 = opacity2 << 24;

        guiGraphics.fillGradient(0, 0, width, height, gradientColor, gradientColor2);
    }

    @Unique
    private static final ResourceLocation VISOR_PROGRESS_BACKGROUND = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/visor_progress_background.png");
    @Unique
    private static final ResourceLocation VISOR_PROGRESS_BAR = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/visor_progress_bar.png");
    @Unique
    private float lastRenderedProgress = 0.0f;

    @Unique
    private void stoneyCore$renderVisorToggleProgress(GuiGraphics guiGraphics, float tickDelta) {
        if (StoneyCore.getConfig().combatOptions().getToggleVisorTime() == 0
                || !ClientPlatform.getKeyInputHelper().isTogglingVisor()
                || ClientPlatform.getKeyInputHelper().isVisorToggled()
                || ClientPlatform.getKeyInputHelper().toggleVisorTicks() <= 0.0f) {
            lastRenderedProgress = 0.0f;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int yOffset = 50;

        int bgWidth = 128;
        int bgHeight = 16;
        int barWidth = 124;
        int barHeight = 12;

        float targetProgress = ClientPlatform.getKeyInputHelper().toggleProgress();
        float interpolationSpeed = 20.0f;

        float smoothProgress = lastRenderedProgress + (targetProgress - lastRenderedProgress) * Math.min(1.0f, interpolationSpeed * tickDelta);
        lastRenderedProgress = smoothProgress;

        int progressWidth = (int) (barWidth * smoothProgress);

        int bgX = centerX - bgWidth / 2;
        int bgY = centerY + yOffset - bgHeight / 2;

        guiGraphics.blit(VISOR_PROGRESS_BACKGROUND, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        if (progressWidth > 0) {
            int barX = centerX - barWidth / 2;
            int barY = centerY + yOffset - barHeight / 2;
            guiGraphics.blit(VISOR_PROGRESS_BAR, barX, barY, 0, 0, progressWidth, barHeight, barWidth, barHeight);
        }
    }
}
