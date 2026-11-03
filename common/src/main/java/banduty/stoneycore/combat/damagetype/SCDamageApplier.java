package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.data.SCTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class SCDamageApplier {
    public static void apply(LivingEntity target, Entity attacker, ItemStack stack, double damage) {
        // Enchantments are applied by the caller (SCDamageCalculator#applyEnchantments) before armor resistances.
        if (attacker == null || !(attacker.level() instanceof ServerLevel serverLevel)) return;

        if (stack.is(SCTags.Item.WEAPONS_IGNORES_ARMOR.getTag()) &&
                target.getHealth() - damage > 0) {

            target.setHealth((float) (target.getHealth() - damage));
            return;
        }

        switch (attacker) {
            case Player player ->
                    target.hurt(serverLevel.damageSources().playerAttack(player), (float) damage);

            case LivingEntity living ->
                    target.hurt(serverLevel.damageSources().mobAttack(living), (float) damage);

            case AbstractArrow arrow ->
                    target.hurt(serverLevel.damageSources().arrow(arrow, arrow.getOwner()), (float) damage);

            default -> {}
        }
    }
}