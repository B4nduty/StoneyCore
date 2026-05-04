package banduty.stoneycore.entity.custom;

import banduty.stoneycore.networking.payload.SiegeYawS2CPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ForgeAbstractSiegeHelper implements AbstractSiegeHelper {
    @Override
    public void updateSiegeNetworkData(ServerLevel serverLevel, AbstractSiegeEntity abstractSiegeEntity) {
        List<ServerPlayer> players = serverLevel.players();
        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            if (!abstractSiegeEntity.playersNotified.contains(playerId)) {
                PacketDistributor.sendToPlayer(player, new SiegeYawS2CPacket(abstractSiegeEntity.getYRot(),
                        abstractSiegeEntity.getXRot(), abstractSiegeEntity.getWheelRotation()));
                abstractSiegeEntity.playersNotified.add(playerId);
            }
        }
        Set<UUID> onlinePlayerIds = players.stream()
                .map(ServerPlayer::getUUID)
                .collect(Collectors.toSet());
        abstractSiegeEntity.playersNotified.removeIf(uuid -> !onlinePlayerIds.contains(uuid));
    }
}