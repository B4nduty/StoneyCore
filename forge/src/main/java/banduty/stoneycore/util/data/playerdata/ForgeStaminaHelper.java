package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.StaminaBlockedS2CPacket;
import banduty.stoneycore.networking.packet.StaminaValueS2CPacket;
import net.minecraft.server.level.ServerPlayer;

public class ForgeStaminaHelper implements StaminaHelper {
    @Override
    public void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        ModMessages.sendToPlayer(new StaminaBlockedS2CPacket(blocked), player);
    }

    @Override
    public void syncStaminaValue(double stamina, ServerPlayer player) {
        ModMessages.sendToPlayer(new StaminaValueS2CPacket(stamina), player);
    }
}