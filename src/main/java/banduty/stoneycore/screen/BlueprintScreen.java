
package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.event.custom.RenderBlueprintEvents;
import banduty.stoneycore.items.item.BlueprintItem;
import banduty.stoneycore.screen.button.TexturedButton;
import banduty.stoneycore.structure.StructureSpawnRegistry;
import banduty.stoneycore.structure.StructureSpawner;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public class BlueprintScreen extends HandledScreen<BlueprintScreenHandler> {
    private boolean showLegend = false;

    public BlueprintScreen(BlueprintScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 170;
        this.backgroundHeight = 172;
        this.playerInventoryTitleY = 1000;
        this.titleY = 1000;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        if (!(this.handler.getItemStack().getItem() instanceof BlueprintItem blueprintItem)) return;
        String namespace = this.handler.getStructureId().getNamespace();
        String originalBasePath = blueprintItem.getBackgroundTexture();
        String basePath = originalBasePath.isEmpty() ? "manuscript" : originalBasePath;
        Identifier textureButton = new Identifier(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + "_legend_button.png");
        Identifier textureButtonOverlay = new Identifier(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + "_legend_button_overlay.png");

        TexturedButton legendButton = new TexturedButton(this.x + this.backgroundWidth - 21, this.y + 5, 16, 16,
                button -> this.showLegend = !this.showLegend, textureButton, textureButtonOverlay);

        this.addDrawableChild(legendButton);

        if (this.showLegend) context.setShaderColor(0.1f, 0.1f, 0.1f, 1f);
        else context.setShaderColor(1f, 1f, 1f, 1f);
        Identifier texture = new Identifier(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + ".png");
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        context.drawTexture(texture, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        texture = new Identifier(this.handler.getStructureId().getNamespace(), "textures/gui/blueprint/" + this.handler.getStructureId().getPath() + ".png");
        RenderSystem.setShaderTexture(0, texture);
        context.drawTexture(texture, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

        if (this.showLegend) {
            context.setShaderColor(1f, 1f, 1f, 1f);

            texture = new Identifier(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + "_legend.png");

            RenderSystem.setShaderTexture(0, texture);
            context.drawTexture(texture, this.x - this.backgroundWidth / 2 + 20, this.y, 0, 0, this.backgroundWidth / 2, this.backgroundHeight, this.backgroundWidth / 2, this.backgroundHeight);

            renderBlockFinderList(context);
        }

        RenderSystem.disableBlend();
    }

    private void renderBlockFinderList(DrawContext context) {
        Identifier structureId = this.handler.getStructureId();
        StructureSpawner spawner = StructureSpawnRegistry.get(structureId);
        if (spawner == null) return;

        List<Block> blocks = spawner.getBlockFinders();

        int maxTextWidth = this.backgroundWidth / 2 - 75;
        int startX = this.x - this.backgroundWidth / 2 + 30;
        int startY = this.y + 10;
        int lineHeight = 10;

        TextRenderer font = this.textRenderer;

        int maxWidth = 0;
        for (int i = 0; i < Math.min(blocks.size(), 10); i++) {
            Block block = blocks.get(i);
            String name = (i + 1) + ". " + block.getName().getString();
            int width = font.getWidth(name);
            if (width > maxWidth) maxWidth = width;
        }

        float textScale = maxWidth > maxTextWidth
                ? ((float) maxTextWidth / (float) maxWidth) * 4
                : 1.0f;

        for (int i = 0; i < Math.min(blocks.size(), 10); i++) {
            Block block = blocks.get(i);
            String name = (i + 1) + ". " + block.getName().getString();

            context.getMatrices().push();
            context.getMatrices().translate(startX, startY + i * lineHeight, 0);
            context.getMatrices().scale(textScale, textScale, 1.0f);
            context.drawText(font, name, 0, 0, 0x000000, false);
            context.getMatrices().pop();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        RenderBlueprintEvents.EVENT.invoker().render(context, mouseX, mouseY, delta, this);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
