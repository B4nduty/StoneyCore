package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public interface ModBlocks {
    DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, StoneyCore.MOD_ID);
    DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(Registries.ITEM, StoneyCore.MOD_ID);

    RegistryObject<Block> CRAFTMAN_ANVIL = registerBlock("craftman_anvil",
            () -> new ForgeCraftmanAnvilBlock(BlockBehaviour.Properties.copy(Blocks.ANVIL)
                    .sound(SoundType.ANVIL)
                    .noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Add blocks to creative tabs
    private static void addBlocksToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CRAFTMAN_ANVIL.get());
        }
    }

    static void registerBlocks(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
        eventBus.addListener(ModBlocks::addBlocksToCreativeTabs);
        StoneyCore.LOG.info("Registering Mod Blocks for " + StoneyCore.MOD_ID);
    }
}