package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SCDamageTypeResolver {
    public static SCDamageType determine(ItemStack stack, Player player) {
        Item item = stack.getItem();

        boolean isBludgeoningNBT = Boolean.TRUE.equals(stack.get(SCDataComponents.BLUDGEONING.get()));
        boolean isPiercingCombo = isPiercingAnimation(player, item);

        boolean bludgeoningToPiercing =
                !SCWeaponUtil.hasDamageType(SCDamageType.SLASHING, item) &&
                        SCWeaponUtil.hasDamageType(SCDamageType.PIERCING, item) &&
                        SCWeaponUtil.hasDamageType(SCDamageType.BLUDGEONING, item);

        if (isBludgeoningNBT || SCWeaponUtil.isOnlyDamageType(SCDamageType.BLUDGEONING, item)) {
            return SCDamageType.BLUDGEONING;
        }

        if (isPiercingCombo || bludgeoningToPiercing ||
                SCWeaponUtil.isOnlyDamageType(SCDamageType.PIERCING, item)) {
            return SCDamageType.PIERCING;
        }

        return SCDamageType.SLASHING;
    }

    private static boolean isPiercingAnimation(Player player, Item item) {
        int comboCount = CombatSelect.getComboCount(player);
        WeaponDefinitionData data = WeaponDefinitionsStorage.getData(item);

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