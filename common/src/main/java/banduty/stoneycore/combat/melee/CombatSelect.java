package banduty.stoneycore.combat.melee;

import banduty.stoneycore.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CombatSelect {
    public static int getComboCount(Player player) {
        return Services.COMBAT.getComboCount(player);
    }

    public static ItemStack getWeaponStack(Entity attacker, ItemStack defaultStack) {
        return Services.COMBAT.getCurrentWeaponStack(attacker, defaultStack);
    }
}
