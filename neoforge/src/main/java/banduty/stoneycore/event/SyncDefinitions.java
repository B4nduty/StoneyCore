package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.SyncDefinitionsPacket;
import banduty.stoneycore.definitions.*;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
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
            sendDefinitions(player);
        }
    }

    /** On /reload (player is null), resync definitions and refresh under-armor attributes for everyone online. */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) return; // joins are handled by onPlayerJoin
        event.getRelevantPlayers().forEach(player -> {
            sendDefinitions(player);
            SCUnderArmor.refreshAttachmentAttributes(player);
        });
    }

    private static void sendDefinitions(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncDefinitionsPacket(
                ArmorDefinitionsStorage.getDefinitions(),
                ArmorAttachmentDefinitionsStorage.getDefinitions(),
                ArmorAttachmentSlotDefinitionsStorage.getDefinitions(),
                LandDefinitionsStorage.getDefinitions(),
                SiegeEngineDefinitionsStorage.getDefinitions(),
                WeaponDefinitionsStorage.getDefinitions()
        ));
    }
}
