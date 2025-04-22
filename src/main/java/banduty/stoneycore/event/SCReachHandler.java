package banduty.stoneycore.event;

import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class SCReachHandler implements ServerTickEvents.StartTick {
    private static final UUID ATTACK_RANGE_MODIFIER_ID = UUID.randomUUID();

    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            updatePlayerReachAttributes(player);
        }
    }

    public static void updatePlayerReachAttributes(ServerPlayerEntity player) {
        if (player == null) return;

        ItemStack mainHandStack = player.getMainHandStack();
        var attackRangeAttribute = player.getAttributeInstance(ReachEntityAttributes.ATTACK_RANGE);

        if (attackRangeAttribute != null) {
            attackRangeAttribute.removeModifier(ATTACK_RANGE_MODIFIER_ID);
        }

        if (!mainHandStack.isEmpty() &&
                SCMeleeWeaponDefinitionsLoader.containsItem(mainHandStack.getItem())) {

            double extraReach = SCWeaponUtil.getMaxDistance(mainHandStack.getItem());

            if (attackRangeAttribute != null) {
                attackRangeAttribute.addPersistentModifier(
                        new EntityAttributeModifier(
                                ATTACK_RANGE_MODIFIER_ID,
                                "Stoneycore attack range",
                                extraReach,
                                EntityAttributeModifier.Operation.ADDITION
                        )
                );
            }
        }
    }
}