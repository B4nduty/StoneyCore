package banduty.stoneycore.items.item;

import banduty.stoneycore.items.tongs.Tongs;
import net.minecraft.world.item.ItemStack;

public class FabricTongs extends Tongs {
    public FabricTongs(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack.copy();
    }
}
