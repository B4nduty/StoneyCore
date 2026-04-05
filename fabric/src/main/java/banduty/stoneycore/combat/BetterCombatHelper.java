package banduty.stoneycore.combat;

import banduty.stoneycore.combat.melee.ICombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BetterCombatHelper implements ICombatHelper {
    @Override
    public int getComboCount(Player player) {
        return ((PlayerAttackProperties) player).getComboCount();
    }

    @Override
    public ItemStack getCurrentWeaponStack(Entity attacker, ItemStack defaultStack) {
        if (!(attacker instanceof Player player)) return defaultStack;

        AttackHand hand = null;
        if (player instanceof PlayerAttackProperties props) {
            hand = PlayerAttackHelper.getCurrentAttack(player, props.getComboCount());
        }

        return hand != null ? hand.itemStack() : defaultStack;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("bettercombat");
    }
}