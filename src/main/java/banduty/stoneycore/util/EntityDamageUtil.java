package banduty.stoneycore.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EntityDamageUtil {
    public static SCDamageCalculator.DamageType damageType;

    public static double onDamage(LivingEntity target, LivingEntity attacker, ItemStack weaponStack) {
        if (!(target.level() instanceof ServerLevel)) {
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
}
