package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class ModifiersUtil {
    private static final UUID ATTACK_RANGE_MODIFIER_ID = UUID.randomUUID();
    private static final UUID RANGE_MODIFIER_ID = UUID.randomUUID();

    public static void updatePlayerReachAttributes(ServerPlayerEntity player) {
        if (player == null) return;

        AttackHand hand = null;
        if (player instanceof PlayerAttackProperties props) {
            hand = PlayerAttackHelper.getCurrentAttack(player, props.getComboCount());
        }

        ItemStack itemStack = player.getMainHandStack();
        if (hand != null) itemStack = hand.itemStack();

        var attackRangeAttribute = player.getAttributeInstance(ReachEntityAttributes.ATTACK_RANGE);
        var rangeAttribute = player.getAttributeInstance(ReachEntityAttributes.REACH);

        boolean shouldHaveModifier = !itemStack.isEmpty() &&
                WeaponDefinitionsLoader.isMelee(itemStack);

        if (shouldHaveModifier) {
            double extraReach = SCWeaponUtil.getMaxDistance(itemStack.getItem());

            updateModifier(attackRangeAttribute, ATTACK_RANGE_MODIFIER_ID,
                    "Stoneycore attack range", extraReach);
            updateModifier(rangeAttribute, RANGE_MODIFIER_ID,
                    "Stoneycore range", extraReach);
        } else {
            removeModifierIfPresent(attackRangeAttribute, ATTACK_RANGE_MODIFIER_ID);
            removeModifierIfPresent(rangeAttribute, RANGE_MODIFIER_ID);
        }
    }

    private static void updateModifier(EntityAttributeInstance attribute, UUID uuid,
                                       String name, double value) {
        if (attribute == null) return;

        EntityAttributeModifier existingModifier = attribute.getModifier(uuid);
        if (existingModifier == null || existingModifier.getValue() != value) {
            attribute.removeModifier(uuid);
            attribute.addTemporaryModifier(
                    new EntityAttributeModifier(uuid, name, value,
                            EntityAttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeModifierIfPresent(EntityAttributeInstance attribute, UUID uuid) {
        if (attribute != null && attribute.getModifier(uuid) != null) {
            attribute.removeModifier(uuid);
        }
    }
}
