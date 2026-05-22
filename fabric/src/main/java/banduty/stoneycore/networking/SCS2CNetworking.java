package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SCS2CNetworking {
    static {
        ClientPlayNetworking.registerGlobalReceiver(StaminaBlockedS2CPacket.ID, StaminaBlockedS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(SyncDefinitionsPacket.ID, SyncDefinitionsPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(LandTitleS2CPacket.ID, LandTitleS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(OutlineClaimS2CPacket.ID, OutlineClaimS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(SiegeYawS2CPacket.ID, SiegeYawS2CPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(LandClientDataS2CPacket.ID, LandClientDataS2CPacket::handle);
    }

    public static void registerS2CNetworking() {
        StoneyCore.LOG.info("Registering S2C Networking for " + StoneyCore.MOD_ID);
    }
}
