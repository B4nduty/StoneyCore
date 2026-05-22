package banduty.stoneycore.networking.payload;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import banduty.stoneycore.networking.SCPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SiegeYawS2CPacket(float yaw, float pitch, float wheelRotation) implements CustomPacketPayload {
    public static final Type<SiegeYawS2CPacket> ID = new Type<>(SCPayloads.SIEGE_YAW_PITCH_S2C_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SiegeYawS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SiegeYawS2CPacket::yaw,
            ByteBufCodecs.FLOAT, SiegeYawS2CPacket::pitch,
            ByteBufCodecs.FLOAT, SiegeYawS2CPacket::wheelRotation,
            SiegeYawS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void handle(ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (context.player() != null && context.player().isPassenger() &&
                    context.player().getVehicle() instanceof AbstractSiegeEntity siege) {

                siege.yBodyRotO = siege.yBodyRot;
                siege.yHeadRotO = siege.yHeadRot;

                siege.setTrackedYaw(yaw);
                siege.setYRot(yaw);
                siege.setYHeadRot(yaw);
                siege.setYBodyRot(yaw);

                siege.setTrackedPitch(pitch);
                siege.setXRot(pitch);
                siege.lastRiderPitch = pitch;

                siege.wheelRotation = wheelRotation;
            }
        });
    }
}