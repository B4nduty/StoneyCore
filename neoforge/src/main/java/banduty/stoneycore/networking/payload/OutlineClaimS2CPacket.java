package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.client.ClientOutlineRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OutlineClaimS2CPacket(List<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<OutlineClaimS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "outline_claim_packet")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OutlineClaimS2CPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), OutlineClaimS2CPacket::positions,
            OutlineClaimS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        List<BlockPos> mutablePositions = new java.util.ArrayList<>(this.positions);
        mutablePositions.add(new BlockPos(0, 400, 0));
        ClientOutlineRenderer.updatePositions(mutablePositions);
    }

    public static float[] intToRgba(int color) {
        int r = (color >> 24) & 0xFF;
        int g = (color >> 16) & 0xFF;
        int b = (color >> 8) & 0xFF;
        int a = color & 0xFF;

        return new float[]{
                r / 255f,
                g / 255f,
                b / 255f,
                a / 255f
        };
        }
}