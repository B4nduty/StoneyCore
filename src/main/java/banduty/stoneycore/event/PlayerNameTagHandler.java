package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.lands.util.LandState;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class PlayerNameTagHandler implements PlayerNameTagEvents {
    @Override
    public List<TagEntry> collectTags(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        var playerId = player.getUuid();

        return LandState.get(world).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst()
                .map(land -> List.of(new PlayerNameTagEvents.TagEntry(land.getLandTag(world), 0)))
                .orElse(List.of());
    }
}
