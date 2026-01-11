package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.items.armor.ISCUnderArmor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class SCUnderArmor extends ArmorItem implements ISCUnderArmor {
    public SCUnderArmor(Properties settings, ArmorMaterial material, Type type) {
        super(material, type, settings);
    }
}