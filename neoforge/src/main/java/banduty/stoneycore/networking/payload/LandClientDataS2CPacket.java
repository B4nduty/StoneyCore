package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.LandClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record LandClientDataS2CPacket(
        UUID playerUuid,
        BlockPos currentLandCore,
        boolean isUnderSiege,
        boolean isParticipant
) implements CustomPacketPayload {

    public static final Type<LandClientDataS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "land_client_data_s2c")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LandClientDataS2CPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, LandClientDataS2CPacket::playerUuid,
            BlockPos.STREAM_CODEC, LandClientDataS2CPacket::currentLandCore,
            ByteBufCodecs.BOOL, LandClientDataS2CPacket::isUnderSiege,
            ByteBufCodecs.BOOL, LandClientDataS2CPacket::isParticipant,
            LandClientDataS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        LandClientState.set(playerUuid, new LandClientState(
                currentLandCore,
                isUnderSiege,
                isParticipant
        ));
    }
}