package banduty.stoneycore.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class LandTitleRenderer {

    private Component currentTitle = null;
    private int titleTicksRemaining = 0;

    private final int fadeInTicks = 20;
    private final int displayTicks = 40;
    private final int fadeOutTicks = 20;

    public void tick() {
        if (titleTicksRemaining > 0) {
            titleTicksRemaining--;
        }
    }

    public void showTitle(Component title) {
        this.currentTitle = title;
        this.titleTicksRemaining = fadeInTicks + displayTicks + fadeOutTicks;
    }

    public void render(GuiGraphics guiGraphics) {
        if (currentTitle == null || titleTicksRemaining <= 0) return;

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int color = getColor();

        int textWidth = font.width(currentTitle);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 4;

        RenderSystem.enableBlend();
        if (currentTitle != null) guiGraphics.drawString(font, currentTitle, x, y, color, true);
        RenderSystem.disableBlend();
    }

    private int getColor() {
        float totalDuration = fadeInTicks + displayTicks + fadeOutTicks;
        float age = totalDuration - titleTicksRemaining;
        float alpha;

        if (age < fadeInTicks) {
            // Fade in
            alpha = age / fadeInTicks;
        } else if (age < fadeInTicks + displayTicks) {
            // Fully visible
            alpha = 1.0f;
        } else if (age < totalDuration) {
            // Fade out
            alpha = (totalDuration - age) / fadeOutTicks;
        } else {
            // Past display time
            alpha = 0.0f;
        }

        alpha = Mth.clamp(alpha, 0f, 1.0f);
        return ((int) (alpha * 255) << 24) | 0xFFFFFF;
    }
}
