package banduty.stoneycore.event.custom;

import banduty.stoneycore.screen.BlueprintScreen;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;

public interface RenderBlueprintEvents {
    Event<RenderBlueprintEvents> EVENT = EventFactory.createArrayBacked(
            RenderBlueprintEvents.class,
            listeners -> (guiGraphics, mouseX, mouseY, delta, blueprintScreen) -> {
                for (RenderBlueprintEvents listener : listeners) {
                    listener.render(guiGraphics, mouseX, mouseY, delta, blueprintScreen);
                }
            }
    );

    void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, BlueprintScreen blueprintScreen);
}