package banduty.stoneycore.forge.client;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.client.overlay.StoneyCoreOverlayRenderer;
import banduty.stoneycore.platform.ClientPlatform;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeOverlayHandler {

    private static final StoneyCoreOverlayRenderer OVERLAY_RENDERER = new StoneyCoreOverlayRenderer();

    // Main overlays - renders on top of HUD
    public static final IGuiOverlay MAIN_OVERLAYS = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (shouldRenderOverlays()) {
            OVERLAY_RENDERER.render(guiGraphics, partialTick);
        }
    };

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("main_overlays", MAIN_OVERLAYS);
    }

    private static boolean shouldRenderOverlays() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return false;
        return !ClientPlatform.getKeyInputHelper().isHidingVisor();
    }
}