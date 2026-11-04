package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.data.SCDataComponents;
import banduty.stoneycore.definitions.WeaponDefinitionData;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import banduty.stoneycore.combat.weapon.SCWeaponUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SCDamageTypeResolver {
    public static SCDamageType determine(ItemStack stack, Player player) {
        boolean isBludgeoningNBT = Boolean.TRUE.equals(stack.get(SCDataComponents.BLUDGEONING.get()));
        boolean isPiercingCombo = isPiercingAnimation(player, stack);

        boolean bludgeoningToPiercing =
                !SCWeaponUtil.hasDamageType(SCDamageType.SLASHING, stack) &&
                        SCWeaponUtil.hasDamageType(SCDamageType.PIERCING, stack) &&
                        SCWeaponUtil.hasDamageType(SCDamageType.BLUDGEONING, stack);

        if (isBludgeoningNBT || SCWeaponUtil.isOnlyDamageType(SCDamageType.BLUDGEONING, stack)) {
            return SCDamageType.BLUDGEONING;
        }

        if (isPiercingCombo || bludgeoningToPiercing ||
                SCWeaponUtil.isOnlyDamageType(SCDamageType.PIERCING, stack)) {
            return SCDamageType.PIERCING;
        }

        return SCDamageType.SLASHING;
    }

    private static boolean isPiercingAnimation(Player player, ItemStack stack) {
        int comboCount = CombatSelect.getComboCount(player);
        WeaponDefinitionData data = WeaponDefinitionsStorage.getData(stack);
        if (data.melee() == null) return false;

        int[] piercingAnimations = data.melee().piercingAnimation();
        int animationTotal = data.melee().animation();

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
}