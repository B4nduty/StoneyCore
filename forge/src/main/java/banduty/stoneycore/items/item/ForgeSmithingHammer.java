package banduty.stoneycore.items.item;

import banduty.stoneycore.items.SmithingHammer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ForgeSmithingHammer extends SmithingHammer {
    public ForgeSmithingHammer(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack newStack = stack.copy();
        newStack.setDamageValue(stack.getDamageValue() + 1);
        if (newStack.getDamageValue() >= newStack.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return newStack;
    }
}