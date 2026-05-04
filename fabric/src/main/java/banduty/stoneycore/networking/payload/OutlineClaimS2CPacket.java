package banduty.stoneycore.networking.payload;

import banduty.stoneycore.client.ClientOutlineRenderer;
import banduty.stoneycore.networking.SCPayloadsClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record OutlineClaimS2CPacket(List<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<OutlineClaimS2CPacket> ID = new Type<>(SCPayloadsClient.OUTLINE_CLAIM_PACKET_ID);
    private static final BlockPos FIX_BLOCKPOS_OUTLINE = new BlockPos(0, 400, 0);

    public static final StreamCodec<RegistryFriendlyByteBuf, OutlineClaimS2CPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), OutlineClaimS2CPacket::positions,
            OutlineClaimS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(OutlineClaimS2CPacket payload, ClientPlayNetworking.Context context) {
        List<BlockPos> mutablePositions = new ArrayList<>(payload.positions);
        mutablePositions.add(FIX_BLOCKPOS_OUTLINE);

        context.client().execute(() -> ClientOutlineRenderer.updatePositions(mutablePositions));
    }
}