package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;

public interface PlayerNameTagEvents {
    Event<PlayerNameTagEvents> EVENT = EventFactory.createArrayBacked(
            PlayerNameTagEvents.class,
            listeners -> (player) -> {
                List<TagEntry> tags = new java.util.ArrayList<>();
                for (PlayerNameTagEvents listener : listeners) {
                    tags.addAll(listener.collectTags(player));
                }
                tags.sort(Comparator.comparingInt(TagEntry::priority));
                return tags;
            }
    );

    List<TagEntry> collectTags(ServerPlayerEntity player);

    record TagEntry(Text text, int priority) {}
}
