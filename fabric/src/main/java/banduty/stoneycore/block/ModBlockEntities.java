package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface ModBlockEntities {
    BlockEntityType<CraftmanAnvilBlockEntity> CRAFTMAN_ANVIL_BLOCK_ENTITY =
            registerBlockEntities("craftman_anvil_block_entity",
                    BlockEntityType.Builder.of(
                            (pos, state) -> new CraftmanAnvilBlockEntity(ModBlockEntities.CRAFTMAN_ANVIL_BLOCK_ENTITY, pos, state),
                            ModBlocks.CRAFTMAN_ANVIL
                    ).build(null)
            );

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntities(String name, BlockEntityType<T> blockEntityType) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(StoneyCore.MOD_ID, name), blockEntityType);
    }

    static void registerBlockEntities() {
        StoneyCore.LOG.info("Registering Mod Block Entities for " + StoneyCore.MOD_ID);
    }
}