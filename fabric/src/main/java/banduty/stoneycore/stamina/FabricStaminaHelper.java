package banduty.stoneycore.stamina;

import banduty.stoneycore.networking.payload.CombatModeS2CPacket;
import banduty.stoneycore.networking.payload.StaminaBlockedS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricStaminaHelper implements StaminaHelper {
    @Override
    public void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        ServerPlayNetworking.send(player, new StaminaBlockedS2CPacket(blocked));
    }

    @Override
    public void syncCombatMode(int combatMode, ServerPlayer player) {
        ServerPlayNetworking.send(player, new CombatModeS2CPacket(combatMode));
    }
}
