package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public interface SCBlocks {
    Supplier<Block> CRAFTMAN_ANVIL = registerBlock("craftman_anvil",
            () -> new CraftmanAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL)
                    .sound(SoundType.ANVIL)
                    .noOcclusion()));

    Supplier<BlockEntityType<CraftmanAnvilBlockEntity>> CRAFTMAN_ANVIL_BLOCK_ENTITY =
            Services.PLATFORM.registerBlockEntityType(
                    "craftman_anvil_block_entity",
                    CraftmanAnvilBlockEntity::new,
                    CRAFTMAN_ANVIL
            );

    private static Supplier<Block> registerBlock(String name, Supplier<Block> blockSupplier) {
        Supplier<Block> registeredBlock = Services.PLATFORM.register(BuiltInRegistries.BLOCK, name, blockSupplier);

        registerBlockItem(name, registeredBlock);

        return registeredBlock;
    }

    private static void registerBlockItem(String name, Supplier<Block> block) {
        Services.PLATFORM.register(BuiltInRegistries.ITEM, name,
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    static void register() {
        StoneyCore.LOG.info("Registering Mod Blocks for " + StoneyCore.MOD_ID);
    }
}