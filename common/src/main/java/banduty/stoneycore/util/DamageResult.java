package banduty.stoneycore.util;

import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.combat.melee.SCDamageType;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record DamageResult(double damage, SCDamageType damageType) {
    public static DamageResult calculateWeaponDamage(LivingEntity attacker, LivingEntity target, ItemStack stack) {
        int comboCount = attacker instanceof Player player ? CombatSelect.getComboCount(player) : 0;

        SCDamageType damageType = SCWeaponUtil.calculateDamageType(stack, comboCount);

        double maxDistance = SCWeaponUtil.getMaxDistance(stack.getItem());
        double actualDistance = attacker.position().distanceTo(target.position());

        if (actualDistance > maxDistance + 1) {
            return new DamageResult(0.0f, damageType);
        }

        double baseDamage = SCWeaponUtil.calculateDamage(stack.getItem(), actualDistance, damageType);
        if (stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f) {
            baseDamage *= 0.25f;
        }
        double enchantmentBonus = EnchantmentHelper.getDamageBonus(stack, target.getMobType());
        baseDamage += enchantmentBonus;

        if (stack.is(SCTags.WEAPONS_IGNORES_ARMOR.getTag())) {
            return new DamageResult(baseDamage, damageType);
        }

        double calculatedDamage = SCDamageType.calculateSCDamage(target, baseDamage, damageType);

        if (stack.is(SCTags.WEAPONS_DAMAGE_BEHIND.getTag())) {
            calculatedDamage = SCWeaponUtil.adjustDamageForBackstab(target, attacker.position(), calculatedDamage);
        }

        return new DamageResult(calculatedDamage, damageType);
    }
}
