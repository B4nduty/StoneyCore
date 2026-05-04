package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public interface SCPayloads {
    ResourceLocation ATTACK_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "attack");
    ResourceLocation RELOAD_PACKET_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "reload_packet");
    ResourceLocation SIEGE_YAW_PITCH_C2S_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "siege_yaw_pitch_c2s");
    ResourceLocation TOGGLE_VISOR_ID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "toggle_visor_c2s");

    static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(AttackC2SPacket.ID, AttackC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ReloadC2SPacket.ID, ReloadC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SiegeYawC2SPacket.ID, SiegeYawC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleVisorC2SPacket.ID, ToggleVisorC2SPacket.CODEC);
    }

    static void registerC2SReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(AttackC2SPacket.ID, AttackC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ReloadC2SPacket.ID, ReloadC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(SiegeYawC2SPacket.ID, SiegeYawC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ToggleVisorC2SPacket.ID, ToggleVisorC2SPacket::handle);
    }
}