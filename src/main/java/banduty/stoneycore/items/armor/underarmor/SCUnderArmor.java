package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.items.armor.ISCUnderArmor;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;

public class SCUnderArmor extends ArmorItem implements ISCUnderArmor {
    public SCUnderArmor(Settings settings, ArmorMaterial material, Type type) {
        super(material, type, settings);
    }
}