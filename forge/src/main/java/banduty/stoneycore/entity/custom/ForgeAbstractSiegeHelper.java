package banduty.stoneycore.entity.custom;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.SiegeYawS2CPacket;
import banduty.stoneycore.sounds.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ForgeAbstractSiegeHelper implements AbstractSiegeHelper {
    @Override
    public SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.BULLET_CRACK.get();
    }

    @Override
    public void updateSiegeNetworkData(ServerLevel serverLevel, AbstractSiegeEntity abstractSiegeEntity) {
        List<ServerPlayer> players = serverLevel.players();
        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            if (!abstractSiegeEntity.playersNotified.contains(playerId)) {
                ModMessages.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SiegeYawS2CPacket(abstractSiegeEntity.getYRot(), abstractSiegeEntity.getXRot(), abstractSiegeEntity.getWheelRotation())
                );
                abstractSiegeEntity.playersNotified.add(playerId);
            }
        }
        Set<UUID> onlinePlayerIds = players.stream()
                .map(ServerPlayer::getUUID)
                .collect(Collectors.toSet());
        abstractSiegeEntity.playersNotified.removeIf(uuid -> !onlinePlayerIds.contains(uuid));
    }
}