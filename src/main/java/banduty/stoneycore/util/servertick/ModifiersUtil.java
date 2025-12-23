package banduty.stoneycore.util.servertick;

import banduty.stoneycore.util.bettercombatlogic.SCBetterCombat;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ModifiersUtil {
    private static final UUID ATTACK_RANGE_MODIFIER_ID = UUID.randomUUID();
    private static final UUID RANGE_MODIFIER_ID = UUID.randomUUID();

    public static void updatePlayerReachAttributes(ServerPlayer player) {
        if (player == null) return;

        ItemStack itemStack = player.getMainHandItem();
        if (FabricLoader.getInstance().isModLoaded("bettercombat")) SCBetterCombat.getWeaponStack(player, player.getMainHandItem());

        var attackRangeAttribute = player.getAttribute(ReachEntityAttributes.ATTACK_RANGE);
        var rangeAttribute = player.getAttribute(ReachEntityAttributes.REACH);

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

    private static void updateModifier(AttributeInstance attribute, UUID uuid,
                                       String name, double value) {
        if (attribute == null) return;

        AttributeModifier existingModifier = attribute.getModifier(uuid);
        if (existingModifier == null || existingModifier.getAmount() != value) {
            attribute.removeModifier(uuid);
            attribute.addTransientModifier(
                    new AttributeModifier(uuid, name, value,
                            AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeModifierIfPresent(AttributeInstance attribute, UUID uuid) {
        if (attribute != null && attribute.getModifier(uuid) != null) {
            attribute.removeModifier(uuid);
        }
    }
}
