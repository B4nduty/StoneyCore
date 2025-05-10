package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.items.armor.ISCUnderArmor;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SCDyeableUnderArmor extends ArmorItem implements DyeableItem, ISCUnderArmor {
    int defaultColor;

    public SCDyeableUnderArmor(Settings settings, ArmorMaterial material, Type type, int defaultColor) {
        super(material, type, settings);
        this.defaultColor = defaultColor;
    }

    @Override
    public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt("display");
        return nbtCompound != null && nbtCompound.contains("color", 99) ? nbtCompound.getInt("color") : defaultColor;
    }
}