package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

import java.util.function.Supplier;

public interface ModBlocks {
    DeferredRegister<Block> BLOCKS = DeferredRegister.create(StoneyCore.MOD_ID, Registries.BLOCK);
    DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(StoneyCore.MOD_ID, Registries.ITEM);

    RegistrySupplier<Block> CRAFTMAN_ANVIL = registerBlock("craftman_anvil",
            () -> new FabricCraftmanAnvilBlock(FabricBlockSettings.copy(Blocks.ANVIL)
                    .sound(SoundType.ANVIL)
                    .noOcclusion()));

    private static <T extends Block> RegistrySupplier<T> registerBlock(String name, Supplier<T> block) {
        RegistrySupplier<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistrySupplier<T> block) {
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static void addBlocksToFunctionalBlocksItemGroup(FabricItemGroupEntries entries) {
        entries.accept(CRAFTMAN_ANVIL.get());
    }

    static void registerBlocks() {
        BLOCKS.register();
        BLOCK_ITEMS.register();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(ModBlocks::addBlocksToFunctionalBlocksItemGroup);
        StoneyCore.LOG.info("Registering Mod Blocks for " + StoneyCore.MOD_ID);
    }
}