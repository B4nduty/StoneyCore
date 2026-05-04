package banduty.stoneycore.util.servertick;

import banduty.stoneycore.networking.payload.LandClientDataS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class ForgeLandTracker implements LandTrackerHelper {
    @Override
    public void sendLandClientDataS2C(ServerPlayer player, ServerPlayer otherPlayer, UUID uuid, boolean landPresent, BlockPos blockPos, boolean playerInLandUnderSiege, Boolean isParticipant) {
        PacketDistributor.sendToPlayer(player, new LandClientDataS2CPacket(uuid, blockPos, playerInLandUnderSiege, isParticipant));
    }
}
