package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.SyncDefinitionsPacket;
import banduty.stoneycore.util.definitionsloader.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SyncDefinitions {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new SyncDefinitionsPacket(
                    ArmorDefinitionsStorage.getDefinitions(),
                    ArmorAttachmentDefinitionsStorage.getDefinitions(),
                    LandDefinitionsStorage.getDefinitions(),
                    SiegeEngineDefinitionsStorage.getDefinitions(),
                    WeaponDefinitionsStorage.getDefinitions()
            ));
        }
    }
}
