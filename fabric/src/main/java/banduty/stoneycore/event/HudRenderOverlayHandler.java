package banduty.stoneycore.event;

import banduty.stoneycore.client.render.SCOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class HudRenderOverlayHandler {

    public static void register() {
        HudRenderCallback.EVENT.register(SCOverlayRenderer::render);
    }
}