package banduty.stoneycore.util;

import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SCDamageCalculator {
    public static double getSCDamage(LivingEntity livingEntity, double initialDamage, DamageType damageType) {
        for (ItemStack armorStack : livingEntity.getArmorItems()) {
            if (ArmorDefinitionsLoader.containsItem(armorStack.getItem())) {
                double resistance = getResistance(armorStack.getItem(), damageType);
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

    public static void applyDamage(LivingEntity target, Entity attacker, ItemStack stack, double damage) {
        if (attacker == null) return;
        float enchantmentBonusDamage = EnchantmentHelper.getAttackDamage(stack, target.getGroup());
        damage += enchantmentBonusDamage;
        if (stack.isIn(SCTags.WEAPONS_IGNORES_ARMOR.getTag()) && target.getHealth() - damage  > 0) {
            target.setHealth((float) (target.getHealth() - damage));
        } else {
            if (attacker instanceof PlayerEntity player) target.damage(attacker.getWorld().getDamageSources().playerAttack(player), (float) damage);
            else if (attacker instanceof LivingEntity livingEntity) target.damage(attacker.getWorld().getDamageSources().mobAttack(livingEntity), (float) damage);
            else if (attacker instanceof PersistentProjectileEntity persistentProjectileEntity) target.damage(attacker.getWorld().getDamageSources().arrow(persistentProjectileEntity, persistentProjectileEntity.getOwner()), (float) damage);
        }
    }

    public enum DamageType {
        SLASHING, PIERCING, BLUDGEONING
    }
}