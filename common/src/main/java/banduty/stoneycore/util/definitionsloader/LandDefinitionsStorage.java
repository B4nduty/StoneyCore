package banduty.stoneycore.util.definitionsloader;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LandDefinitionsStorage {
    protected static final Map<ResourceLocation, LandValues> DEFINITIONS = new ConcurrentHashMap<>();

    public static LandValues getData(ResourceLocation landId) {
        return DEFINITIONS.getOrDefault(landId, getDefaultData());
    }

    public static boolean containsLand(ResourceLocation landId) {
        return DEFINITIONS.containsKey(landId);
    }

    public static void clearDefinitions() {
        DEFINITIONS.clear();
    }

    public static void addDefinition(ResourceLocation id, LandValues data) {
        DEFINITIONS.put(id, data);
    }

    private static LandValues getDefaultData() {
        return new LandValues(0, Map.of(), "", -1);
    }
}