package banduty.stoneycore.util;

import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public record DamageResult(double damage, SCDamageCalculator.DamageType damageType) {
    public static DamageResult calculateWeaponDamage(LivingEntity attacker, LivingEntity target, ItemStack stack) {
        int comboCount = attacker instanceof PlayerEntity player ?
                ((PlayerAttackProperties) player).getComboCount() : 0;

        SCDamageCalculator.DamageType damageType = SCWeaponUtil.calculateDamageType(stack, comboCount);

        double maxDistance = SCWeaponUtil.getMaxDistance(stack.getItem());
        double actualDistance = attacker.getPos().distanceTo(target.getPos());

        if (actualDistance > maxDistance + 1) {
            return new DamageResult(0.0f, damageType);
        }

        double baseDamage = SCWeaponUtil.calculateDamage(stack.getItem(), actualDistance, damageType);
        double enchantmentBonus = EnchantmentHelper.getAttackDamage(stack, target.getGroup());
        baseDamage += enchantmentBonus;

        if (stack.isIn(SCTags.WEAPONS_IGNORES_ARMOR.getTag())) {
            return new DamageResult(baseDamage, damageType);
        }

        double calculatedDamage = SCDamageCalculator.getSCDamage(target, baseDamage, damageType);

        if (stack.isIn(SCTags.WEAPONS_DAMAGE_BEHIND.getTag())) {
            calculatedDamage = SCWeaponUtil.adjustDamageForBackstab(target, attacker.getPos(), calculatedDamage);
        }

        return new DamageResult(calculatedDamage, damageType);
    }
}
