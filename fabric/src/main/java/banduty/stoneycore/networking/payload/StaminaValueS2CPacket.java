package banduty.stoneycore.networking.payload;

import banduty.stoneycore.networking.SCPayloadsClient;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StaminaValueS2CPacket(double stamina) implements CustomPacketPayload {
    public static final Type<StaminaValueS2CPacket> ID = new Type<>(SCPayloadsClient.STAMINA_VALUE_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaValueS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, StaminaValueS2CPacket::stamina,
            StaminaValueS2CPacket::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(StaminaValueS2CPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (context.client().player != null) {
                StaminaData.setStamina(context.client().player, payload.stamina);
            }
        });
    }
}