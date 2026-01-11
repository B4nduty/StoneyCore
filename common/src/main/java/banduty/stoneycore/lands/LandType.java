package banduty.stoneycore.lands;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public record LandType(ResourceLocation id, Block coreBlock, Item coreItem, int baseRadius,
                       Map<Item, Integer> itemsToExpand, String expandFormula, TerrainType terrainType, int maxAllies) {
    @Override
    public String toString() {
        return "LandType{id=" + id + ", block=" + coreBlock + ", item=" + coreItem + ", baseRadius=" + baseRadius + ", itemsToExpand=" + itemsToExpand + ", expandFormula=" + expandFormula + ", terrainType=" + terrainType + "}";
    }

    public boolean isOf(TerrainType terrainType) {
        return this.terrainType() == terrainType;
    }

    public enum TerrainType {GROUND, WATER, LAVA, GW, GL, WL, GWL}
}