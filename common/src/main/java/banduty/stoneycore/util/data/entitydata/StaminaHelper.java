package banduty.stoneycore.util.data.entitydata;

import net.minecraft.server.level.ServerPlayer;

public interface StaminaHelper {
    void syncStaminaBlocked(boolean blocked, ServerPlayer player);
}