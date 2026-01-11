package banduty.stoneycore.items.item;

import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.tongs.ITongsHelper;
import net.minecraft.world.item.ItemStack;

public class ForgeTongsHelper implements ITongsHelper {
    @Override
    public ItemStack getTongs(ItemStack targetStack) {
        return new ItemStack(SCItems.TONGS.get());
    }
}
