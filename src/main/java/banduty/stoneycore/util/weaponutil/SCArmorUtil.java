package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import net.minecraft.item.Item;

import java.util.Map;

public class SCArmorUtil {
    public static double getResistance(SCDamageCalculator.DamageType type, Item item) {
        ArmorDefinitionsLoader.DefinitionData attributeData = ArmorDefinitionsLoader.getData(item);
        Map<String, Double> damageValues = attributeData.damageResistance();

        return damageValues.getOrDefault(type.name(), 0d);
    }
}
