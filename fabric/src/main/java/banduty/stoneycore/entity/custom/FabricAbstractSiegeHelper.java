package banduty.stoneycore.entity.custom;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.sounds.ModSounds;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FabricAbstractSiegeHelper implements AbstractSiegeHelper {
    @Override
    public SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.BULLET_CRACK;
    }

    @Override
    public void updateSiegeNetworkData(ServerLevel serverLevel, AbstractSiegeEntity abstractSiegeEntity) {
        List<ServerPlayer> players = serverLevel.players();
        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            if (!abstractSiegeEntity.playersNotified.contains(playerId)) {
                FriendlyByteBuf buffer = PacketByteBufs.create();
                buffer.writeFloat(abstractSiegeEntity.getYRot());
                buffer.writeFloat(abstractSiegeEntity.getXRot());
                buffer.writeFloat(abstractSiegeEntity.getWheelRotation());
                ServerPlayNetworking.send(player, ModMessages.SIEGE_YAW_PITCH_S2C_ID, buffer);
                abstractSiegeEntity.playersNotified.add(playerId);
            }
        }
        Set<UUID> onlinePlayerIds = players.stream()
                .map(ServerPlayer::getUUID)
                .collect(Collectors.toSet());
        abstractSiegeEntity.playersNotified.removeIf(uuid -> !onlinePlayerIds.contains(uuid));
    }
}
