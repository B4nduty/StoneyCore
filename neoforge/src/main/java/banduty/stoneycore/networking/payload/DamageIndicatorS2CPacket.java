package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DamageIndicatorS2CPacket(float damage) implements CustomPacketPayload {

    public static final Type<DamageIndicatorS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "damage_indicator_data_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageIndicatorS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    DamageIndicatorS2CPacket::damage,
                    DamageIndicatorS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DamageIndicatorS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }

            if (!StoneyCore.getConfig().visualOptions().getDamageIndicator()) {
                return;
            }

            context.player().displayClientMessage(
                    Component.literal("Damage: " + (int) packet.damage()),
                    true
            );
        });
    }
}