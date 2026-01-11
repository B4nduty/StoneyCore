package banduty.stoneycore.items.item;

import banduty.stoneycore.items.manuscript.Manuscript;
import net.minecraft.world.item.ItemStack;

public class FabricManuscript extends Manuscript {
    public FabricManuscript(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack.copy();
    }
}
