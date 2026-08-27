package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerNameTagHandler {

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var serverLevel = player.serverLevel();
        var playerId = player.getUUID();

        // Find the first land where player is owner or ally
        var landOpt = LandState.get(serverLevel).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst();

        if (landOpt.isPresent()) {
            var land = landOpt.get();
            MutableComponent finalDisplayName = Component.empty();

            finalDisplayName.append(land.getLandTag(serverLevel)).append(" ");

            String titleName = land.getPlayerTitle(playerId);
            if (titleName != null && !titleName.isBlank()) {
                Integer colorInt = land.getTitles().get(titleName);
                TextColor color = colorInt != null ? TextColor.fromRgb(colorInt) : TextColor.fromLegacyFormat(ChatFormatting.WHITE);

                Component titleComponent = Component.literal("[" + titleName + "]")
                        .setStyle(Style.EMPTY.withColor(color));

                finalDisplayName.append(titleComponent).append(" ");
            }

            finalDisplayName.append(event.getUsername());

            event.setDisplayname(finalDisplayName);
        }
    }
}