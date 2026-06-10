package banduty.stoneycore.client.render;

import net.minecraft.world.item.Item;

import java.util.IdentityHashMap;
import java.util.Map;

public class ArmorAttachmentRenderManager {
    private static final Map<Item, ArmorAttachmentRenderer> CACHE = new IdentityHashMap<>();

    public static ArmorAttachmentRenderer getRenderer(Item item) {
        if (CACHE.containsKey(item)) {
            return CACHE.get(item);
        }

        if (item instanceof ArmorAttachmentRenderProvider provider) {
            ArmorAttachmentRenderer renderer = provider.getRenderer();
            CACHE.put(item, renderer);
            return renderer;
        }

        CACHE.put(item, null);
        return null;
    }
}