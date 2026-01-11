package banduty.stoneycore.networking.packet;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.horse.Horse;

public class SiegeYawC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        server.execute(() -> {
            if (player.isPassenger() && player.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
                abstractSiegeEntity.setTrackedYaw(yaw);
                abstractSiegeEntity.setYRot(yaw);
                abstractSiegeEntity.setYHeadRot(yaw);
                abstractSiegeEntity.setYBodyRot(yaw);
                abstractSiegeEntity.lastRiderYaw = yaw;

                abstractSiegeEntity.setTrackedPitch(pitch);
                abstractSiegeEntity.setXRot(pitch);
                abstractSiegeEntity.lastRiderPitch = pitch;
            } else if (player.isPassenger() && player.getVehicle() instanceof Horse horse && horse.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
                abstractSiegeEntity.setTrackedYaw(yaw);
                abstractSiegeEntity.setYRot(yaw);
                abstractSiegeEntity.setYHeadRot(yaw);
                abstractSiegeEntity.setYBodyRot(yaw);
                abstractSiegeEntity.lastRiderYaw = yaw;
            }
        });
    }
}
