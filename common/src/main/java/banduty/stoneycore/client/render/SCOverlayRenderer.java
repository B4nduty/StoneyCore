package banduty.stoneycore.client.render;

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

public class SCOverlayRenderer {

    private static ResourceLocation[] noiseTextures;
    private static int currentNoiseTexture = 0;
    private static int currentNoiseTextureTime = 0;
    private static float lastRenderedProgress = 0.0f;

    private static final ResourceLocation VISOR_PROGRESS_BACKGROUND =
            new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/visor_progress_background.png");
    private static final ResourceLocation VISOR_PROGRESS_BAR =
            new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/visor_progress_bar.png");

    public static void render(GuiGraphics gui, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || player.isSpectator()) return;
        if (client.options.hideGui) return;

        int width = gui.guiWidth();
        int height = gui.guiHeight();

        beginOverlay();

        renderVisor(gui, player, width, height, client);
        renderStamina(gui, player, width, height);
        renderVisorToggleProgress(gui, tickDelta, client);
        StoneyCoreClient.LAND_TITLE_RENDERER.render(gui);

        endOverlay();
    }

    private static void beginOverlay() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void endOverlay() {
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderVisor(GuiGraphics gui, LocalPlayer player, int width, int height, Minecraft client) {

        if (ClientPlatform.getKeyInputHelper().isHidingVisor()) return;
        if (!client.options.getCameraType().isFirstPerson()
                && !StoneyCore.getConfig().visualOptions().overlayThirdPerson()) return;
        for (ItemStack stack : Services.PLATFORM.getEquippedAccessories(player)) {
            ResourceLocation visorId = AccessoriesDefinitionsStorage.getData(stack).visoredHelmet();

            if (!NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)
                    && !(visorId.getPath().isEmpty() || visorId.getPath().equals("empty"))
                    && StoneyCore.getConfig().visualOptions().getVisoredHelmet()) {

                String namespace = visorId.getNamespace().isEmpty() ? "stoneycore" : visorId.getNamespace();

                ResourceLocation texture =
                        new ResourceLocation(namespace, "textures/overlay/visor/" + visorId.getPath() + ".png");

                RenderSystem.setShaderTexture(0, texture);

                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                gui.setColor(
                        1f, 1f, 1f,
                        player.isCreative()
                                ? StoneyCore.getConfig().visualOptions().getVisoredHelmetAlphaCreative()
                                : StoneyCore.getConfig().visualOptions().getVisoredHelmetAlphaSurvival()
                );

                gui.blit(texture, 0, 0, 0, 0, width, height, width, height);
                break;
            }
        }

        gui.setColor(1f, 1f, 1f, 1f);
    }

    private static void renderStamina(GuiGraphics gui, LocalPlayer player, int width, int height) {
        if (player.isCreative()) return;
        if (!StaminaData.isStaminaBlocked((IEntityDataSaver) player)) return;

        double stamina = StaminaData.getStamina(player);
        double max = player.getAttributeBaseValue(Services.ATTRIBUTES.getMaxStamina()) * 0.15d;

        double percent = stamina / max;

        if (StoneyCore.getConfig().combatOptions().getRealisticCombat()) {
            if (noiseTextures == null) initNoise();
            renderTunnel(gui, width, height, percent);
            if (StoneyCore.getConfig().visualOptions().getNoiseEffect())
                renderNoise(gui, width, height, percent);
        } else {
            int opacity = (int) ((Math.max(0, 0.4f - percent) * 255));
            int color = opacity << 24 | 0x00FF0000;
            gui.fillGradient(0, 0, width, height, 0x00FFFFFF, color);
        }
    }

    private static void initNoise() {
        noiseTextures = new ResourceLocation[12];
        for (int i = 0; i < 12; i++) {
            noiseTextures[i] = new ResourceLocation(
                    StoneyCore.MOD_ID,
                    "textures/overlay/noise/noise_" + i + ".png"
            );
        }
    }

    private static void renderNoise(GuiGraphics gui, int w, int h, double percent) {
        if (currentNoiseTextureTime-- <= 0 && !Minecraft.getInstance().isPaused()) {
            currentNoiseTexture = (currentNoiseTexture + 1) % noiseTextures.length;
            currentNoiseTextureTime = 10;
        }

        float alpha = (float) Math.max(0, 1.0f - percent) * 0.2f;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 0.5f, 0.5f, alpha);

        gui.blit(noiseTextures[currentNoiseTexture], 0, 0, 0, 0, w, h, w, h);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private static void renderTunnel(GuiGraphics gui, int w, int h, double percent) {
        int o1 = (int) ((Math.max(0, 0.2f - percent) * 255));
        int o2 = (int) ((Math.max(0, 0.6f - percent) * 255));

        gui.fillGradient(0, 0, w, h, o1 << 24, o2 << 24);
    }

    private static void renderVisorToggleProgress(GuiGraphics gui, float tickDelta, Minecraft mc) {
        if (StoneyCore.getConfig().combatOptions().getToggleVisorTime() == 0
                || !ClientPlatform.getKeyInputHelper().isTogglingVisor()
                || ClientPlatform.getKeyInputHelper().isVisorToggled()
                || ClientPlatform.getKeyInputHelper().toggleVisorTicks() <= 0.0f) {
            lastRenderedProgress = 0.0f;
            return;
        }

        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;

        float target = ClientPlatform.getKeyInputHelper().toggleProgress();
        float smooth = lastRenderedProgress + (target - lastRenderedProgress)
                * Math.min(1.0f, 20.0f * tickDelta);

        lastRenderedProgress = smooth;

        int progressWidth = (int) (124 * smooth);

        gui.blit(VISOR_PROGRESS_BACKGROUND, centerX - 64, centerY + 42, 0, 0, 128, 16, 128, 16);

        if (progressWidth > 0) {
            gui.blit(VISOR_PROGRESS_BAR, centerX - 62, centerY + 44,
                    0, 0, progressWidth, 12, 124, 12);
        }
    }
}