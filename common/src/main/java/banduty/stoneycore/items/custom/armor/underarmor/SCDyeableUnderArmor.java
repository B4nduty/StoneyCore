package banduty.stoneycore.items.custom.armor.underarmor;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class SCDyeableUnderArmor extends SCUnderArmor {
    private final int defaultColor;

    public SCDyeableUnderArmor(Holder<ArmorMaterial> material, Type type, Properties properties, int defaultColor) {
        super(material, type, properties);
        this.defaultColor = defaultColor;
    }

    public int getColor(ItemStack stack) {
        DyedItemColor dyedItemColor = stack.get(DataComponents.DYED_COLOR);
        return dyedItemColor != null ? dyedItemColor.rgb() : this.defaultColor;
    }

    public boolean hasColor(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    public void clearColor(ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
    }

    public void setColor(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
    }
}