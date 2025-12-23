package banduty.stoneycore.util.bettercombatlogic;

import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static banduty.stoneycore.util.weaponutil.SCWeaponUtil.getDamageValues;

public class SCBetterCombat {
    public static ItemStack getWeaponStack(Entity attacker, ItemStack defaultStack) {
        AttackHand hand = null;
        if (attacker instanceof Player player) {
            if (player instanceof PlayerAttackProperties props) {
                hand = PlayerAttackHelper.getCurrentAttack(player, props.getComboCount());
            }
        }
        ItemStack itemStack = defaultStack;
        if (hand != null) itemStack = hand.itemStack();
        return itemStack;
    }

    public static SCDamageCalculator.DamageType determineDamageType(ItemStack mainHandStack, WeaponDefinitionsLoader.DefinitionData weaponData, Player player) {
        Item item = mainHandStack.getItem();
        boolean isBludgeoning = NBTDataHelper.get(mainHandStack, INBTKeys.BLUDGEONING, false);
        boolean isPiercing = isPiercing((PlayerAttackProperties) player, item);

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

    private static boolean isPiercing(PlayerAttackProperties player, Item item) {
        int comboCount = player.getComboCount();
        WeaponDefinitionsLoader.DefinitionData attributeData = WeaponDefinitionsLoader.getData(item);
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
