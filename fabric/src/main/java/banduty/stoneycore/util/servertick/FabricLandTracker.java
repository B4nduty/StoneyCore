package banduty.stoneycore.util.servertick;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class FabricLandTracker implements LandTrackerHelper {
    @Override
    public void sendLandClientDataS2C(ServerPlayer player, ServerPlayer otherPlayer, UUID uuid, boolean landPresent, BlockPos blockPos, boolean playerInLandUnderSiege, Boolean isParticipant) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(player.getUUID());
        buf.writeBoolean(landPresent);
        buf.writeBoolean(SiegeManager.isPlayerInLandUnderSiege(player.serverLevel(), player));
        buf.writeBoolean(SiegeManager.getPlayerSiege(player.serverLevel(), player.getUUID())
                .map(siege -> !siege.disabledPlayers.contains(player.getUUID()))
                .orElse(false));

        ServerPlayNetworking.send(otherPlayer, ModMessages.LAND_CLIENT_DATA_S2C_ID, buf);
    }
}
