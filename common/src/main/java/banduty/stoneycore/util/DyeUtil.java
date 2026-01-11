package banduty.stoneycore.util;

import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class DyeUtil {
    public static float[] getFloatDyeColor(ItemStack stack) {
        if (!(stack.getItem() instanceof DyeableLeatherItem dyeableItem)) {
            return new float[]{1, 1, 1};
        }
        int color = dyeableItem.getColor(stack);
        return new float[]{(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F};
    }
}