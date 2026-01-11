
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlueprintScreen extends AbstractContainerScreen<BlueprintScreenHandler> {
    private boolean showLegend = false;

    public BlueprintScreen(BlueprintScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 170;
        this.imageHeight = 172;
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
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        if (!(this.menu.getItemStack().getItem() instanceof BlueprintItem blueprintItem)) return;
        String namespace = this.menu.getStructureId().getNamespace();
        String originalBasePath = blueprintItem.getBackgroundTexture();
        String basePath = originalBasePath.isEmpty() ? "manuscript" : originalBasePath;
        ResourceLocation textureButton = new ResourceLocation(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + "_legend_button.png");
        ResourceLocation textureButtonOverlay = new ResourceLocation(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + "_legend_button_overlay.png");

        TexturedButton legendButton = new TexturedButton(this.getX() + this.imageWidth - 21, this.getY() + 5, 16, 16,
                button -> this.showLegend = !this.showLegend, textureButton, textureButtonOverlay);

        this.addRenderableWidget(legendButton);

        if (this.showLegend) guiGraphics.setColor(0.1f, 0.1f, 0.1f, 1f);
        else guiGraphics.setColor(1f, 1f, 1f, 1f);
        ResourceLocation texture = new ResourceLocation(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + ".png");
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.imageWidth, this.imageHeight);
        texture = new ResourceLocation(this.menu.getStructureId().getNamespace(), "textures/gui/blueprint/" + this.menu.getStructureId().getPath() + ".png");
        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        if (this.showLegend) {
            guiGraphics.setColor(1f, 1f, 1f, 1f);

            texture = new ResourceLocation(originalBasePath.isEmpty() ? StoneyCore.MOD_ID : namespace, "textures/gui/blueprint/" + basePath + "_legend.png");

            RenderSystem.setShaderTexture(0, texture);
            guiGraphics.blit(texture, this.getX() - this.imageWidth / 2 + 20, this.getY(), 0, 0, this.imageWidth / 2, this.imageHeight, this.imageWidth / 2, this.imageHeight);

            renderBlockFinderList(guiGraphics);
        }

        RenderSystem.disableBlend();
    }

    private void renderBlockFinderList(GuiGraphics guiGraphics) {
        ResourceLocation structureId = this.menu.getStructureId();
        StructureSpawner spawner = StructureSpawnRegistry.get(structureId);
        if (spawner == null) return;

        List<Block> blocks = spawner.getBlockFinders();

        int maxTextWidth = this.imageWidth / 2 - 75;
        int startX = this.getX() - this.imageWidth / 2 + 30;
        int startY = this.getY() + 10;
        int lineHeight = 10;

        Font font = this.font;

        int maxWidth = 0;
        for (int i = 0; i < Math.min(blocks.size(), 10); i++) {
            Block block = blocks.get(i);
            String name = (i + 1) + ". " + block.getName().getString();
            int width = font.width(name);
            if (width > maxWidth) maxWidth = width;
        }

        float textScale = maxWidth > maxTextWidth
                ? ((float) maxTextWidth / (float) maxWidth) * 4
                : 1.0f;

        for (int i = 0; i < Math.min(blocks.size(), 10); i++) {
            Block block = blocks.get(i);
            String name = (i + 1) + ". " + block.getName().getString();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(startX, startY + i * lineHeight, 0);
            guiGraphics.pose().scale(textScale, textScale, 1.0f);
            guiGraphics.drawString(font, name, 0, 0, 0x000000, false);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        Services.BLUEPRINT.renderBlueprintEvents(guiGraphics, mouseX, mouseY, delta, this);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
