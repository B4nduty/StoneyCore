package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.lands.util.LandState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerNameTagHandler implements PlayerNameTagEvents {
    @Override
    public List<TagEntry> collectTags(ServerPlayer player) {
        var serverLevel = player.serverLevel();
        var playerId = player.getUUID();

        return LandState.get(serverLevel).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst()
                .map(land -> {
                    List<TagEntry> entries = new ArrayList<>();
                    entries.add(new TagEntry(land.getLandTag(serverLevel), 0));

                    String titleName = land.getPlayerTitle(playerId);
                    if (titleName != null && !titleName.isBlank()) {
                        Integer colorInt = land.getTitles().get(titleName);
                        TextColor color = colorInt != null ? TextColor.fromRgb(colorInt) : TextColor.fromLegacyFormat(ChatFormatting.WHITE);

                        Component titleComponent = Component.literal("[" + titleName + "]")
                                .setStyle(Style.EMPTY.withColor(color));
                        entries.add(new TagEntry(titleComponent, 1));
                    }
                    return entries;
                })
                .orElse(List.of());
    }
}
