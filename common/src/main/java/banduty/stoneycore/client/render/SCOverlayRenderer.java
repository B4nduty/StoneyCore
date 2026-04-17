package banduty.stoneycore.client.render;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
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

public class SCOverlayRenderer {
    private static ResourceLocation[] noiseTextures;
    private static int currentNoise = 0;
    private static int noiseTick = 0;

    public static void render(GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.options.hideGui) return;
        if (!mc.options.getCameraType().isFirstPerson()
                && !StoneyCore.getConfig().visualOptions().overlayThirdPerson()) return;

        int width = gui.guiWidth();
        int height = gui.guiHeight();

        renderVisor(gui, player, width, height);
        renderStaminaEffects(gui, player, width, height);

        StoneyCoreClient.LAND_TITLE_RENDERER.render(gui);
    }

    private static void renderVisor(GuiGraphics gui, LocalPlayer player, int width, int height) {
        for (ItemStack stack : Services.PLATFORM.getEquippedAccessories(player)) {
            ResourceLocation visorId = AccessoriesDefinitionsStorage.getData(stack).visoredHelmet();

            if (!NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)
                    && !(visorId.getPath().isEmpty() || visorId.getPath().equals("empty"))
                    && StoneyCore.getConfig().visualOptions().getVisoredHelmet()) {

                String namespace = visorId.getNamespace().isEmpty() ? "stoneycore" : visorId.getNamespace();
                ResourceLocation texture = new ResourceLocation(namespace,
                        "textures/overlay/visor/" + visorId.getPath() + ".png");

                float alpha = player.isCreative()
                        ? StoneyCore.getConfig().visualOptions().getVisoredHelmetAlphaCreative()
                        : StoneyCore.getConfig().visualOptions().getVisoredHelmetAlphaSurvival();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

                gui.blit(texture, 0, 0, 0, 0, width, height, width, height);

                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
                break;
            }
        }
    }

    private static void renderStaminaEffects(GuiGraphics gui, LocalPlayer player, int width, int height) {
        double stamina = StaminaData.getStamina(player);
        double max = player.getAttributeBaseValue(Services.ATTRIBUTES.getMaxStamina()) * 0.15d;

        if (player.isCreative()) return;
        if (!StaminaData.isStaminaBlocked((IEntityDataSaver) player)) return;

        double percent = stamina / max;

        if (StoneyCore.getConfig().combatOptions().getRealisticCombat()) {
            renderTunnelVision(gui, width, height, percent);
            renderNoise(gui, width, height, percent);
        } else {
            int opacity = (int) ((Math.max(0, 0.4f - percent) * 255));
            int color = opacity << 24 | 0x00FF0000;
            gui.fillGradient(0, 0, width, height, 0x00FFFFFF, color);
        }
    }

    private static void renderTunnelVision(GuiGraphics gui, int width, int height, double percent) {
        int opacity1 = (int) ((Math.max(0, 0.2f - percent) * 255));
        int opacity2 = (int) ((Math.max(0, 0.6f - percent) * 255));

        gui.fillGradient(0, 0, width, height, opacity1 << 24, opacity2 << 24);
    }

    private static void renderNoise(GuiGraphics gui, int width, int height, double percent) {
        if (noiseTextures == null) {
            noiseTextures = new ResourceLocation[12];
            for (int i = 0; i < 12; i++) {
                noiseTextures[i] = new ResourceLocation(StoneyCore.MOD_ID,
                        "textures/overlay/noise/noise_" + i + ".png");
            }
        }

        if (noiseTick-- <= 0) {
            currentNoise = (currentNoise + 1) % noiseTextures.length;
            noiseTick = 10;
        }

        float alpha = (float) Math.max(0, 1.0f - percent) * 0.2f;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 0.5f, 0.5f, alpha);

        gui.blit(noiseTextures[currentNoise], 0, 0, 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }
}