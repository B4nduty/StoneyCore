package banduty.stoneycore.items.item;

import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.hotiron.IHotIronHelper;
import net.minecraft.world.item.ItemStack;

public class FabricHotIronHelper implements IHotIronHelper {
    @Override
    public ItemStack getHotIron(ItemStack targetStack) {
        return new ItemStack(SCItems.HOT_IRON.get());
    }
}
