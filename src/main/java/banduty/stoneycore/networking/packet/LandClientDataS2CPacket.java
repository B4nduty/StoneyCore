package banduty.stoneycore.networking.packet;

import banduty.stoneycore.lands.util.LandClientState;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class LandClientDataS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        UUID playerUuid = buf.readUuid();
        boolean hasLand = buf.readBoolean();
        BlockPos currentLandCore = hasLand ? buf.readBlockPos() : null;
        boolean isUnderSiege = buf.readBoolean();
        boolean isParticipant = buf.readBoolean();

        client.execute(() -> {
            LandClientState.set(playerUuid, new LandClientState(currentLandCore, isUnderSiege, isParticipant));
        });
    }
}
