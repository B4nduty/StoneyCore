package banduty.stoneycore.networking;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class SCPayload {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);

        registrar.playToServer(AttackC2SPacket.TYPE, AttackC2SPacket.STREAM_CODEC, AttackC2SPacket::handle);
        registrar.playToServer(ReloadC2SPacket.TYPE, ReloadC2SPacket.STREAM_CODEC, ReloadC2SPacket::handle);
        registrar.playToServer(SiegeYawC2SPacket.TYPE, SiegeYawC2SPacket.STREAM_CODEC, SiegeYawC2SPacket::handle);
        registrar.playToServer(ToggleVisorC2SPacket.TYPE, ToggleVisorC2SPacket.STREAM_CODEC, ToggleVisorC2SPacket::handle);

        registrar.playToClient(LandClientDataS2CPacket.TYPE, LandClientDataS2CPacket.STREAM_CODEC, LandClientDataS2CPacket::handle);
        registrar.playToClient(LandTitleS2CPacket.TYPE, LandTitleS2CPacket.STREAM_CODEC, LandTitleS2CPacket::handle);
        registrar.playToClient(OutlineClaimS2CPacket.TYPE, OutlineClaimS2CPacket.STREAM_CODEC, OutlineClaimS2CPacket::handle);
        registrar.playToClient(SiegeYawS2CPacket.TYPE, SiegeYawS2CPacket.STREAM_CODEC, SiegeYawS2CPacket::handle);
        registrar.playToClient(StaminaBlockedS2CPacket.TYPE, StaminaBlockedS2CPacket.STREAM_CODEC, StaminaBlockedS2CPacket::handle);
        registrar.playToClient(StaminaValueS2CPacket.TYPE, StaminaValueS2CPacket.STREAM_CODEC, StaminaValueS2CPacket::handle);
        registrar.playToClient(SyncDefinitionsPacket.TYPE, SyncDefinitionsPacket.STREAM_CODEC, SyncDefinitionsPacket::handle);
    }
}
