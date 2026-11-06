package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.siege.SiegeMovement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SiegeYawC2SPacket(float yaw, float pitch) implements CustomPacketPayload {
    public static final Type<SiegeYawC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "siege_yaw_pitch_c2s")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SiegeYawC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SiegeYawC2SPacket::yaw,
            ByteBufCodecs.FLOAT, SiegeYawC2SPacket::pitch,
            SiegeYawC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            SiegeMovement.applyClientAim(player, yaw, pitch);
        }
    }
}
