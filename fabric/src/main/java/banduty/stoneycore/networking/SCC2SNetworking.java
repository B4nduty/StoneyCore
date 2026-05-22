package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.AttackC2SPacket;
import banduty.stoneycore.networking.payload.ReloadC2SPacket;
import banduty.stoneycore.networking.payload.SiegeYawC2SPacket;
import banduty.stoneycore.networking.payload.ToggleVisorC2SPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SCC2SNetworking {
    static {
        ServerPlayNetworking.registerGlobalReceiver(AttackC2SPacket.ID, AttackC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ReloadC2SPacket.ID, ReloadC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(SiegeYawC2SPacket.ID, SiegeYawC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ToggleVisorC2SPacket.ID, ToggleVisorC2SPacket::handle);
    }

    public static void registerC2SNetworking() {
        StoneyCore.LOG.info("Registering C2S Networking for " + StoneyCore.MOD_ID);
    }
}
