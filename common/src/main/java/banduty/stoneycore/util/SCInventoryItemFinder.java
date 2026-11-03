package banduty.stoneycore.util;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SCInventoryItemFinder {
    // Items.AIR is used as a "no alternative" placeholder in ammo requirements, and every
    // empty slot reports AIR, so empty stacks must never count as a match.
    private static boolean matches(ItemStack stack, Item... items) {
        if (stack.isEmpty()) return false;
        for (Item item : items) {
            if (item != Items.AIR && stack.getItem() == item) return true;
        }
        return false;
    }

    /** Removes up to {@code amount} items matching any of {@code items}, across as many stacks as needed. */
    public static int consumeItems(ServerPlayer player, int amount, Item... items) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (!matches(stack, items)) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        if (remaining != amount) player.getInventory().setChanged();
        return amount - remaining;
    }

    public static int countItem(ServerPlayer player, Item... items) {
        int count = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (matches(stack, items)) count += stack.getCount();
        }

        return count;
    }

    public static ItemStack findUnderArmor(LivingEntity entity, ArmorItem.Type type) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof SCUnderArmor underArmor &&
                    underArmor.getType() == type) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
