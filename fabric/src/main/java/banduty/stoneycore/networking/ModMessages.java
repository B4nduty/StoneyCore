package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.packet.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public interface ModMessages {
    ResourceLocation STAMINA_BLOCKED_ID = new ResourceLocation(StoneyCore.MOD_ID, "stamina_blocked");
    ResourceLocation ATTACK_ID = new ResourceLocation(StoneyCore.MOD_ID, "attack");
    ResourceLocation RELOAD_PACKET_ID = new ResourceLocation(StoneyCore.MOD_ID, "reload_packet");
    ResourceLocation LAND_TITLE_PACKET_ID = new ResourceLocation(StoneyCore.MOD_ID, "land_title_packet");
    ResourceLocation OUTLINE_CLAIM_PACKET_ID = new ResourceLocation(StoneyCore.MOD_ID, "outline_claim_packet");
    ResourceLocation SIEGE_YAW_PITCH_C2S_ID = new ResourceLocation(StoneyCore.MOD_ID, "siege_yaw_pitch_c2s");
    ResourceLocation SIEGE_YAW_PITCH_S2C_ID = new ResourceLocation(StoneyCore.MOD_ID, "siege_yaw_pitch_s2c");
    ResourceLocation LAND_CLIENT_DATA_S2C_ID = new ResourceLocation(StoneyCore.MOD_ID, "land_client_data_s2c");
    ResourceLocation TOGGLE_VISOR_ID = new ResourceLocation(StoneyCore.MOD_ID, "toggle_visor_c2s");

    static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ATTACK_ID, AttackC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(RELOAD_PACKET_ID, ReloadC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(SIEGE_YAW_PITCH_C2S_ID, SiegeYawC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_VISOR_ID, ToogleVisorC2SPacket::receive);
    }

    static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(STAMINA_BLOCKED_ID, StaminaBlockedS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(LAND_TITLE_PACKET_ID, LandTitleS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(OUTLINE_CLAIM_PACKET_ID, OutlineClaimS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(SIEGE_YAW_PITCH_S2C_ID, SiegeYawS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(LAND_CLIENT_DATA_S2C_ID, LandClientDataS2CPacket::receive);
    }
}