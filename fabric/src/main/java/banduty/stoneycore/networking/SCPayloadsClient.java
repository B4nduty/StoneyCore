package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.ResourceLocation;

public interface SCPayloadsClient {
    ResourceLocation STAMINA_BLOCKED_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "stamina_blocked");
    ResourceLocation SYNC_DEFINITIONS = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "sync_definitions");
    ResourceLocation LAND_TITLE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "land_title_packet");
    ResourceLocation OUTLINE_CLAIM_PACKET_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "outline_claim_packet");
    ResourceLocation SIEGE_YAW_PITCH_S2C_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "siege_yaw_pitch_s2c");
    ResourceLocation LAND_CLIENT_DATA_S2C_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "land_client_data_s2c");

    static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(StaminaBlockedS2CPacket.ID, StaminaBlockedS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncDefinitionsPacket.ID, SyncDefinitionsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(LandTitleS2CPacket.ID, LandTitleS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OutlineClaimS2CPacket.ID, OutlineClaimS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SiegeYawS2CPacket.ID, SiegeYawS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(LandClientDataS2CPacket.ID, LandClientDataS2CPacket.CODEC);
    }

    static void registerS2CReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(StaminaBlockedS2CPacket.ID, StaminaBlockedS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(SyncDefinitionsPacket.ID, SyncDefinitionsPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(LandTitleS2CPacket.ID, LandTitleS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(OutlineClaimS2CPacket.ID, OutlineClaimS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(SiegeYawS2CPacket.ID, SiegeYawS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(LandClientDataS2CPacket.ID, LandClientDataS2CPacket::handle);
    }
}