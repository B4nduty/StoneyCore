package banduty.stoneycore.items.item;

import banduty.stoneycore.items.manuscript.Manuscript;
import net.minecraft.world.item.ItemStack;

public class ForgeManuscript extends Manuscript {
    public ForgeManuscript(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }
}
