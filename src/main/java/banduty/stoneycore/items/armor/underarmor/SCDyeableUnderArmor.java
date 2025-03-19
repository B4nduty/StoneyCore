package banduty.stoneycore.items.armor.underarmor;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableItem;

public class SCDyeableUnderArmor extends SCUnderArmor implements DyeableItem {
    int defaultColor;

    public SCDyeableUnderArmor(Settings settings, ArmorMaterial material, Type type,
                               double slashingResistance, double bludgeoningResistance, double piercingResistance,
                               int defaultColor) {
        super(settings, material, type, slashingResistance, bludgeoningResistance, piercingResistance);
        this.defaultColor = defaultColor;
    }

    @Override
    public int getDefaultColor() {
        return defaultColor;
    }
}
