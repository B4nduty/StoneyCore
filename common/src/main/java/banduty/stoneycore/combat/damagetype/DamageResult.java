package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.combat.weapon.SCWeaponUtil;
import banduty.stoneycore.data.SCTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
        if (stack.is(SCTags.Item.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f) {
            baseDamage *= 0.25f;
        }

        // Vanilla enchantments scale the custom (slashing/piercing/bludgeoning) damage before armor resistances.
        // The vanilla enchantment pass in Player#attack / Mob#doHurtTarget is skipped for these weapons (see
        // PlayerMixin / MobMixin) so enchantments are only counted once.
        DamageSource source = attacker instanceof Player player
                ? attacker.damageSources().playerAttack(player)
                : attacker.damageSources().mobAttack(attacker);
        if (attacker.level() instanceof ServerLevel serverLevel) {
            baseDamage = SCDamageCalculator.applyEnchantments(serverLevel, stack, target, source, baseDamage);
        }

        if (stack.is(SCTags.Item.WEAPONS_IGNORES_ARMOR.getTag())) {
            return new DamageResult(baseDamage, damageType);
        }

        double calculatedDamage = SCDamageCalculator.applyArmor(target, baseDamage, damageType, source);

        if (stack.is(SCTags.Item.WEAPONS_DAMAGE_BEHIND.getTag())) {
            calculatedDamage = SCWeaponUtil.adjustDamageForBackstab(target, attacker.position(), calculatedDamage);
        }

        return new DamageResult(calculatedDamage, damageType);
    }
}
