package banduty.stoneycore.items.item;

import banduty.stoneycore.items.SmithingHammer;
import net.minecraft.world.item.ItemStack;

public class FabricSmithingHammer extends SmithingHammer {
    public FabricSmithingHammer(Properties properties) {
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