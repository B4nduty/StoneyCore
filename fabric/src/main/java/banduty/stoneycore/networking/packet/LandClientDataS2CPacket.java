package banduty.stoneycore.networking.packet;

import banduty.stoneycore.lands.util.LandClientState;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class LandClientDataS2CPacket {
    public static void receive(Minecraft client, ClientPacketListener handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        UUID playerUuid = buf.readUUID();
        boolean hasLand = buf.readBoolean();
        BlockPos currentLandCore = hasLand ? buf.readBlockPos() : null;
        boolean isUnderSiege = buf.readBoolean();
        boolean isParticipant = buf.readBoolean();

        client.execute(() -> {
            LandClientState.set(playerUuid, new LandClientState(currentLandCore, isUnderSiege, isParticipant));
        });
    }
}
