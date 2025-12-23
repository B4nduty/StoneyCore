package banduty.stoneycore.items.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SmithingHammer extends Item {
    public SmithingHammer(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        ItemStack newStack = stack.copy();
        newStack.setDamageValue(stack.getDamageValue() + 1);
        if (newStack.getDamageValue() >= newStack.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return newStack;
    }
}