package banduty.stoneycore.combat.melee;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICombatHelper {
    default int getComboCount(Player player) {
       return 0;
    }
    default ItemStack getCurrentWeaponStack(Entity attacker, ItemStack defaultStack) {
        return defaultStack;
    }
    default int getPriority() {
        return 0;
    }
    default boolean isAvailable() {
        return true;
    }
}