package banduty.stoneycore.client.overlay;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class StoneyCoreOverlayRenderer {

    private static final ResourceLocation VISOR_PROGRESS_BACKGROUND = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/visor_progress_background.png");
    private static final ResourceLocation VISOR_PROGRESS_BAR = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/visor_progress_bar.png");

    private ResourceLocation[] noiseTextures;
    private int currentNoiseTexture = 0;
    private int currentNoiseTextureTime = 0;
    private float lastRenderedProgress = 0.0f;

    public void render(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || player.isSpectator()) return;
        if (client.options.hideGui) return;
        if (!client.options.getCameraType().isFirstPerson() && !StoneyCore.getConfig().visualOptions().overlayThirdPerson())
            return;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        renderVisor(guiGraphics, player, width, height);
        renderStaminaEffects(guiGraphics, player, width, height);
        renderVisorToggleProgress(guiGraphics, tickDelta);
        StoneyCoreClient.LAND_TITLE_RENDERER.render(guiGraphics);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private void renderVisor(GuiGraphics guiGraphics, LocalPlayer player, int width, int height) {
        for (ItemStack itemStack : Services.PLATFORM.getEquippedAccessories(player)) {
            ResourceLocation visorId = AccessoriesDefinitionsStorage.getData(itemStack).visoredHelmet();

            if (!NBTDataHelper.get(itemStack, INBTKeys.VISOR_OPEN, false)
                    && !(visorId.getPath().isEmpty() || visorId.getPath().equals("empty"))
                    && StoneyCore.getConfig().visualOptions().getVisoredHelmet()) {

                String namespace = visorId.getNamespace();
                if (namespace.isEmpty()) namespace = "stoneycore";

                ResourceLocation visorTexture =
                        new ResourceLocation(namespace, "textures/overlay/visor/" + visorId.getPath() + ".png");

                RenderSystem.setShaderTexture(0, visorTexture);
                RenderSystem.texParameter(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_MIN_FILTER,
                        GL11.GL_LINEAR
                );
                RenderSystem.texParameter(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_MAG_FILTER,
                        GL11.GL_LINEAR
                );

                float alpha = player.isCreative()
                        ? StoneyCore.getConfig().visualOptions().getVisoredHelmetAlphaCreative()
                        : StoneyCore.getConfig().visualOptions().getVisoredHelmetAlphaSurvival();

                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                guiGraphics.blit(visorTexture, 0, 0, 0, 0, width, height, width, height);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                break;
            }
        }
    }

    private void renderStaminaEffects(GuiGraphics guiGraphics, LocalPlayer player, int width, int height) {
        double stamina = StaminaData.getStamina(player);
        double secondLevel = player.getAttributeBaseValue(Services.ATTRIBUTES.getMaxStamina()) * 0.15d;

        if (!player.isCreative()
                && StaminaData.isStaminaBlocked((IEntityDataSaver) player)
                && StoneyCore.getConfig().visualOptions().getLowStaminaIndicator()) {

            double staminaPercentage = stamina / secondLevel;

            if (StoneyCore.getConfig().combatOptions().getRealisticCombat()) {
                if (noiseTextures == null) initNoiseTextures();
                renderBlurEffect(guiGraphics, width, height, staminaPercentage);
            } else {
                int opacity = (int) ((Math.max(0, 0.4f - staminaPercentage) * 255));
                int green = StaminaData.isStaminaBlocked((IEntityDataSaver) player)
                        ? 0
                        : (int) (stamina / secondLevel);

                int gradientColorEnd = opacity << 24 | green | 0x00FF0000;
                guiGraphics.fillGradient(0, 0, width, height, 0x00FFFFFF, gradientColorEnd);
            }
        }
    }

    private void initNoiseTextures() {
        noiseTextures = new ResourceLocation[12];
        for (int i = 0; i < noiseTextures.length; i++) {
            noiseTextures[i] = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/noise/noise_" + i + ".png");
        }
    }

    private void renderBlurEffect(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        renderBlur(guiGraphics, width, height, staminaPercentage);
        renderTunnelVision(guiGraphics, width, height, staminaPercentage);
        if (StoneyCore.getConfig().visualOptions().getNoiseEffect())
            renderNoise(guiGraphics, width, height, staminaPercentage);
    }

    private void renderBlur(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        float blurStrength = (float) (Math.max(0.1f, 1.0f - staminaPercentage) * 0.4f);
        int alpha = (int) (blurStrength * 255);
        int color = alpha << 24;
        guiGraphics.fill(0, 0, width, height, color);

        try {
            ClientPlatform.getClientPlaformHelper().startBlurService(blurStrength * 12.0f);
        } catch (Exception e) {
            // Fallback silently if blur fails
        }
    }

    private void renderNoise(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        if (noiseTextures == null) return;

        ResourceLocation noiseTexture = noiseTextures[currentNoiseTexture];
        float alpha = (float) (1 - Math.max(0, 1.0f - staminaPercentage)) * 0.2f;

        if (currentNoiseTextureTime-- <= 0 && !Minecraft.getInstance().isPaused()) {
            currentNoiseTexture = (currentNoiseTexture + 1) % noiseTextures.length;
            currentNoiseTextureTime = 10;
        }

        RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, alpha);
        guiGraphics.blit(noiseTexture, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderTunnelVision(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        int opacity = (int) ((Math.max(0, 0.2f - staminaPercentage) * 255));
        int gradientColor = opacity << 24;
        int opacity2 = (int) ((Math.max(0, 0.6f - staminaPercentage) * 255));
        int gradientColor2 = opacity2 << 24;
        guiGraphics.fillGradient(0, 0, width, height, gradientColor, gradientColor2);
    }

    private void renderVisorToggleProgress(GuiGraphics guiGraphics, float tickDelta) {
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