package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SCDamageCalculator {
    public static double applyArmor(LivingEntity target, double damage, SCDamageType type) {
        for (ItemStack armorStack : target.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack.getItem())) {
                double resistance = getResistance(armorStack.getItem(), type);
                damage *= Math.max(1 - resistance, 0);
            }
        }
        return damage;
    }

    public static double getResistance(Item item, SCDamageType type) {
        if (type == null) {
            return SCArmorUtil.getResistance(SCDamageType.BLUDGEONING, item);
        }

        return switch (type) {
            case SLASHING -> SCArmorUtil.getResistance(SCDamageType.SLASHING, item);
            case PIERCING -> SCArmorUtil.getResistance(SCDamageType.PIERCING, item);
            case BLUDGEONING -> SCArmorUtil.getResistance(SCDamageType.BLUDGEONING, item);
        };
    }
}