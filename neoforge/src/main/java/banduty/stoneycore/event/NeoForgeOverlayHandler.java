package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.client.overlay.StoneyCoreOverlayRenderer;
import banduty.stoneycore.platform.ClientPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgeOverlayHandler {

    private static final StoneyCoreOverlayRenderer OVERLAY_RENDERER = new StoneyCoreOverlayRenderer();

    public static final LayeredDraw.Layer MAIN_OVERLAYS = (guiGraphics, deltaTracker) -> {
        if (shouldRenderOverlays()) {
            float partialTick = deltaTracker.getGameTimeDeltaTicks();
            OVERLAY_RENDERER.render(guiGraphics, partialTick);
        }
    };

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "main_overlays"), MAIN_OVERLAYS);
    }

    private static boolean shouldRenderOverlays() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return false;
        return !ClientPlatform.getKeyInputHelper().isHidingVisor();
    }
}