package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LandTitleS2CPacket(Component title) implements CustomPacketPayload {
    public static final Type<LandTitleS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "land_title_packet")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LandTitleS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, LandTitleS2CPacket::title,
            LandTitleS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        StoneyCoreClient.LAND_TITLE_RENDERER.showTitle(this.title);
    }
}