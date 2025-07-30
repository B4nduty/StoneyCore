package banduty.stoneycore.networking.packet;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class SiegeYawC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        server.execute(() -> {
            if (player.hasVehicle() && player.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
                abstractSiegeEntity.setTrackedYaw(yaw);
                abstractSiegeEntity.setYaw(yaw);
                abstractSiegeEntity.setHeadYaw(yaw);
                abstractSiegeEntity.setBodyYaw(yaw);
                abstractSiegeEntity.lastRiderYaw = yaw;

                abstractSiegeEntity.setTrackedPitch(pitch);
                abstractSiegeEntity.setPitch(pitch);
                abstractSiegeEntity.lastRiderPitch = pitch;
            } else if (player.hasVehicle() && player.getVehicle() instanceof HorseEntity horseEntity && horseEntity.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
                abstractSiegeEntity.setTrackedYaw(yaw);
                abstractSiegeEntity.setYaw(yaw);
                abstractSiegeEntity.setHeadYaw(yaw);
                abstractSiegeEntity.setBodyYaw(yaw);
                abstractSiegeEntity.lastRiderYaw = yaw;
            }
        });
    }
}
