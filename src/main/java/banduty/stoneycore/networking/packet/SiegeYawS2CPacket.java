package banduty.stoneycore.networking.packet;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class SiegeYawS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        float wheelRotation = buf.readFloat();
        if (client.player != null && client.player.hasVehicle() && client.player.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
            abstractSiegeEntity.prevBodyYaw = abstractSiegeEntity.bodyYaw;
            abstractSiegeEntity.prevHeadYaw = abstractSiegeEntity.headYaw;
            abstractSiegeEntity.setTrackedYaw(yaw);
            abstractSiegeEntity.setYaw(yaw);
            abstractSiegeEntity.setHeadYaw(yaw);
            abstractSiegeEntity.setBodyYaw(yaw);


            abstractSiegeEntity.setTrackedPitch(pitch);
            abstractSiegeEntity.setPitch(pitch);
            abstractSiegeEntity.lastRiderPitch = pitch;

            abstractSiegeEntity.wheelRotation = wheelRotation;
        }
    }
}
