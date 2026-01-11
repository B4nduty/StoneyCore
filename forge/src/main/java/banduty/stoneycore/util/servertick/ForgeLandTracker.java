package banduty.stoneycore.util.servertick;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.LandClientDataS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class ForgeLandTracker implements LandTrackerHelper {
    @Override
    public void sendLandClientDataS2C(ServerPlayer player, ServerPlayer otherPlayer, UUID uuid, boolean landPresent, BlockPos blockPos, boolean playerInLandUnderSiege, Boolean isParticipant) {
        ModMessages.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LandClientDataS2CPacket(uuid, blockPos, playerInLandUnderSiege, isParticipant));
    }
}
