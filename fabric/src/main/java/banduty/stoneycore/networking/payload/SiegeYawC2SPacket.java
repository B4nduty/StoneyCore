package banduty.stoneycore.networking.payload;

import banduty.stoneycore.entity.siege.SiegeMovement;
import banduty.stoneycore.networking.SCPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SiegeYawC2SPacket(float yaw, float pitch) implements CustomPacketPayload {
    public static final Type<SiegeYawC2SPacket> ID = new Type<>(SCPayloads.SIEGE_YAW_PITCH_C2S_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SiegeYawC2SPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SiegeYawC2SPacket::yaw,
            ByteBufCodecs.FLOAT, SiegeYawC2SPacket::pitch,
            SiegeYawC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> SiegeMovement.applyClientAim(context.player(), yaw, pitch));
    }
}
