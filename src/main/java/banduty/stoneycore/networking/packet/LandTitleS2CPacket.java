package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCoreClient;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class LandTitleS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        Text title = buf.readText();
        if (client.player != null) {
            StoneyCoreClient.LAND_TITLE_RENDERER.showTitle(title);
        }
    }
}
