package banduty.stoneycore.util.data.entitydata;

import banduty.stoneycore.networking.payload.DamageIndicatorS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricConfigHelper implements ConfigHelper {

    @Override
    public void sendDamageIndicator(ServerPlayer player, float damage) {
        ServerPlayNetworking.send(
                player,
                new DamageIndicatorS2CPacket(damage)
        );
    }
}
