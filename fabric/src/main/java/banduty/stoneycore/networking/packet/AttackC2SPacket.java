package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class AttackC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        StaminaData.removeStamina(player, StoneyCore.getConfig().combatOptions().attackStaminaConstant() * WeightUtil.getCachedWeight(player));
    }
}
