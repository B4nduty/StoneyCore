package banduty.stoneycore.lands;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.*;

public class LandTypeRegistry {
    private static final Map<Identifier, LandType> TYPES = new LinkedHashMap<>();

    public static LandType register(Identifier id, Block coreBlock, Item coreItem, int baseRadius, Map<Item, Integer> itemsToExpand, String expandFormula, LandType.TerrainType terrainType) {
        if (TYPES.containsKey(id)) {
            throw new IllegalArgumentException("LandType with id " + id + " is already registered!");
        }

        boolean blockAlreadyUsed = TYPES.values().stream()
                .anyMatch(type -> type.coreBlock() == coreBlock);
        if (blockAlreadyUsed) {
            throw new IllegalArgumentException("A LandType with core block " + coreBlock + " is already registered!");
        }

        LandType type = new LandType(id, coreBlock, coreItem, baseRadius, itemsToExpand, expandFormula, terrainType);
        TYPES.put(id, type);
        return type;
    }

    public static Optional<LandType> getById(Identifier id) {
        return Optional.ofNullable(TYPES.get(id));
    }

    public static Optional<LandType> getByBlock(Block block) {
        return TYPES.values().stream()
                .filter(type -> type.coreBlock() == block)
                .findFirst();
    }

    public static Collection<LandType> getAll() {
        return Collections.unmodifiableCollection(TYPES.values());
    }
}