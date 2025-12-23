package banduty.stoneycore.networking.packet;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class SiegeYawS2CPacket {
    public static void receive(Minecraft client, ClientPacketListener handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        float wheelRotation = buf.readFloat();
        if (client.player != null && client.player.isPassenger() && client.player.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
            abstractSiegeEntity.yBodyRotO = abstractSiegeEntity.yBodyRot;
            abstractSiegeEntity.yHeadRotO = abstractSiegeEntity.yHeadRot;
            abstractSiegeEntity.setTrackedYaw(yaw);
            abstractSiegeEntity.setYRot(yaw);
            abstractSiegeEntity.setYHeadRot(yaw);
            abstractSiegeEntity.setYBodyRot(yaw);


            abstractSiegeEntity.setTrackedPitch(pitch);
            abstractSiegeEntity.setXRot(pitch);
            abstractSiegeEntity.lastRiderPitch = pitch;

            abstractSiegeEntity.wheelRotation = wheelRotation;
        }
    }
}
