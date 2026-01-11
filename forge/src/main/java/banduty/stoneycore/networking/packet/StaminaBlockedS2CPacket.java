package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StaminaBlockedS2CPacket(boolean blocked) {

    public static void handle(StaminaBlockedS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                NBTDataHelper.set((IEntityDataSaver) Minecraft.getInstance().player, PDKeys.STAMINA_BLOCKED, msg.blocked);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static StaminaBlockedS2CPacket decode(FriendlyByteBuf buf) {
        return new StaminaBlockedS2CPacket(buf.readBoolean());
    }

    public static void encode(StaminaBlockedS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.blocked);
    }
}