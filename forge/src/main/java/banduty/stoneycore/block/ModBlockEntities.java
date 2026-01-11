package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModBlockEntities {
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StoneyCore.MOD_ID);

    RegistryObject<BlockEntityType<CraftmanAnvilBlockEntity>> CRAFTMAN_ANVIL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("craftman_anvil_block_entity",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new CraftmanAnvilBlockEntity(ModBlockEntities.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(), pos, state),
                            ModBlocks.CRAFTMAN_ANVIL.get()
                    ).build(null)
            );

    static void registerBlockEntities(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
        StoneyCore.LOG.info("Registering Mod Block Entities for " + StoneyCore.MOD_ID);
    }
}