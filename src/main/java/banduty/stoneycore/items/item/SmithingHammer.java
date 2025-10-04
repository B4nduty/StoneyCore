package banduty.stoneycore.items.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SmithingHammer extends Item {
    public SmithingHammer(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        ItemStack newStack = stack.copy();
        newStack.setDamage(stack.getDamage() + 1);
        if (newStack.getDamage() >= newStack.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return newStack;
    }
}