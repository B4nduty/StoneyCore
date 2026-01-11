package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.LandState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerNameTagHandler {

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var serverLevel = player.serverLevel();
        var playerId = player.getUUID();

        // Find the first land where player is owner or ally
        var landTag = LandState.get(serverLevel).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst()
                .map(land -> land.getLandTag(serverLevel))
                .orElse(null);

        if (landTag != null) {
            // Append land tag to player name
            String formattedName = player.getName().getString() + " " + landTag.getString();
            event.setDisplayname(Component.literal(formattedName));
        }
    }
}