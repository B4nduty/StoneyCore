package banduty.stoneycore.combat.melee;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.definitions.WeaponDefinitionData;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ShieldUtil {
    private ShieldUtil() {}

    public static boolean isShield(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee != null && (melee.blockingEnabled() || melee.parryWindow() != 0);
    }

    public static int getParryWindowTicks(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        if (melee == null) return 0;

        var combat = StoneyCore.getConfig().combatOptions();
        if (!combat.getParry()) return 0;

        if (melee.parryWindow() < 0) {
            return Math.max(0, combat.getParryWindowTicks());
        }
        return melee.parryWindow();
    }

    public static boolean canBlock(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee != null && melee.blockingEnabled();
    }

    public static int getBlockCooldownTicks(Player player, ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        if (player.isCreative()) return 0;
        return melee == null ? 0 : Math.max(0, melee.blockCooldown());
    }

    public static int getBlockCooldownTicks(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee == null ? 0 : Math.max(0, melee.blockCooldown());
    }

    public static boolean isParryOnly(ItemStack stack) {
        return isShield(stack) && !canBlock(stack);
    }

    public static boolean canRaise(ItemStack stack) {
        return isShield(stack) && (canBlock(stack) || getParryWindowTicks(stack) > 0);
    }

    private static WeaponDefinitionData.MeleeData getMelee(ItemStack stack) {
        if (!WeaponDefinitionsStorage.isMelee(stack)) return null;
        return WeaponDefinitionsStorage.getData(stack).melee();
    }
}