package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.client.render.SCOverlayRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, value = Dist.CLIENT)
public class HudRenderOverlayHandler {
    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        SCOverlayRenderer.render(event.getGuiGraphics());
    }
}
