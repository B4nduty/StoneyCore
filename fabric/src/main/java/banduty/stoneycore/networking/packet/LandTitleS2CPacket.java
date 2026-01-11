package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCoreClient;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class LandTitleS2CPacket {
    public static void receive(Minecraft client, ClientPacketListener handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        Component title = buf.readComponent();
        if (client.player != null) {
            StoneyCoreClient.LAND_TITLE_RENDERER.showTitle(title);
        }
    }
}
