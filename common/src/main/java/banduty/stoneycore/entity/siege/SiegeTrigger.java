package banduty.stoneycore.entity.siege;

import banduty.stoneycore.lands.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** The one place that decides "this engine / projectile just entered enemy land, start a siege". */
public final class SiegeTrigger {

    private SiegeTrigger() {}

    public static void tryStart(ServerLevel level, @Nullable UUID attacker, BlockPos pos) {
        if (attacker == null) return;
        if (SiegeManager.getPlayerSiege(level, attacker).isPresent()) return;

        LandState.get(level).getLandAt(pos).ifPresent(land -> {
            if (!land.getOwnerUUID().equals(attacker)) {
                SiegeManager.startSiege(level, attacker, land.getOwnerUUID());
            }
        });
    }
}
