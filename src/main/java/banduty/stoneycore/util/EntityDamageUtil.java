package banduty.stoneycore.util;

import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class EntityDamageUtil {
    public static SCDamageCalculator.DamageType damageType;

    public static double onDamage(LivingEntity target, LivingEntity attacker, ItemStack weaponStack, CallbackInfo ci) {
        double amount = 0;

        if (!(target.getWorld() instanceof ServerWorld)) {
            ci.cancel();
            return 0;
        }

        if (!weaponStack.isEmpty()) {
            DamageResult damageResult = DamageResult.calculateWeaponDamage(attacker, target, weaponStack);

            damageType = damageResult.damageType();
            amount += damageResult.damage();
        }

        return Math.max(amount, 0.0F);
    }

    public static ItemStack getWeaponStack(Entity attacker) {
        AttackHand hand = null;
        if (attacker instanceof PlayerEntity player) {
            if (player instanceof PlayerAttackProperties props) {
                hand = PlayerAttackHelper.getCurrentAttack(player, props.getComboCount());
            }
        }
        ItemStack itemStack = ItemStack.EMPTY;
        if (hand != null) itemStack = hand.itemStack();
        return itemStack;
    }
}
