package banduty.stoneycore.structure;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StructureSpawnRegistry {
    private static final Map<ResourceLocation, StructureSpawner> STRUCTURES = new HashMap<>();

    public static void register(ResourceLocation id, StructureSpawner structure) {
        if (STRUCTURES.containsKey(id)) {
            throw new IllegalStateException("Structure with ID '" + id + "' is already registered.");
        }

        STRUCTURES.put(id, structure);
    }

    public static StructureSpawner get(ResourceLocation id) {
        return STRUCTURES.get(id);
    }

    public static ResourceLocation getId(StructureSpawner structureSpawner) {
        for (Map.Entry<ResourceLocation, StructureSpawner> entry : STRUCTURES.entrySet()) {
            if (entry.getValue().equals(structureSpawner)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Collection<StructureSpawner> getAll() {
        return STRUCTURES.values();
    }
}
