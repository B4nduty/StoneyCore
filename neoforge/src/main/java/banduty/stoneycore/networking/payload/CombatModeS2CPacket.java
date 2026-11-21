package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.data.IEntityDataSaver;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CombatModeS2CPacket(int combatMode) implements CustomPacketPayload {
    public static final Type<CombatModeS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "combat_mode")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CombatModeS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CombatModeS2CPacket::combatMode,
            CombatModeS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        ((IEntityDataSaver) context.player()).stoneycore$getPersistentData().putInt("combat_mode", combatMode);
    }
}