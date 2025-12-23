package banduty.stoneycore.items.item;

import net.minecraft.world.item.ItemStack;

public interface CraftmanAnvilHelper {
    default ItemStack acceptCraftmanAnvilItem(ItemStack itemStack) {
        return itemStack;
    }
}
