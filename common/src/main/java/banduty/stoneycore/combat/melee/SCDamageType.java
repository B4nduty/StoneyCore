package banduty.stoneycore.combat.melee;

import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public enum SCDamageType {
    SLASHING,
    PIERCING,
    BLUDGEONING;

    public static final Codec<SCDamageType> CODEC = Codec.STRING.xmap(
            str -> SCDamageType.valueOf(str.toUpperCase()),
            SCDamageType::name
    );

    // Determination Logic
    public static SCDamageType determine(ItemStack mainHandStack, Player player) {
        Item item = mainHandStack.getItem();
        boolean isBludgeoningNBT = NBTDataHelper.get(mainHandStack, INBTKeys.BLUDGEONING, false);
        boolean isPiercingCombo = isPiercingAnimation(player, item);

        boolean bludgeoningToPiercing =
                !SCWeaponUtil.hasDamageType(SCDamageType.SLASHING, item) &&
                        SCWeaponUtil.hasDamageType(SCDamageType.PIERCING, item) &&
                        SCWeaponUtil.hasDamageType(SCDamageType.BLUDGEONING, item);

        if (isBludgeoningNBT || SCWeaponUtil.isOnlyDamageType(SCDamageType.BLUDGEONING, item)) {
            return SCDamageType.BLUDGEONING;
        }

        if (isPiercingCombo || bludgeoningToPiercing || SCWeaponUtil.isOnlyDamageType(SCDamageType.PIERCING, item)) {
            return SCDamageType.PIERCING;
        }

        return SCDamageType.SLASHING;
    }

    private static boolean isPiercingAnimation(Player player, Item item) {
        int comboCount = CombatSelect.getComboCount(player);
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        int[] piercingAnimations = attributeData.melee().piercingAnimation();
        int animationTotal = attributeData.melee().animation();

        if (animationTotal > 0) {
            for (int piercingIdx : piercingAnimations) {
                if (comboCount % animationTotal == piercingIdx - 1) {
                    return true;
                }
            }
            return piercingAnimations.length == animationTotal;
        }
        return false;
    }

    // Calculation Logic
    public static double calculateSCDamage(LivingEntity target, double initialDamage, SCDamageType type) {
        for (ItemStack armorStack : target.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(armorStack.getItem())) {
                double resistance = getResistance(armorStack.getItem(), type);
                initialDamage *= Math.max(1 - resistance, 0);
            }
        }
        return initialDamage;
    }

    public static double getResistance(Item item, SCDamageType damageType) {
        if (damageType == null) {
            return SCArmorUtil.getResistance(BLUDGEONING, item);
        }
        return switch (damageType) {
            case SLASHING -> SCArmorUtil.getResistance(SLASHING, item);
            case PIERCING -> SCArmorUtil.getResistance(PIERCING, item);
            case BLUDGEONING -> SCArmorUtil.getResistance(BLUDGEONING, item);
        };
    }

    public static void apply(LivingEntity target, Entity attacker, ItemStack stack, double damage) {
        if (attacker == null) return;
        float enchantmentBonus = EnchantmentHelper.getDamageBonus(stack, target.getMobType());
        damage += enchantmentBonus;

        if (stack.is(SCTags.WEAPONS_IGNORES_ARMOR.getTag()) && target.getHealth() - damage > 0) {
            target.setHealth((float) (target.getHealth() - damage));
        } else {
            if (attacker instanceof Player player) {
                target.hurt(attacker.level().damageSources().playerAttack(player), (float) damage);
            } else if (attacker instanceof LivingEntity living) {
                target.hurt(attacker.level().damageSources().mobAttack(living), (float) damage);
            } else if (attacker instanceof AbstractArrow arrow) {
                target.hurt(attacker.level().damageSources().arrow(arrow, arrow.getOwner()), (float) damage);
            }
        }
    }
}