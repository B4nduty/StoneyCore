package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.networking.ModMessages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class FabricStaminaHelper implements StaminaHelper {
    @Override
    public void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeBoolean(blocked);
        ServerPlayNetworking.send(player, ModMessages.STAMINA_BLOCKED_ID, buffer);
    }
}
