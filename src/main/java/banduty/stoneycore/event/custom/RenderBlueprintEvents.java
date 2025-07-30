package banduty.stoneycore.event.custom;

import banduty.stoneycore.screen.BlueprintScreen;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.DrawContext;

public interface RenderBlueprintEvents {
    Event<RenderBlueprintEvents> EVENT = EventFactory.createArrayBacked(
            RenderBlueprintEvents.class,
            listeners -> (context, mouseX, mouseY, delta, blueprintScreen) -> {
                for (RenderBlueprintEvents listener : listeners) {
                    listener.render(context, mouseX, mouseY, delta, blueprintScreen);
                }
            }
    );

    void render(DrawContext context, int mouseX, int mouseY, float delta, BlueprintScreen blueprintScreen);
}