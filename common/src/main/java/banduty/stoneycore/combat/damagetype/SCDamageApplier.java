package banduty.stoneycore.combat.damagetype;

import banduty.stoneycore.util.data.itemdata.SCTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SCDamageApplier {
    public static void apply(LivingEntity target, Entity attacker, ItemStack stack, double damage) {
        if (attacker == null || !(attacker.level() instanceof ServerLevel serverLevel)) return;

        damage = EnchantmentHelper.modifyDamage(serverLevel, stack, target, serverLevel.damageSources().generic(), (float) damage);

        if (stack.is(SCTags.WEAPONS_IGNORES_ARMOR.getTag()) &&
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