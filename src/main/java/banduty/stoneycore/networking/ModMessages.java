package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.packet.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ModMessages {
    public static final ResourceLocation STAMINA_BLOCKED_ID = new ResourceLocation(StoneyCore.MOD_ID, "stamina_blocked");
    public static final ResourceLocation ATTACK_ID = new ResourceLocation(StoneyCore.MOD_ID, "attack");
    public static final ResourceLocation RELOAD_PACKET_ID = new ResourceLocation(StoneyCore.MOD_ID, "reload_packet");
    public static final ResourceLocation LAND_TITLE_PACKET_ID = new ResourceLocation(StoneyCore.MOD_ID, "land_title_packet");
    public static final ResourceLocation OUTLINE_CLAIM_PACKET_ID = new ResourceLocation(StoneyCore.MOD_ID, "outline_claim_packet");
    public static final ResourceLocation SIEGE_YAW_PITCH_C2S_ID = new ResourceLocation(StoneyCore.MOD_ID, "siege_yaw_pitch_c2s");
    public static final ResourceLocation SIEGE_YAW_PITCH_S2C_ID = new ResourceLocation(StoneyCore.MOD_ID, "siege_yaw_pitch_s2c");
    public static final ResourceLocation LAND_CLIENT_DATA_S2C_ID = new ResourceLocation(StoneyCore.MOD_ID, "land_client_data_s2c");
    public static final ResourceLocation TOGGLE_VISOR_ID = new ResourceLocation(StoneyCore.MOD_ID, "toggle_visor_c2s");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ATTACK_ID, AttackC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(RELOAD_PACKET_ID, ReloadC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(SIEGE_YAW_PITCH_C2S_ID, SiegeYawC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_VISOR_ID, ToogleVisorC2SPacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(STAMINA_BLOCKED_ID, StaminaBlockedS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(LAND_TITLE_PACKET_ID, LandTitleS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(OUTLINE_CLAIM_PACKET_ID, OutlineClaimS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(SIEGE_YAW_PITCH_S2C_ID, SiegeYawS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(LAND_CLIENT_DATA_S2C_ID, LandClientDataS2CPacket::receive);
    }
}