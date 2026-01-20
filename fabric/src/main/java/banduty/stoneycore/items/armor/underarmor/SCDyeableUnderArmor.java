package banduty.stoneycore.items.armor.underarmor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class SCDyeableUnderArmor extends SCUnderArmor implements DyeableLeatherItem {
    public SCDyeableUnderArmor(Properties properties, ArmorMaterial material, Type type, int defaultColor) {
        super(properties, material, type);
        this.defaultColor = defaultColor;
    }

    int defaultColor;

    @Override
    public int getColor(ItemStack stack) {
        CompoundTag nbtCompound = stack.getTagElement("display");
        return nbtCompound != null && nbtCompound.contains("color", 99) ? nbtCompound.getInt("color") : defaultColor;
    }
}