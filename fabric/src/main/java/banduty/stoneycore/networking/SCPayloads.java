package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class SCPayloads {
    public static ResourceLocation ATTACK_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "attack");
    public static ResourceLocation RELOAD_PACKET_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "reload_packet");
    public static ResourceLocation SIEGE_YAW_PITCH_C2S_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "siege_yaw_pitch_c2s");
    public static ResourceLocation TOGGLE_VISOR_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "toggle_visor_c2s");
    public static ResourceLocation STAMINA_BLOCKED_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "stamina_blocked");
    public static ResourceLocation SYNC_DEFINITIONS = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "sync_definitions");
    public static ResourceLocation LAND_TITLE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "land_title_packet");
    public static ResourceLocation OUTLINE_CLAIM_PACKET_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "outline_claim_packet");
    public static ResourceLocation SIEGE_YAW_PITCH_S2C_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "siege_yaw_pitch_s2c");
    public static ResourceLocation LAND_CLIENT_DATA_S2C_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "land_client_data_s2c");

    static {
        registerC2S(AttackC2SPacket.ID, AttackC2SPacket.CODEC);
        registerC2S(ReloadC2SPacket.ID, ReloadC2SPacket.CODEC);
        registerC2S(SiegeYawC2SPacket.ID, SiegeYawC2SPacket.CODEC);
        registerC2S(ToggleVisorC2SPacket.ID, ToggleVisorC2SPacket.CODEC);

        registerS2C(StaminaBlockedS2CPacket.ID, StaminaBlockedS2CPacket.CODEC);
        registerS2C(SyncDefinitionsPacket.ID, SyncDefinitionsPacket.CODEC);
        registerS2C(LandTitleS2CPacket.ID, LandTitleS2CPacket.CODEC);
        registerS2C(OutlineClaimS2CPacket.ID, OutlineClaimS2CPacket.CODEC);
        registerS2C(SiegeYawS2CPacket.ID, SiegeYawS2CPacket.CODEC);
        registerS2C(LandClientDataS2CPacket.ID, LandClientDataS2CPacket.CODEC);
    }

    private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> packetIdentifier, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.playS2C().register(packetIdentifier, codec);
    }

    private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> packetIdentifier, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.playC2S().register(packetIdentifier, codec);
    }

    public static void registerPayloads() {
        StoneyCore.LOG.info("Registering Payloads for " + StoneyCore.MOD_ID);
    }
}