package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class StaminaValueS2CPacket {
    public static void receive(Minecraft client, ClientPacketListener handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        double stamina = buf.readDouble();
        if (client.player != null) {
            StaminaData.setStamina(client.player, stamina);
        }
    }
}