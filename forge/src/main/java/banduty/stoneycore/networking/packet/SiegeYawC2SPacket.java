package banduty.stoneycore.networking.packet;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SiegeYawC2SPacket(float yaw, float pitch) {

    public static void handle(SiegeYawC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.isPassenger()) {
                if (player.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
                    abstractSiegeEntity.setTrackedYaw(msg.yaw);
                    abstractSiegeEntity.setYRot(msg.yaw);
                    abstractSiegeEntity.setYHeadRot(msg.yaw);
                    abstractSiegeEntity.setYBodyRot(msg.yaw);
                    abstractSiegeEntity.lastRiderYaw = msg.yaw;

                    abstractSiegeEntity.setTrackedPitch(msg.pitch);
                    abstractSiegeEntity.setXRot(msg.pitch);
                    abstractSiegeEntity.lastRiderPitch = msg.pitch;
                } else if (player.getVehicle() instanceof Horse horse && horse.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {
                    abstractSiegeEntity.setTrackedYaw(msg.yaw);
                    abstractSiegeEntity.setYRot(msg.yaw);
                    abstractSiegeEntity.setYHeadRot(msg.yaw);
                    abstractSiegeEntity.setYBodyRot(msg.yaw);
                    abstractSiegeEntity.lastRiderYaw = msg.yaw;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static SiegeYawC2SPacket decode(FriendlyByteBuf buf) {
        return new SiegeYawC2SPacket(buf.readFloat(), buf.readFloat());
    }

    public static void encode(SiegeYawC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
    }
}