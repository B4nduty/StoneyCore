package banduty.stoneycore.util.definitionsloader;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SiegeEngineDefinitionsStorage {
    protected static final Map<ResourceLocation, SiegeEngineDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    public static SiegeEngineDefinitionData getData(EntityType<?> entityType) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return DEFINITIONS.getOrDefault(entityId, SiegeEngineDefinitionData.DEFAULT);
    }

    public static boolean containsEntity(EntityType<?> entityType) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return DEFINITIONS.containsKey(entityId);
    }

    public static void clearDefinitions() {
        DEFINITIONS.clear();
    }

    public static void addDefinition(ResourceLocation id, SiegeEngineDefinitionData data) {
        DEFINITIONS.put(id, data);
    }
}