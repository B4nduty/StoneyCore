package banduty.stoneycore.client.item;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.deco.DecoContents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClientDecoTooltip implements ClientTooltipComponent {
    private static final ResourceLocation SLOT_BACKGROUND_SPRITE = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "container/bundle/slot_background");

    private static final int SLOT_SIZE = 24;
    private static final int COLUMNS = 4;

    private final DecoContents contents;

    public ClientDecoTooltip(DecoContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight() {
        return this.gridSizeY() * SLOT_SIZE + 4;
    }

    @Override
    public int getWidth(Font font) {
        return Math.min(this.contents.items().size(), COLUMNS) * SLOT_SIZE;
    }

    private int gridSizeY() {
        return Mth.positiveCeilDiv(this.contents.items().size(), COLUMNS);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        List<ItemStack> list = this.contents.items();
        int slotIndex = list.size() - 1;

        int rows = this.gridSizeY();
        for (int p = 0; p < rows; ++p) {
            for (int q = 0; q < COLUMNS; ++q) {
                if (slotIndex < 0) break;

                int slotX = x + q * SLOT_SIZE;
                int slotY = y + p * SLOT_SIZE;

                this.renderSlot(slotIndex, slotX, slotY, list, font, guiGraphics);
                slotIndex--;
            }
        }
    }

    private void renderSlot(int index, int slotX, int slotY, List<ItemStack> list, Font font, GuiGraphics guiGraphics) {
        ItemStack itemStack = list.get(index);

        guiGraphics.blitSprite(SLOT_BACKGROUND_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);

        guiGraphics.renderItem(itemStack, slotX + 4, slotY + 4, index);
        guiGraphics.renderItemDecorations(font, itemStack, slotX + 4, slotY + 4);
    }
}