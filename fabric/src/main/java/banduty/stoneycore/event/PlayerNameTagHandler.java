package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.lands.util.LandState;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class PlayerNameTagHandler implements PlayerNameTagEvents {
    @Override
    public List<TagEntry> collectTags(ServerPlayer player) {
        var serverLevel = player.serverLevel();
        var playerId = player.getUUID();

        return LandState.get(serverLevel).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst()
                .map(land -> List.of(new TagEntry(land.getLandTag(serverLevel), 0)))
                .orElse(List.of());
    }
}
