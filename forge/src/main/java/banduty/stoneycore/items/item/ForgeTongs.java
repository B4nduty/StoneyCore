package banduty.stoneycore.items.item;

import banduty.stoneycore.items.tongs.Tongs;
import net.minecraft.world.item.ItemStack;

public class ForgeTongs extends Tongs {
    public ForgeTongs(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }
}
