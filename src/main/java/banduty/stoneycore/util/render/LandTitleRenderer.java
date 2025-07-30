package banduty.stoneycore.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class LandTitleRenderer {

    private Text currentTitle = null;
    private int titleTicksRemaining = 0;

    private final int fadeInTicks = 20;
    private final int displayTicks = 40;
    private final int fadeOutTicks = 20;

    public void tick() {
        if (titleTicksRemaining > 0) {
            titleTicksRemaining--;
        }
    }

    public void showTitle(Text title) {
        this.currentTitle = title;
        this.titleTicksRemaining = fadeInTicks + displayTicks + fadeOutTicks;
    }

    public void render(DrawContext context) {
        if (currentTitle == null || titleTicksRemaining <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer font = client.textRenderer;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int color = getColor();

        int textWidth = font.getWidth(currentTitle);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 4;

        RenderSystem.enableBlend();
        if (currentTitle != null) context.drawText(font, currentTitle, x, y, color, true);
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

        alpha = MathHelper.clamp(alpha, 0f, 1.0f);
        return ((int) (alpha * 255) << 24) | 0xFFFFFF;
    }
}
