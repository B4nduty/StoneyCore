package banduty.stoneycore.combat;

import banduty.stoneycore.combat.melee.ICombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class BetterCombatHelper implements ICombatHelper {
    @Override
    public int getComboCount(Player player) {
        return ((PlayerAttackProperties) player).getComboCount();
    }

    @Override
    public ItemStack getCurrentWeaponStack(Entity attacker, ItemStack defaultStack) {
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

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isAvailable() {
        return ModList.get().isLoaded("bettercombat");
    }
}
