package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.networking.SCPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LandTitleS2CPacket(Component title) implements CustomPacketPayload {
    public static final Type<LandTitleS2CPacket> ID = new Type<>(SCPayloads.LAND_TITLE_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, LandTitleS2CPacket> CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, LandTitleS2CPacket::title,
            LandTitleS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public void handle(ClientPlayNetworking.Context context) {
        if (context.player() != null) {
            StoneyCoreClient.LAND_TITLE_RENDERER.showTitle(title);
        }
    }
}