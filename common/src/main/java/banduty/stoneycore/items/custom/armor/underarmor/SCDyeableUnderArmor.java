package banduty.stoneycore.items.custom.armor.underarmor;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

public class SCDyeableUnderArmor extends SCUnderArmor {
    private final int defaultColor;

    public SCDyeableUnderArmor(Holder<ArmorMaterial> material, Type type, Properties properties, int defaultColor) {
        super(material, type, properties);
        this.defaultColor = defaultColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }
}