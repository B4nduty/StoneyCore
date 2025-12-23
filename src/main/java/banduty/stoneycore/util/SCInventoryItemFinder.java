package banduty.stoneycore.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SCInventoryItemFinder {
    public static ItemStack getItemFromInventory(ServerPlayer player, Item... items) {
        return player.getInventory().items.stream()
                .filter(stack -> java.util.Arrays.stream(items).anyMatch(item -> stack.getItem() == item))
                .findFirst()
                .orElse(null);
    }

    public static int getItemSlot(ServerPlayer player, ItemStack itemStack) {
        return player.getInventory().items.stream()
                .filter(stack -> stack == itemStack)
                .map(player.getInventory().items::indexOf)
                .findFirst()
                .orElse(-1);
    }

    public static boolean areItemsInInventory(ItemStack[] itemStacks, int[] necessaryAmounts) {
        if (itemStacks.length != necessaryAmounts.length) {
            throw new IllegalArgumentException("ItemStacks and necessaryAmounts must have the same length.");
        }

        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStack = itemStacks[i];
            int necessaryAmount = necessaryAmounts[i];

            if (itemStack == null || itemStack.getCount() < necessaryAmount) {
                return false;
            }
        }

        return true;
    }
}
