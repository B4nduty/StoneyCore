package banduty.stoneycore.client.render;

import net.minecraft.world.item.Item;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class AccessoryRenderManager {
    private static final Map<Item, AccessoryRenderer> CACHE = new IdentityHashMap<>();

    public static Optional<AccessoryRenderer> getOrLookUp(Item item) {
        if (CACHE.containsKey(item)) {
            return Optional.ofNullable(CACHE.get(item));
        }

        if (item instanceof AccessoryRenderProvider provider) {
            AccessoryRenderer renderer = provider.getRenderer();
            CACHE.put(item, renderer);
            return Optional.ofNullable(renderer);
        }

        CACHE.put(item, null);
        return Optional.empty();
    }
}