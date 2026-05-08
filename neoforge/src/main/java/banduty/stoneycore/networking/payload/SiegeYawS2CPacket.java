package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SiegeYawS2CPacket(float yaw, float pitch, float wheelRotation) implements CustomPacketPayload {
    public static final Type<SiegeYawS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "siege_yaw_s2c")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SiegeYawS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SiegeYawS2CPacket::yaw,
            ByteBufCodecs.FLOAT, SiegeYawS2CPacket::pitch,
            ByteBufCodecs.FLOAT, SiegeYawS2CPacket::wheelRotation,
            SiegeYawS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        if (context.player().isPassenger() && context.player().getVehicle() instanceof AbstractSiegeEntity abstractSiegeEntity) {

            abstractSiegeEntity.yBodyRotO = abstractSiegeEntity.yBodyRot;
            abstractSiegeEntity.yHeadRotO = abstractSiegeEntity.yHeadRot;
            abstractSiegeEntity.setTrackedYaw(this.yaw);
            abstractSiegeEntity.setYRot(this.yaw);
            abstractSiegeEntity.setYHeadRot(this.yaw);
            abstractSiegeEntity.setYBodyRot(this.yaw);

            abstractSiegeEntity.setTrackedPitch(this.pitch);
            abstractSiegeEntity.setXRot(this.pitch);
            abstractSiegeEntity.lastRiderPitch = this.pitch;

            abstractSiegeEntity.wheelRotation = this.wheelRotation;
        }
    }
}