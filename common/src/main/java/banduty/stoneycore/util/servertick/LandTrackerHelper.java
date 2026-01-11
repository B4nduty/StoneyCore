package banduty.stoneycore.util.servertick;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface LandTrackerHelper {
    void sendLandClientDataS2C(ServerPlayer player, ServerPlayer otherPlayer, UUID uuid, boolean landPresent, BlockPos blockPos, boolean playerInLandUnderSiege, Boolean isParticipant);
}
