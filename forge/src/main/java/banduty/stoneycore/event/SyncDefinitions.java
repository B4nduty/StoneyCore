package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.SyncDefinitionsPacket;
import banduty.stoneycore.util.definitionsloader.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SyncDefinitions {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModMessages.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncDefinitionsPacket(
                    ArmorDefinitionsStorage.getDefinitions(),
                    AccessoriesDefinitionsStorage.getDefinitions(),
                    LandDefinitionsStorage.getDefinitions(),
                    SiegeEngineDefinitionsStorage.getDefinitions(),
                    WeaponDefinitionsStorage.getDefinitions()
            ));
        }
    }
}
