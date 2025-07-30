package banduty.stoneycore.structure;

import net.minecraft.util.Identifier;

import java.util.*;

public class StructureSpawnRegistry {
    private static final Map<Identifier, StructureSpawner> STRUCTURES = new HashMap<>();

    public static void register(Identifier id, StructureSpawner structure) {
        if (STRUCTURES.containsKey(id)) {
            throw new IllegalStateException("Structure with ID '" + id + "' is already registered.");
        }

        STRUCTURES.put(id, structure);
    }

    public static StructureSpawner get(Identifier id) {
        return STRUCTURES.get(id);
    }

    public static Identifier getId(StructureSpawner structureSpawner) {
        for (Map.Entry<Identifier, StructureSpawner> entry : STRUCTURES.entrySet()) {
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
