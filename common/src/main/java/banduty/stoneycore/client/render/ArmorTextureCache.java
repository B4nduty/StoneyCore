package banduty.stoneycore.client.render;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public class ArmorTextureCache {
    private static final Map<String, ResourceLocation> BASE_CACHE = new HashMap<>();
    private static final Map<String, ResourceLocation> OVERLAY_CACHE = new HashMap<>();

    public static ResourceLocation getBaseTexture(String namespace, String path) {
        String key = namespace + ":" + path;
        return BASE_CACHE.computeIfAbsent(key, k ->
                ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + ".png")
        );
    }

    public static ResourceLocation getOverlayTexture(String namespace, String path) {
        String key = namespace + ":" + path;
        return OVERLAY_CACHE.computeIfAbsent(key, k ->
                ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + path + "_overlay.png")
        );
    }
}