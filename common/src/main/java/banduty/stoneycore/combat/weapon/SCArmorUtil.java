package banduty.stoneycore.combat.weapon;

import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.data.SCAttributes;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;

public class SCArmorUtil {
    public static Map<SCDamageType, Double> getDeflectChances(LivingEntity entity) {
        Map<SCDamageType, Double> chances = new EnumMap<>(SCDamageType.class);
        for (SCDamageType type : SCDamageType.values()) {
            chances.put(type, entity.getAttributeValue(SCAttributes.deflectChance(type)));
        }
        return chances;
    }

    public static double getPiercingDeflect(LivingEntity entity) {
        return entity.getAttributeValue(SCAttributes.PIERCING_DEFLECT_CHANCE);
    }
}