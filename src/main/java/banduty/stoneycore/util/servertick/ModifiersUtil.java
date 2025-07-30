package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.definitionsloader.SCWeaponDefinitionsLoader;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
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

        ItemStack mainHandStack = player.getMainHandStack();
        var attackRangeAttribute = player.getAttributeInstance(ReachEntityAttributes.ATTACK_RANGE);
        var rangeAttribute = player.getAttributeInstance(ReachEntityAttributes.REACH);

        boolean shouldHaveModifier = !mainHandStack.isEmpty() &&
                SCWeaponDefinitionsLoader.isMelee(mainHandStack);

        if (shouldHaveModifier) {
            double extraReach = SCWeaponUtil.getMaxDistance(mainHandStack.getItem());
            double reachModifier = extraReach - 4.5F;

            updateModifier(attackRangeAttribute, ATTACK_RANGE_MODIFIER_ID,
                    "Stoneycore attack range", reachModifier);
            updateModifier(rangeAttribute, RANGE_MODIFIER_ID,
                    "Stoneycore range", reachModifier);
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
