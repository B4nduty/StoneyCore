package banduty.stoneycore.event.custom;

import banduty.stoneycore.screen.BlueprintScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class RenderBlueprintEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final int mouseX;
    private final int mouseY;
    private final float delta;
    private final BlueprintScreen blueprintScreen;

    public RenderBlueprintEvent(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, BlueprintScreen blueprintScreen) {
        this.guiGraphics = guiGraphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
        this.blueprintScreen = blueprintScreen;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public float getDelta() {
        return delta;
    }

    public BlueprintScreen getBlueprintScreen() {
        return blueprintScreen;
    }
}