package banduty.stoneycore.screen.button;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedButton extends Button {
    private final ResourceLocation buttonTexture;
    private final ResourceLocation hoverTexture;

    public TexturedButton(int x, int y, int width, int height, OnPress onPress, ResourceLocation buttonTexture, ResourceLocation hoverTexture) {
        super(x, y, width, height, Component.literal(""), onPress, Button.DEFAULT_NARRATION);
        this.buttonTexture = buttonTexture;
        this.hoverTexture = hoverTexture;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        ResourceLocation texture = this.isHovered() ? hoverTexture : buttonTexture;
        Minecraft.getInstance().getTextureManager().bindForSetup(texture);
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
    }
}