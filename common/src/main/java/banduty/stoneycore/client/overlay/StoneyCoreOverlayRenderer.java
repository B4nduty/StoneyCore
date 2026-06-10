package banduty.stoneycore.client.overlay;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class StoneyCoreOverlayRenderer {
    private static final ResourceLocation VISOR_PROGRESS_BACKGROUND = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/overlay/visor_progress_background.png");
    private static final ResourceLocation VISOR_PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/overlay/visor_progress_bar.png");

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

        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
        for (ItemStack armorAttachments : SCUnderArmor.getArmorAttachments(itemStack)) {
            var data = ArmorAttachmentDefinitionsStorage.getData(armorAttachments);
            ResourceLocation visorId = data.visoredHelmet();

            if (!armorAttachments.getOrDefault(SCDataComponents.VISOR_OPEN.get(), false)
                    && !(visorId.getPath().isEmpty() || visorId.getPath().equals("empty"))
                    && StoneyCore.getConfig().visualOptions().getVisoredHelmet()) {

                String namespace = visorId.getNamespace().isEmpty() ? "stoneycore" : visorId.getNamespace();
                ResourceLocation visorTexture = ResourceLocation.fromNamespaceAndPath(namespace, "textures/overlay/visor/" + visorId.getPath() + ".png");

                RenderSystem.setShaderTexture(0, visorTexture);

                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

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
        double maxStamina = player.getAttributeValue(SCAttributes.MAX_STAMINA);
        double secondLevel = maxStamina * 0.15d;

        if (!player.isCreative()
                && StaminaData.isStaminaBlocked((IEntityDataSaver) player)
                && StoneyCore.getConfig().visualOptions().getLowStaminaIndicator()) {

            double staminaPercentage = stamina / secondLevel;

            if (StoneyCore.getConfig().combatOptions().getRealisticCombat()) {
                if (noiseTextures == null) initNoiseTextures();
                renderBlurEffect(guiGraphics, width, height, staminaPercentage);
            } else {
                int opacity = (int) ((Math.max(0, 0.4f - staminaPercentage) * 255));
                int green = StaminaData.isStaminaBlocked((IEntityDataSaver) player) ? 0 : (int)(stamina / secondLevel);
                int gradientColorEnd = opacity << 24 | green | 0x00FF0000;

                guiGraphics.fillGradient(0, 0, width, height, 0x00FFFFFF, gradientColorEnd);
            }
        }
    }

    private void initNoiseTextures() {
        noiseTextures = new ResourceLocation[12];
        for (int i = 0; i < noiseTextures.length; i++) {
            noiseTextures[i] = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/overlay/noise/noise_" + i + ".png");
        }
    }

    private void renderBlurEffect(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        float blurStrength = (float) (Math.max(0.1f, 1.0f - staminaPercentage) * 0.4f);

        try {
            ClientPlatform.getClientPlaformHelper().startBlurService(blurStrength * 12.0f);
        } catch (Exception ignored) {}

        renderTunnelVision(guiGraphics, width, height, staminaPercentage);
        if (StoneyCore.getConfig().visualOptions().getNoiseEffect())
            renderNoise(guiGraphics, width, height, staminaPercentage);
    }

    private void renderNoise(GuiGraphics guiGraphics, int width, int height, double staminaPercentage) {
        if (noiseTextures == null) return;

        ResourceLocation noiseTexture = noiseTextures[currentNoiseTexture];
        float alpha = (float) Math.max(0, 1.0f - staminaPercentage);

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
        int opacity2 = (int) ((Math.max(0, 0.6f - staminaPercentage) * 255));
        guiGraphics.fillGradient(0, 0, width, height, opacity << 24, opacity2 << 24);
    }

    private void renderVisorToggleProgress(GuiGraphics guiGraphics, float tickDelta) {
        if (StoneyCore.getConfig().combatOptions().getToggleVisorTime() == 0
                || !ClientPlatform.getKeyInputHelper().isTogglingVisor()
                || ClientPlatform.getKeyInputHelper().isVisorToggled()
                || ClientPlatform.getKeyInputHelper().toggleVisorTicks() <= 0.0f) {
            lastRenderedProgress = 0.0f;
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        float targetProgress = ClientPlatform.getKeyInputHelper().toggleProgress();
        lastRenderedProgress = lastRenderedProgress + (targetProgress - lastRenderedProgress) * Math.min(1.0f, 20.0f * tickDelta);

        int progressWidth = (int) (124 * lastRenderedProgress);

        guiGraphics.blit(VISOR_PROGRESS_BACKGROUND, centerX - 64, centerY + 42, 0, 0, 128, 16, 128, 16);
        if (progressWidth > 0) {
            guiGraphics.blit(VISOR_PROGRESS_BAR, centerX - 62, centerY + 44, 0, 0, progressWidth, 12, 124, 12);
        }
    }
}