package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StaminaBlockedS2CPacket(boolean blocked) implements CustomPacketPayload {
    public static final Type<StaminaBlockedS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "stamina_blocked")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaBlockedS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, StaminaBlockedS2CPacket::blocked,
            StaminaBlockedS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        ((IEntityDataSaver) context.player()).stoneycore$getPersistentData().putBoolean("stamina_blocked", blocked);
    }
}