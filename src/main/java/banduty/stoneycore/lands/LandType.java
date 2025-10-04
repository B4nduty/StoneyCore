package banduty.stoneycore.lands;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record LandType(Identifier id, Block coreBlock, Item coreItem, int baseRadius,
                       Map<Item, Integer> itemsToExpand, String expandFormula, TerrainType terrainType, int maxAllies) {
    @Override
    public @NotNull String toString() {
        return "LandType{id=" + id + ", block=" + coreBlock + ", item=" + coreItem + ", baseRadius=" + baseRadius + ", itemsToExpand=" + itemsToExpand + ", expandFormula=" + expandFormula + ", terrainType=" + terrainType + "}";
    }

    public boolean isOf(TerrainType terrainType) {
        return this.terrainType() == terrainType;
    }

    public enum TerrainType {GROUND, WATER, LAVA, GW, GL, WL, GWL}
}