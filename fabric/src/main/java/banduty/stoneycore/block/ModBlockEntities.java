package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface ModBlockEntities {
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(StoneyCore.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    RegistrySupplier<BlockEntityType<CraftmanAnvilBlockEntity>> CRAFTMAN_ANVIL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("craftman_anvil_block_entity",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new CraftmanAnvilBlockEntity(ModBlockEntities.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(), pos, state),
                            ModBlocks.CRAFTMAN_ANVIL.get()
                    ).build(null)
            );

    static void registerBlockEntities() {
        BLOCK_ENTITIES.register();
        StoneyCore.LOG.info("Registering Mod Block Entities for " + StoneyCore.MOD_ID);
    }
}