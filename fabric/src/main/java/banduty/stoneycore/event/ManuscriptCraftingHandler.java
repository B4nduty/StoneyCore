package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.CraftingPreviewCallback;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.items.manuscript.Manuscript;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ManuscriptCraftingHandler implements CraftingPreviewCallback {

    @Override
    public ItemStack modifyResult(ServerPlayer player, CraftingContainer inventory, ItemStack original) {
        ItemStack paper = ItemStack.EMPTY;
        ItemStack manuscript = ItemStack.EMPTY;
        ItemStack hammer = ItemStack.EMPTY;
        ItemStack itemInput = ItemStack.EMPTY;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(Items.PAPER)) {
                paper = stack;
            } else if (stack.is(SCItems.MANUSCRIPT.get())) {
                manuscript = stack;
            } else if (stack.getItem() instanceof SmithingHammer) {
                hammer = stack;
            } else {
                itemInput = stack;
            }
        }

        if (manuscript.getItem() instanceof Manuscript && !Manuscript.hasTargetStack(manuscript)) return ItemStack.EMPTY;

        if (!itemInput.isEmpty() && !paper.isEmpty() && !hammer.isEmpty() && manuscript.isEmpty()) {
            return Manuscript.createForStack(itemInput);
        }

        return original;
    }
}
