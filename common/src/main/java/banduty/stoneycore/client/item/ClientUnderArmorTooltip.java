package banduty.stoneycore.client.item;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionData;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionsStorage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClientUnderArmorTooltip implements ClientTooltipComponent {
    private static final ResourceLocation SLOT_BACKGROUND_SPRITE = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "container/bundle/slot_background");

    private static final int SLOT_SIZE = 24;
    private static final int COLUMNS = 4;

    private final UnderArmorContents contents;
    private final List<ArmorAttachmentSlotDefinitionData> visibleSlots;

    public ClientUnderArmorTooltip(UnderArmorContents contents, ArmorItem.Type armorType) {
        this.contents = contents;
        this.visibleSlots = calculateVisibleSlots(armorType);
    }

    @Override
    public int getHeight() {
        return this.gridSizeY() * SLOT_SIZE;
    }

    @Override
    public int getWidth(Font font) {
        return Math.min(this.visibleSlots.size(), COLUMNS) * SLOT_SIZE;
    }

    private int gridSizeY() {
        return Mth.positiveCeilDiv(this.visibleSlots.size(), COLUMNS);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        for (int i = 0; i < this.visibleSlots.size(); i++) {
            ArmorAttachmentSlotDefinitionData slotDef = this.visibleSlots.get(i);

            int column = i % COLUMNS;
            int row = i / COLUMNS;

            int slotX = x + column * SLOT_SIZE;
            int slotY = y + row * SLOT_SIZE;

            guiGraphics.blitSprite(SLOT_BACKGROUND_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);

            ItemStack equippedItem = ItemStack.EMPTY;
            if (this.contents != null && !this.contents.isEmpty()) equippedItem = findItemForSlot(slotDef);

            if (!equippedItem.isEmpty()) {
                guiGraphics.renderItem(equippedItem, slotX + 4, slotY + 4, i);
                guiGraphics.renderItemDecorations(font, equippedItem, slotX + 4, slotY + 4);
            } else if (!slotDef.icon().isEmpty()) {
                ResourceLocation iconSprite = ResourceLocation.parse(slotDef.icon());
                guiGraphics.blitSprite(iconSprite, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            }
        }
    }

    private ItemStack findItemForSlot(ArmorAttachmentSlotDefinitionData slotDef) {
        for (ItemStack stack : this.contents.attachments()) {
            if (slotDef.items().contains(stack.getItemHolder().unwrapKey().map(ResourceKey::location).orElse(null))) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private List<ArmorAttachmentSlotDefinitionData> calculateVisibleSlots(ArmorItem.Type armorType) {
        List<ArmorAttachmentSlotDefinitionData> allSlots = ArmorAttachmentSlotDefinitionsStorage.getSlotsForArmorType(armorType);
        List<ArmorAttachmentSlotDefinitionData> filtered = new ArrayList<>();

        for (ArmorAttachmentSlotDefinitionData slotDef : allSlots) {
            if (slotDef.requiredSlot().isEmpty()) {
                filtered.add(slotDef);
            } else {
                boolean unlocked = false;
                if (this.contents != null && !this.contents.isEmpty()) {
                    for (ItemStack stack : this.contents.attachments()) {
                        ArmorAttachmentSlotDefinitionData activeDef = ArmorAttachmentSlotDefinitionsStorage.getData(stack, armorType);
                        if (activeDef.slot().equals(slotDef.requiredSlot())) {
                            unlocked = true;
                            break;
                        }
                    }
                }
                if (unlocked) {
                    filtered.add(slotDef);
                }
            }
        }
        return filtered;
    }
}