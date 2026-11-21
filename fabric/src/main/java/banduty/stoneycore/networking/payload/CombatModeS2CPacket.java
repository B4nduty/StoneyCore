package banduty.stoneycore.networking.payload;

import banduty.stoneycore.data.IEntityDataSaver;
import banduty.stoneycore.networking.SCPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CombatModeS2CPacket(int combatMode) implements CustomPacketPayload {
    public static final Type<CombatModeS2CPacket> ID = new Type<>(SCPayloads.COMBAT_MODE_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CombatModeS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CombatModeS2CPacket::combatMode,
            CombatModeS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void handle(ClientPlayNetworking.Context context) {
        if (context.player() != null) {
            ((IEntityDataSaver) context.player()).stoneycore$getPersistentData().putInt("combat_mode", combatMode);
        }
    }
}