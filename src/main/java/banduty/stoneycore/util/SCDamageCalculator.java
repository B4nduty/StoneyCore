package banduty.stoneycore.util;

import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SCDamageCalculator {
    public static float getSCDamage(LivingEntity livingEntity, float initialDamage, DamageType damageType) {
        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (SCArmorDefinitionsLoader.containsItem(armorStack.getItem())) {
                float resistance = (float) getResistance(armorStack.getItem(), damageType);
                initialDamage *= Math.max(1 - resistance, 0);
            }
        }
        return initialDamage;
    }

    protected static double getResistance(Item item, DamageType damageType) {
        if (damageType == null) {
            System.err.println("[SCDamageCalculator] Error: DamageType is null for item " + item.getName().getString() + ", changing to default Bludgeoning");
            return SCArmorUtil.getResistance(SCDamageCalculator.DamageType.BLUDGEONING, item);
        }
        return switch (damageType) {
            case SLASHING -> SCArmorUtil.getResistance(SCDamageCalculator.DamageType.SLASHING, item);
            case PIERCING -> SCArmorUtil.getResistance(SCDamageCalculator.DamageType.PIERCING, item);
            case BLUDGEONING -> SCArmorUtil.getResistance(SCDamageCalculator.DamageType.BLUDGEONING, item);
        };
    }

    public static void applyDamage(LivingEntity target, LivingEntity attacker, ItemStack stack, float damage) {
        float enchantmentBonusDamage = EnchantmentHelper.getAttackDamage(stack, target.getGroup());
        damage += enchantmentBonusDamage;
        if (stack.isIn(SCTags.WEAPONS_IGNORES_ARMOR.getTag()) && target.getHealth() - (damage - 1) > 0) {
            target.setHealth(target.getHealth() - (damage - 1));
        } else {
            if (attacker instanceof PlayerEntity player) target.damage(attacker.getWorld().getDamageSources().playerAttack(player), damage - 1);
            else target.damage(attacker.getWorld().getDamageSources().mobAttack(attacker), damage - 1);
        }
    }

    public enum DamageType {
        SLASHING, PIERCING, BLUDGEONING
    }
}