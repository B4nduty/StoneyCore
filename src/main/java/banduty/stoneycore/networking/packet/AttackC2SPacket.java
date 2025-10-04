package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class AttackC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        StaminaData.removeStamina(player, StoneyCore.getConfig().combatOptions.attackStaminaConstant() * WeightUtil.getCachedWeight(player));
    }
}
