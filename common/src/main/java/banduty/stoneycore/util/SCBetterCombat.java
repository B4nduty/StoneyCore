package banduty.stoneycore.util;

import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static banduty.stoneycore.util.weaponutil.SCWeaponUtil.getDamageValues;

public class SCBetterCombat {
    public static ItemStack getWeaponStack(Entity attacker, ItemStack defaultStack) {
        return Services.PLATFORM.getWeaponStack(attacker, defaultStack);
    }

    public static SCDamageCalculator.DamageType determineDamageType(ItemStack mainHandStack, WeaponDefinitionData weaponData, Player player) {
        Item item = mainHandStack.getItem();
        boolean isBludgeoning = NBTDataHelper.get(mainHandStack, INBTKeys.BLUDGEONING, false);
        boolean isPiercing = isPiercing(player, item);

        boolean bludgeoningToPiercing =
                getDamageValues(SCDamageCalculator.DamageType.SLASHING, item) == 0 &&
                        getDamageValues(SCDamageCalculator.DamageType.PIERCING, item) > 0 &&
                        getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING, item) > 0;

        SCDamageCalculator.DamageType onlyType = weaponData.melee().onlyDamageType();

        if (isBludgeoning || onlyType == SCDamageCalculator.DamageType.BLUDGEONING) {
            return SCDamageCalculator.DamageType.BLUDGEONING;
        }

        if (isPiercing || bludgeoningToPiercing || onlyType == SCDamageCalculator.DamageType.PIERCING) {
            return SCDamageCalculator.DamageType.PIERCING;
        }

        return SCDamageCalculator.DamageType.SLASHING;
    }

    private static boolean isPiercing(Player player, Item item) {
        int comboCount = Services.PLATFORM.comboCount(player);
        WeaponDefinitionData attributeData = WeaponDefinitionsStorage.getData(item);
        int[] piercingAnimations = attributeData.melee().piercingAnimation();
        int animation = attributeData.melee().animation();

        if (animation > 0) {
            for (int piercingAnimation : piercingAnimations) {
                if (comboCount % animation == piercingAnimation - 1) {
                    return true;
                }
            }
            return piercingAnimations.length == animation;
        }
        return false;
    }
}
