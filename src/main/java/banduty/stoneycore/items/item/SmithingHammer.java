package banduty.stoneycore.items.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

public class SmithingHammer extends Item {
    public SmithingHammer(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        ItemStack newStack = stack.copy();
        if (newStack.damage(1, Random.create(), null)) {
            newStack.decrement(1);
            newStack.setDamage(0);
        }
        return newStack;
    }
}