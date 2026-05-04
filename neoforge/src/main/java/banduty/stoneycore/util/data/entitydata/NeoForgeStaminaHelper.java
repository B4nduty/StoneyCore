package banduty.stoneycore.util.data.entitydata;

import banduty.stoneycore.networking.payload.StaminaBlockedS2CPacket;
import banduty.stoneycore.networking.payload.StaminaValueS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeStaminaHelper implements StaminaHelper {
    @Override
    public void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new StaminaBlockedS2CPacket(blocked));
    }

    @Override
    public void syncStaminaValue(double stamina, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new StaminaValueS2CPacket(stamina));
    }
}