package banduty.stoneycore.util;

import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

public class EntityDamageUtil {
    public static SCDamageCalculator.DamageType damageType;

    public static double onDamage(LivingEntity target, LivingEntity attacker, ItemStack weaponStack) {
        if (!(target.getWorld() instanceof ServerWorld)) {
            return 0;
        }

        double amount = 0;
        if (!weaponStack.isEmpty()) {
            DamageResult result = DamageResult.calculateWeaponDamage(attacker, target, weaponStack);
            damageType = result.damageType();
            amount = result.damage();
        }

        return Math.max(amount, 0.0);
    }

    public static ItemStack getWeaponStack(Entity attacker) {
        if (attacker instanceof PlayerEntity player) {
            if (player instanceof PlayerAttackProperties props) {
                AttackHand hand = PlayerAttackHelper.getCurrentAttack(player, props.getComboCount());
                if (hand != null) {
                    return hand.itemStack();
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
