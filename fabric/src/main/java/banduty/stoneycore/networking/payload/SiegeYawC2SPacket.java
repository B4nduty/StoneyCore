package banduty.stoneycore.networking.payload;

import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import banduty.stoneycore.networking.SCPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.Horse;

public record SiegeYawC2SPacket(float yaw, float pitch) implements CustomPacketPayload {
    public static final Type<SiegeYawC2SPacket> ID = new Type<>(SCPayloads.SIEGE_YAW_PITCH_C2S_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SiegeYawC2SPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SiegeYawC2SPacket::yaw,
            ByteBufCodecs.FLOAT, SiegeYawC2SPacket::pitch,
            SiegeYawC2SPacket::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(SiegeYawC2SPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        context.server().execute(() -> {
            if (player.getVehicle() instanceof AbstractSiegeEntity siege) {
                applyYawPitch(siege, payload.yaw, payload.pitch);
            } else if (player.getVehicle() instanceof Horse horse && horse.getVehicle() instanceof AbstractSiegeEntity siege) {
                applyYawPitch(siege, payload.yaw, 0); // Horse only syncs yaw[cite: 15]
            }
        });
    }

    private static void applyYawPitch(AbstractSiegeEntity siege, float yaw, float pitch) {
        siege.setTrackedYaw(yaw);
        siege.setYRot(yaw);
        siege.setYHeadRot(yaw);
        siege.setYBodyRot(yaw);
        siege.lastRiderYaw = yaw;
        if (pitch != 0) {
            siege.setTrackedPitch(pitch);
            siege.setXRot(pitch);
            siege.lastRiderPitch = pitch;
        }
    }
}