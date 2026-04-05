package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.combat.melee.SCDamageType;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionData;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import net.minecraft.world.item.Item;

import java.util.Map;

public class SCArmorUtil {
    public static double getResistance(SCDamageType type, Item item) {
        ArmorDefinitionData attributeData = ArmorDefinitionsStorage.getData(item);
        Map<String, Double> damageValues = attributeData.damageResistance();

        return damageValues.getOrDefault(type.name(), 0d);
    }
}
