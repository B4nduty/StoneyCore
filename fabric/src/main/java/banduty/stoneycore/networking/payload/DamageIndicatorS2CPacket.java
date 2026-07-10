package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.SCPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DamageIndicatorS2CPacket(float damage) implements CustomPacketPayload {

    public static final Type<DamageIndicatorS2CPacket> ID =
            new Type<>(SCPayloads.DAMAGE_INDICATOR_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageIndicatorS2CPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    DamageIndicatorS2CPacket::damage,
                    DamageIndicatorS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void handle(ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (context.player() == null) {
                return;
            }

            if (!StoneyCore.getConfig().visualOptions().getDamageIndicator()) {
                return;
            }

            context.player().displayClientMessage(
                    Component.literal("Damage: " + (int) damage),
                    true
            );
        });
    }
}