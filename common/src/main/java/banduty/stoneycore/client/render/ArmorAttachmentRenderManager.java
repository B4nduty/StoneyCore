package banduty.stoneycore.client.render;

import net.minecraft.world.item.Item;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class ArmorAttachmentRenderManager {
    private static final Map<Item, ArmorAttachmentRenderer> CACHE = new IdentityHashMap<>();

    public static Optional<ArmorAttachmentRenderer> getOrLookUp(Item item) {
        if (CACHE.containsKey(item)) {
            return Optional.ofNullable(CACHE.get(item));
        }

        if (item instanceof ArmorAttachmentRenderProvider provider) {
            ArmorAttachmentRenderer renderer = provider.getRenderer();
            CACHE.put(item, renderer);
            return Optional.ofNullable(renderer);
        }

        CACHE.put(item, null);
        return Optional.empty();
    }
}