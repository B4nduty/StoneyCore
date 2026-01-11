package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCoreClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LandTitleS2CPacket(Component title) {

    public static void handle(LandTitleS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            StoneyCoreClient.LAND_TITLE_RENDERER.showTitle(msg.title);
        });
        ctx.get().setPacketHandled(true);
    }

    public static LandTitleS2CPacket decode(FriendlyByteBuf buf) {
        return new LandTitleS2CPacket(buf.readComponent());
    }

    public static void encode(LandTitleS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeComponent(msg.title);
    }
}