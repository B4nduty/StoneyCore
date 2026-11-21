package banduty.stoneycore.combat.melee;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.definitions.WeaponDefinitionData;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import banduty.stoneycore.effect.SCEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ShieldUtil {
    private ShieldUtil() {}

    public static boolean isShield(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee != null && (melee.blockingEnabled() || melee.parryWindow() != 0);
    }

    public static int getParryWindowTicks(ItemStack stack, LivingEntity livingEntity) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        if (melee == null) return 0;

        var combat = StoneyCore.getConfig().combatOptions();
        if (!combat.getParry()) return 0;

        int parryWindow = melee.parryWindow();

        if (parryWindow < 0) {
            parryWindow = combat.getParryWindowTicks();
        }

        if (livingEntity.hasEffect(SCEffects.WINDED)) {
            int level = livingEntity.getEffect(SCEffects.WINDED).getAmplifier();
            parryWindow -= level + 1;
        }
        return Math.max(0, parryWindow);
    }

    public static boolean canBlock(ItemStack stack) {
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        return melee != null && melee.blockingEnabled();
    }

    public static int getBlockCooldownTicks(Player player, ItemStack stack) {
        if (player.isCreative()) return 0;
        WeaponDefinitionData.MeleeData melee = getMelee(stack);
        if (melee == null) return 0;
        int blockCooldown = melee.blockCooldown();
        if (player.hasEffect(SCEffects.WINDED)) blockCooldown += 20 * (player.getEffect(SCEffects.WINDED).getAmplifier() + 1);
        return Math.max(0, blockCooldown);
    }

    public static boolean isParryOnly(ItemStack stack) {
        return isShield(stack) && !canBlock(stack);
    }

    public static boolean canRaise(ItemStack stack, LivingEntity livingEntity) {
        return isShield(stack) && (canBlock(stack) || getParryWindowTicks(stack, livingEntity) > 0);
    }

    private static WeaponDefinitionData.MeleeData getMelee(ItemStack stack) {
        if (!WeaponDefinitionsStorage.isMelee(stack)) return null;
        return WeaponDefinitionsStorage.getData(stack).melee();
    }
}