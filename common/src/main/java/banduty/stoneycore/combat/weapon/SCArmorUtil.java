package banduty.stoneycore.combat.weapon;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.definitions.ArmorDefinitionData;
import banduty.stoneycore.definitions.ArmorDefinitionsStorage;
import net.minecraft.world.item.Item;

import java.util.Map;

public class SCArmorUtil {
    public static double getResistance(SCDamageType type, Item item) {
        ArmorDefinitionData attributeData = ArmorDefinitionsStorage.getData(item);
        Map<String, Double> damageValues = attributeData.damageResistance();

        return damageValues.getOrDefault(type.name(), 0d);
    }
}
