package banduty.stoneycore.stamina;

import net.minecraft.server.level.ServerPlayer;

public interface StaminaHelper {
    void syncStaminaBlocked(boolean blocked, ServerPlayer player);
}