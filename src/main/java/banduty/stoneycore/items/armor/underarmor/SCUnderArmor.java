package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.items.armor.ISCUnderArmor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class SCUnderArmor extends ArmorItem implements ISCUnderArmor {
    public SCUnderArmor(Item.Properties settings, ArmorMaterial material, Type type) {
        super(material, type, settings);
    }
}