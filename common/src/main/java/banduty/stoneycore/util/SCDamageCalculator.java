package banduty.stoneycore.util;

import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SCDamageCalculator {
    public static double getSCDamage(LivingEntity livingEntity, double initialDamage, DamageType damageType) {
        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack.getItem())) {
                double resistance = getResistance(armorStack.getItem(), damageType);
                initialDamage *= Math.max(1 - resistance, 0);
            }
        }
        return initialDamage;
    }

    protected static double getResistance(Item item, DamageType damageType) {
        if (damageType == null) {
            System.err.println("[SCDamageCalculator] Error: DamageType is null for item " + item.getDescriptionId() + ", changing to default Bludgeoning");
            return SCArmorUtil.getResistance(DamageType.BLUDGEONING, item);
        }
        return switch (damageType) {
            case SLASHING -> SCArmorUtil.getResistance(DamageType.SLASHING, item);
            case PIERCING -> SCArmorUtil.getResistance(DamageType.PIERCING, item);
            case BLUDGEONING -> SCArmorUtil.getResistance(DamageType.BLUDGEONING, item);
        };
    }

    public static void applyDamage(LivingEntity target, Entity attacker, ItemStack stack, double damage) {
        if (attacker == null) return;
        float enchantmentBonusDamage = EnchantmentHelper.getDamageBonus(stack, target.getMobType());
        damage += enchantmentBonusDamage;
        if (stack.is(SCTags.WEAPONS_IGNORES_ARMOR.getTag()) && target.getHealth() - damage  > 0) {
            target.setHealth((float) (target.getHealth() - damage));
        } else {
            if (attacker instanceof Player player) target.hurt(attacker.level().damageSources().playerAttack(player), (float) damage);
            else if (attacker instanceof LivingEntity livingEntity) target.hurt(attacker.level().damageSources().mobAttack(livingEntity), (float) damage);
            else if (attacker instanceof AbstractArrow abstractArrow) target.hurt(attacker.level().damageSources().arrow(abstractArrow, abstractArrow.getOwner()), (float) damage);
        }
    }

    public enum DamageType {
        SLASHING, PIERCING, BLUDGEONING;

        public static final Codec<DamageType> CODEC = Codec.STRING.xmap(
                str -> DamageType.valueOf(str.toUpperCase()),
                DamageType::name
        );
    }
}