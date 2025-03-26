package banduty.stoneycore.util;

import banduty.stoneycore.util.definitionsloader.SCUnderArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SCDamageCalculator {
    public static float getSCDamage(LivingEntity livingEntity, float initialDamage, DamageType damageType) {
        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (armorStack.getItem() instanceof ArmorItem armorItem && SCUnderArmorDefinitionsLoader.containsItem(armorItem)) {
                float resistance = (float) getResistance(armorItem, damageType);
                initialDamage *= Math.max(1 - resistance, 0);
            }
        }
        return initialDamage;
    }

    protected static double getResistance(Item item, DamageType damageType) {
        return switch (damageType) {
            case SLASHING -> SCArmorUtil.getResistance(SCDamageCalculator.DamageType.SLASHING, item);
            case PIERCING -> SCArmorUtil.getResistance(SCDamageCalculator.DamageType.PIERCING, item);
            case BLUDGEONING -> SCArmorUtil.getResistance(SCDamageCalculator.DamageType.BLUDGEONING, item);
        } * 100;
    }

    public static void applyDamage(LivingEntity target, PlayerEntity playerEntity, ItemStack stack, float damage) {
        float enchantmentBonusDamage = EnchantmentHelper.getAttackDamage(stack, target.getGroup());
        damage += enchantmentBonusDamage;
        if (stack.isIn(SCTags.WEAPONS_IGNORES_ARMOR.getTag()) && target.getHealth() - (damage - 1) > 0) {
            target.setHealth(target.getHealth() - (damage - 1));
        } else {
            target.damage(playerEntity.getWorld().getDamageSources().playerAttack(playerEntity), damage - 1);
        }
    }

    public enum DamageType {
        SLASHING, PIERCING, BLUDGEONING
    }
}