package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public interface ModBlocks {
    Block CRAFTMAN_ANVIL = registerBlock("craftman_anvil",
            new FabricCraftmanAnvilBlock(FabricBlockSettings.copy(Blocks.ANVIL)
                    .sound(SoundType.ANVIL)
                    .noOcclusion()));

    private static Block registerBlock(String name, Block block) {
        Block blockItem = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(StoneyCore.MOD_ID, name), block);
        registerBlockItem(name, blockItem);
        return blockItem;
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(StoneyCore.MOD_ID, name), new BlockItem(block, new Item.Properties()));
    }

    private static void addBlocksToFunctionalBlocksItemGroup(FabricItemGroupEntries entries) {
        entries.accept(CRAFTMAN_ANVIL);
    }

    static void registerBlocks() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(ModBlocks::addBlocksToFunctionalBlocksItemGroup);
        StoneyCore.LOG.info("Registering Mod Blocks for " + StoneyCore.MOD_ID);
    }
}