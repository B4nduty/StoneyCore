package banduty.stoneycore.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class DyeUtil {
    public static float[] getFloatDyeColor(ItemStack stack) {
        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);

        if (dyedColor == null) {
            return new float[]{1.0F, 1.0F, 1.0F};
        }

        int color = dyedColor.rgb();
        return new float[]{
                (float) (color >> 16 & 255) / 255.0F,
                (float) (color >> 8 & 255) / 255.0F,
                (float) (color & 255) / 255.0F
        };
    }

    public static int getDyeColorInt(ItemStack stack) {
        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);

        if (dyedColor == null) {
            return 0xFFFFFF;
        }

        return dyedColor.rgb();
    }

    public static int getDyeColorARGB(ItemStack stack) {
        int rgb = getDyeColorInt(stack);

        return FastColor.ARGB32.color(
                255,
                (rgb >> 16 & 255),
                (rgb >> 8 & 255),
                (rgb & 255)
        );
    }
}