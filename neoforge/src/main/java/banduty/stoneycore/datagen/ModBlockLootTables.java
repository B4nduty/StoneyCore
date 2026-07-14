package banduty.stoneycore.datagen;

import banduty.stoneycore.block.SCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(SCBlocks.CRAFTMAN_ANVIL.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(
                SCBlocks.CRAFTMAN_ANVIL.get()
        );
    }
}