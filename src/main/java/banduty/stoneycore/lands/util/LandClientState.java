package banduty.stoneycore.lands.util;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record LandClientState(BlockPos currentLandCore, boolean isUnderSiege, boolean isParticipant) {
    private static final Map<UUID, LandClientState> PLAYER_STATES = new HashMap<>();

    public static void set(UUID uuid, LandClientState state) {
        PLAYER_STATES.put(uuid, state);
    }

    public static LandClientState get(UUID uuid) {
        return PLAYER_STATES.getOrDefault(uuid, new LandClientState(null, false, false));
    }
}
