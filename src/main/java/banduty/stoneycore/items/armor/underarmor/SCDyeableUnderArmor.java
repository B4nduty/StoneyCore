package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.items.armor.ISCUnderArmor;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableItem;

public class SCDyeableUnderArmor extends ArmorItem implements DyeableItem, ISCUnderArmor {
    int defaultColor;

    public SCDyeableUnderArmor(Settings settings, ArmorMaterial material, Type type, int defaultColor) {
        super(material, type, settings);
        this.defaultColor = defaultColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }
}