package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.packet.AttackC2SPacket;
import banduty.stoneycore.networking.packet.ReloadC2SPacket;
import banduty.stoneycore.networking.packet.StaminaBlockedS2CPacket;
import banduty.stoneycore.networking.packet.StaminaFloatS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModMessages {
    public static final Identifier STAMINA_FLOAT_ID = new Identifier(StoneyCore.MOD_ID, "stamina_float");
    public static final Identifier STAMINA_BLOCKED_ID = new Identifier(StoneyCore.MOD_ID, "stamina_blocked");
    public static final Identifier ATTACK_ID = new Identifier(StoneyCore.MOD_ID, "attack");
    public static final Identifier RELOAD_PACKET_ID = new Identifier(StoneyCore.MOD_ID, "reload_packet");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ATTACK_ID, AttackC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(RELOAD_PACKET_ID, ReloadC2SPacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(STAMINA_FLOAT_ID, StaminaFloatS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(STAMINA_BLOCKED_ID, StaminaBlockedS2CPacket::receive);
    }
}