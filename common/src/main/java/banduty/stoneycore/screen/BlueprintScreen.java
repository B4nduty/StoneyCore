package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.blueprint.BlueprintItem;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.screen.button.TexturedButton;
import banduty.stoneycore.structure.StructureSpawnRegistry;
import banduty.stoneycore.structure.StructureSpawner;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlueprintScreen extends AbstractContainerScreen<BlueprintScreenHandler> {
    private boolean showLegend = false;
    private TexturedButton legendButton;

    public BlueprintScreen(BlueprintScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 171;
        this.imageHeight = 173;
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    public int getX() {
        return this.leftPos;
    }

    public int getY() {
        return this.topPos;
    }

    public Font getTextRenderer() {
        return this.font;
    }

    @Override
    protected void init() {
        super.init();

        this.legendButton = new TexturedButton(0, 0, 87, 174,
                button -> this.showLegend = !this.showLegend,
                new ResourceLocation("stoneycore", "textures/models/armor/a_layer_1.png"),
                new ResourceLocation("stoneycore", "textures/models/armor/a_layer_1.png")
        );

        this.addRenderableWidget(legendButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        if (!(this.menu.getItemStack().getItem() instanceof BlueprintItem blueprintItem)) return;

        String namespace = this.menu.getStructureId().getNamespace();
        String originalBasePath = blueprintItem.getBackgroundTexture();
        String basePath = originalBasePath.isEmpty() ? "manuscript" : originalBasePath;

        int xOffset = 92 / 2 - 7; // move right (+) or left (-)
        int yOffset = 0;  // move down (+) or up (-)

        ResourceLocation texture = new ResourceLocation(
                originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace,
                "textures/gui/blueprint/" + basePath + ".png"
        );

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(
                texture,
                this.getX() + xOffset,
                this.getY() + yOffset,
                0, 0,
                this.imageWidth,
                this.imageHeight
        );

        texture = new ResourceLocation(
                namespace,
                "textures/gui/blueprint/" + this.menu.getStructureId().getPath() + ".png"
        );

        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(
                texture,
                this.getX() + xOffset,
                this.getY() + yOffset,
                0, 0,
                this.imageWidth,
                this.imageHeight,
                this.imageWidth,
                this.imageHeight
        );

        guiGraphics.setColor(1f, 1f, 1f, 1f);

        renderLegend(guiGraphics, originalBasePath, namespace, basePath, 6 + xOffset);

        RenderSystem.disableBlend();
    }

    private void renderLegend(GuiGraphics guiGraphics, String originalBasePath, String namespace, String basePath, int xOffset) {

        ResourceLocation texture = new ResourceLocation(
                originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace,
                "textures/gui/blueprint/" + basePath + "_legend.png"
        );


        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(texture, this.getX() - this.imageWidth / 2 + xOffset, this.getY(), 0, 0, 87, 174, 87, 174);

        renderBlockFinderList(guiGraphics, xOffset);
    }

    private void updateLegendButton(String originalBasePath, String namespace, String basePath) {
        String legendSuffix = showLegend ? "" : "_legend";

        int xPos = this.getX() - (showLegend ? -(this.imageWidth - 157 + 1) : (this.imageWidth / 2 - 74));
        int width = showLegend ? 157 : 16;
        int height = 174;

        ResourceLocation hoverTexture = new ResourceLocation(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace,
                "textures/gui/blueprint/" + basePath + legendSuffix + "_overlay.png");

        legendButton.setX(xPos);
        legendButton.setY(this.getY());
        legendButton.setWidth(width);
        legendButton.setHeight(height);
        legendButton.setHoverTexture(hoverTexture);
    }

    private void renderBlockFinderList(GuiGraphics guiGraphics, int x) {

        ResourceLocation structureId = this.menu.getStructureId();
        StructureSpawner spawner = StructureSpawnRegistry.get(structureId);
        if (spawner == null) return;

        List<Block> blocks = spawner.getBlockFinders();

        float textScale = 1.0f;
        int baseLineHeight = 10;
        int lineHeight = (int) (baseLineHeight * textScale);

        int maxTextWidth = (int) (65 / textScale);

        int startX = this.getX() - this.imageWidth / 2 + 10 + x;
        int startY = this.getY() + 10;

        int yOffset = 0;

        Font font = this.font;

        for (int i = 0; i < Math.min(blocks.size(), 10); i++) {

            Block block = blocks.get(i);

            Component text = Component.literal((i + 1) + ". ")
                    .append(block.getName());

            List<FormattedCharSequence> lines = font.split(text, maxTextWidth);

            for (FormattedCharSequence line : lines) {

                guiGraphics.pose().pushPose();

                guiGraphics.pose().translate(
                        startX,
                        startY + yOffset,
                        0
                );

                guiGraphics.pose().scale(textScale, textScale, 1f);

                guiGraphics.drawString(
                        font,
                        line,
                        0,
                        0,
                        0x000000,
                        false
                );

                guiGraphics.pose().popPose();

                yOffset += lineHeight;
            }

            yOffset += (int) (3 * textScale);   // spacing between block entries
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        Services.BLUEPRINT.renderBlueprintEvents(guiGraphics, mouseX, mouseY, delta, this);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
