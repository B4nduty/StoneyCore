package banduty.stoneycore.platform.services;

import net.minecraft.server.level.ServerPlayer;

public interface ConfigHelper {
    void sendDamageIndicator(ServerPlayer player, float damage);
}
