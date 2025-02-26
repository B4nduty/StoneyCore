package banduty.stoneycore.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class SCInventoryItemFinder {
    public static ItemStack getItemFromInventory(ServerPlayerEntity player, Item... items) {
        return player.getInventory().main.stream()
                .filter(stack -> java.util.Arrays.stream(items).anyMatch(item -> stack.getItem() == item))
                .findFirst()
                .orElse(null);
    }

    public static int getItemSlot(ServerPlayerEntity player, ItemStack itemStack) {
        return player.getInventory().main.stream()
                .filter(stack -> stack == itemStack)
                .map(player.getInventory().main::indexOf)
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
