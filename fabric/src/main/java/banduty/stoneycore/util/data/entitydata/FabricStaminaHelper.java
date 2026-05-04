package banduty.stoneycore.util.data.entitydata;

import banduty.stoneycore.networking.payload.StaminaBlockedS2CPacket;
import banduty.stoneycore.networking.payload.StaminaValueS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricStaminaHelper implements StaminaHelper {
    @Override
    public void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        ServerPlayNetworking.send(player, new StaminaBlockedS2CPacket(blocked));
    }

    @Override
    public void syncStaminaValue(double stamina, ServerPlayer player) {
        ServerPlayNetworking.send(player, new StaminaValueS2CPacket(stamina));

    }
}
