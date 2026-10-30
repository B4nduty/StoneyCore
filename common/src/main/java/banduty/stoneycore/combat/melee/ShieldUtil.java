package banduty.stoneycore.combat.melee;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.definitions.WeaponDefinitionData;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import net.minecraft.world.item.ItemStack;

/**
 * Block / parry behaviour of melee weapons, driven entirely by the weapon definition (no item tag).
 * <p>
 * A melee weapon is a "shield" (can be raised with use) when its definition has
 * {@code blockingEnabled: true} and/or a non-zero {@code parryWindow}.
 * Items without a melee definition are never shields, and every helper returns a neutral value for them.
 */
public final class ShieldUtil {
    private ShieldUtil() {}

    /** True for melee weapons whose definition allows blocking and/or parrying. */
    public static boolean isShield(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee != null && (melee.blockingEnabled() || melee.parryWindow() != 0);
    }

    /**
     * Parry window in ticks. 0 means "no parry" (global parry option off, or the weapon has no parry window).
     * A negative parryWindow in the definition means "use the global config value".
     */
    public static int getParryWindowTicks(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        if (melee == null) return 0;

        var combat = StoneyCore.getConfig().combatOptions();
        if (!combat.getParry()) return 0;

        if (melee.parryWindow() < 0) {
            return Math.max(0, (int) combat.getParryWindowTicks());
        }
        return melee.parryWindow();
    }

    /** Whether the weapon actually blocks damage after the parry window. */
    public static boolean canBlock(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee != null && melee.blockingEnabled();
    }

    /** Cooldown (ticks) applied to the item when the player stops blocking/parrying. */
    public static int getBlockCooldownTicks(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee == null ? 0 : Math.max(0, melee.blockCooldown());
    }

    /** Parry-only: blocking disabled, so it is lowered automatically when the parry window ends. */
    public static boolean isParryOnly(ItemStack stack) {
        return isShield(stack) && !canBlock(stack);
    }

    /** A shield that can neither block nor parry would do nothing, so it cannot be raised. */
    public static boolean canRaise(ItemStack stack) {
        return isShield(stack) && (canBlock(stack) || getParryWindowTicks(stack) > 0);
    }

    private static WeaponDefinitionData.MeleeData getMelee(ItemStack stack) {
        if (!WeaponDefinitionsStorage.isMelee(stack)) return null;
        return WeaponDefinitionsStorage.getData(stack).melee();
    }
}