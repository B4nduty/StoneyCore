package banduty.stoneycore.networking.payload;

import banduty.stoneycore.lands.util.LandClientState;
import banduty.stoneycore.networking.SCPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record LandClientDataS2CPacket(
        UUID playerUuid,
        BlockPos currentLandCore,
        boolean isUnderSiege,
        boolean isParticipant
) implements CustomPacketPayload {
    public static final Type<LandClientDataS2CPacket> ID = new Type<>(SCPayloads.LAND_CLIENT_DATA_S2C_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, LandClientDataS2CPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, LandClientDataS2CPacket::playerUuid,
            BlockPos.STREAM_CODEC, LandClientDataS2CPacket::currentLandCore,
            ByteBufCodecs.BOOL, LandClientDataS2CPacket::isUnderSiege,
            ByteBufCodecs.BOOL, LandClientDataS2CPacket::isParticipant,
            LandClientDataS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public void handle(ClientPlayNetworking.Context context) {
        context.client().execute(() -> LandClientState.set(playerUuid, new LandClientState(
                currentLandCore,
                isUnderSiege,
                isParticipant
        )));
    }
}