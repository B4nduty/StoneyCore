package banduty.stoneycore.items.custom;

import net.minecraft.world.item.ItemStack;

public interface CraftmanAnvilHelper {
    default ItemStack acceptCraftmanAnvilItem(ItemStack itemStack) {
        return itemStack;
    }
}
