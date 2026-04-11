package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StaminaValueS2CPacket(double stamina) {
    public static void handle(StaminaValueS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                StaminaData.setStamina(Minecraft.getInstance().player, msg.stamina);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static StaminaValueS2CPacket decode(FriendlyByteBuf buf) {
        return new StaminaValueS2CPacket(buf.readDouble());
    }

    public static void encode(StaminaValueS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.stamina);
    }
}