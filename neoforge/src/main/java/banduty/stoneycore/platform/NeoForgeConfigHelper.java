package banduty.stoneycore.platform;

import banduty.stoneycore.platform.services.ConfigHelper;
import banduty.stoneycore.networking.payload.DamageIndicatorS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeConfigHelper implements ConfigHelper {

    @Override
    public void sendDamageIndicator(ServerPlayer player, float damage) {
        PacketDistributor.sendToPlayer(
                player,
                new DamageIndicatorS2CPacket(damage)
        );
    }
}
