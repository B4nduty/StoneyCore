package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class StaminaBlockedS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        if (client.player != null) {
            ((IEntityDataSaver) client.player).stoneycore$getPersistentData().putBoolean("stamina_blocked", buf.readBoolean());
        }
    }
}