package banduty.stoneycore.networking.packet;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SiegeYawS2CPacket(float yaw, float pitch, float wheelRotation) {

    public static void handle(SiegeYawS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null &&
                    Minecraft.getInstance().player.isPassenger() &&
                    Minecraft.getInstance().player.getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {

                abstractSiegeEntity.yBodyRotO = abstractSiegeEntity.yBodyRot;
                abstractSiegeEntity.yHeadRotO = abstractSiegeEntity.yHeadRot;
                abstractSiegeEntity.setTrackedYaw(msg.yaw);
                abstractSiegeEntity.setYRot(msg.yaw);
                abstractSiegeEntity.setYHeadRot(msg.yaw);
                abstractSiegeEntity.setYBodyRot(msg.yaw);

                abstractSiegeEntity.setTrackedPitch(msg.pitch);
                abstractSiegeEntity.setXRot(msg.pitch);
                abstractSiegeEntity.lastRiderPitch = msg.pitch;

                abstractSiegeEntity.wheelRotation = msg.wheelRotation;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static SiegeYawS2CPacket decode(FriendlyByteBuf buf) {
        return new SiegeYawS2CPacket(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void encode(SiegeYawS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
        buf.writeFloat(msg.wheelRotation);
    }
}