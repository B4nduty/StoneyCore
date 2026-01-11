package banduty.stoneycore.lands;

import banduty.stoneycore.util.definitionsloader.LandValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class LandTypeRegistry {
    private static final Map<ResourceLocation, LandType> TYPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, LandValues> OVERRIDES = new HashMap<>();

    public static LandType register(ResourceLocation id, Block coreBlock, Item coreItem, int baseRadius,
                                    Map<Item, Integer> itemsToExpand, String expandFormula, LandType.TerrainType terrainType, int maxAllies) {
        if (TYPES.containsKey(id)) {
            throw new IllegalArgumentException("LandType with id " + id + " is already registered!");
        }
        boolean blockAlreadyUsed = TYPES.values().stream()
                .anyMatch(type -> type.coreBlock() == coreBlock);
        if (blockAlreadyUsed) {
            throw new IllegalArgumentException("A LandType with core block " + coreBlock + " is already registered!");
        }

        LandType type = new LandType(id, coreBlock, coreItem, baseRadius, itemsToExpand, expandFormula, terrainType, maxAllies);
        TYPES.put(id, type);
        return type;
    }

    public static void applyOverride(ResourceLocation id, LandValues values) {
        OVERRIDES.put(id, values);
    }

    public static void clearOverrides() {
        OVERRIDES.clear();
    }

    public static Optional<LandType> getById(ResourceLocation id) {
        LandType type = TYPES.get(id);
        if (type == null) return Optional.empty();

        LandValues override = OVERRIDES.get(id);
        if (override != null) {
            return Optional.of(new LandType(
                    type.id(),
                    type.coreBlock(),
                    type.coreItem(),
                    override.baseRadius() <= 0 ? type.baseRadius() : override.baseRadius(),
                    override.itemsToExpand().isEmpty() ? type.itemsToExpand() : override.itemsToExpand(),
                    override.expandFormula().isEmpty() ? type.expandFormula() : override.expandFormula(),
                    type.terrainType(),
                    override.maxAllies()
            ));
        }
        return Optional.of(type);
    }

    public static Optional<LandType> getByBlock(Block block) {
        return TYPES.values().stream()
                .filter(type -> type.coreBlock() == block)
                .findFirst()
                .flatMap(type -> getById(type.id()));
    }

    public static Collection<LandType> getAll() {
        return TYPES.keySet().stream()
                .map(id -> getById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}