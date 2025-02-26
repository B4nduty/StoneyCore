package banduty.stoneycore.util;

import banduty.stoneycore.items.armor.SCUnderArmorItem;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class SCDamageCalculator {
    public static float getSCDamage(LivingEntity livingEntity, float initialDamage, DamageType damageType) {
        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (armorStack.getItem() instanceof SCUnderArmorItem scUnderArmorItem) {
                float resistance = (float) getResistance(scUnderArmorItem, damageType);
                initialDamage *= Math.max(1 - resistance, 0);
            }
        }
        return initialDamage;
    }

    protected static double getResistance(SCUnderArmorItem scUnderArmorItem, DamageType damageType) {
        return switch (damageType) {
            case SLASHING -> SCArmorUtil.getResistance(SCArmorUtil.ResistanceType.SLASHING, scUnderArmorItem);
            case PIERCING -> SCArmorUtil.getResistance(SCArmorUtil.ResistanceType.PIERCING, scUnderArmorItem);
            case BLUDGEONING -> SCArmorUtil.getResistance(SCArmorUtil.ResistanceType.BLUDGEONING, scUnderArmorItem);
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
        SLASHING("slashing", 0),
        PIERCING("piercing", 1),
        BLUDGEONING("bludgeoning", 2);

        private final String name;
        private final int index;

        DamageType(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public String getName() {
            return this.name;
        }

        public int getIndex() {
            return this.index;
        }
    }
}