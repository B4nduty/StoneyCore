package banduty.stoneycore.networking.payload;

import banduty.stoneycore.networking.SCPayloadsClient;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StaminaBlockedS2CPacket(boolean blocked) implements CustomPacketPayload {
    public static final Type<StaminaBlockedS2CPacket> ID = new Type<>(SCPayloadsClient.STAMINA_BLOCKED_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaBlockedS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, StaminaBlockedS2CPacket::blocked,
            StaminaBlockedS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void handle(StaminaBlockedS2CPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (context.player() != null) {
                ((IEntityDataSaver) context.player()).stoneycore$getPersistentData()
                        .putBoolean("stamina_blocked", payload.blocked());
            }
        });
    }
}