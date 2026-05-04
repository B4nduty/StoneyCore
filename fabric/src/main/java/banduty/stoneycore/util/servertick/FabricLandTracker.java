package banduty.stoneycore.util.servertick;

import banduty.stoneycore.networking.payload.LandClientDataS2CPacket;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class FabricLandTracker implements LandTrackerHelper {
    @Override
    public void sendLandClientDataS2C(ServerPlayer player, ServerPlayer otherPlayer, UUID uuid, boolean landPresent, BlockPos blockPos, boolean playerInLandUnderSiege, Boolean isParticipant) {
        ServerPlayNetworking.send(otherPlayer, new LandClientDataS2CPacket(player.getUUID(), blockPos,
                SiegeManager.isPlayerInLandUnderSiege(player.serverLevel(), player),
                SiegeManager.getPlayerSiege(player.serverLevel(), player.getUUID())
                        .map(siege -> !siege.disabledPlayers.contains(player.getUUID()))
                        .orElse(false)));
    }
}
