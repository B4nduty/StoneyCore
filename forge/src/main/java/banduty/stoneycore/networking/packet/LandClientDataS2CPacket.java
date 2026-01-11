package banduty.stoneycore.networking.packet;

import banduty.stoneycore.lands.util.LandClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record LandClientDataS2CPacket(UUID playerUuid, BlockPos currentLandCore, boolean isUnderSiege,
                                      boolean isParticipant) {

    public static void handle(LandClientDataS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LandClientState.set(msg.playerUuid, new LandClientState(msg.currentLandCore, msg.isUnderSiege, msg.isParticipant));
        });
        ctx.get().setPacketHandled(true);
    }

    public static LandClientDataS2CPacket decode(FriendlyByteBuf buf) {
        UUID playerUuid = buf.readUUID();
        boolean hasLand = buf.readBoolean();
        BlockPos currentLandCore = hasLand ? buf.readBlockPos() : null;
        boolean isUnderSiege = buf.readBoolean();
        boolean isParticipant = buf.readBoolean();
        return new LandClientDataS2CPacket(playerUuid, currentLandCore, isUnderSiege, isParticipant);
    }

    public static void encode(LandClientDataS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        buf.writeBoolean(msg.currentLandCore != null);
        if (msg.currentLandCore != null) {
            buf.writeBlockPos(msg.currentLandCore);
        }
        buf.writeBoolean(msg.isUnderSiege);
        buf.writeBoolean(msg.isParticipant);
    }
}