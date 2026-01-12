package banduty.stoneycore.items.item;

import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.manuscript.IManuscriptHelper;
import net.minecraft.world.item.ItemStack;

public class FabricManuscriptHelper implements IManuscriptHelper {
    @Override
    public ItemStack getManuscript(ItemStack targetStack) {
        return new ItemStack(SCItems.MANUSCRIPT);
    }
}
