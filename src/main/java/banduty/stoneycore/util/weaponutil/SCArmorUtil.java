package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import net.minecraft.item.Item;

import java.util.Map;

public class SCArmorUtil {
    public static double getResistance(SCDamageCalculator.DamageType type, Item item) {
        return switch (type) {
            case SLASHING -> getDamageResistances(SCDamageCalculator.DamageType.SLASHING.name(), item);
            case BLUDGEONING -> getDamageResistances(SCDamageCalculator.DamageType.BLUDGEONING.name(), item);
            case PIERCING -> getDamageResistances(SCDamageCalculator.DamageType.PIERCING.name(), item);
        };
    }

    public static double getDamageResistances(String key, Item item) {
        SCArmorDefinitionsLoader.DefinitionData attributeData = SCArmorDefinitionsLoader.getData(item);
        Map<String, Double> damageValues = attributeData.damageResistance();

        return damageValues.getOrDefault(key, 0d);
    }
}
