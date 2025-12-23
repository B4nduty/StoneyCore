package banduty.stoneycore.block;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static final BlockEntityType<CraftmanAnvilBlockEntity> CRAFTMAN_ANVIL_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(StoneyCore.MOD_ID, "craftman_anvil_block_entity"),
            BlockEntityType.Builder.of(CraftmanAnvilBlockEntity::new, ModBlocks.CRAFTMAN_ANVIL).build(null)
    );

    public static void registerBlockEntities() {
        StoneyCore.LOGGER.info("Registering ModBlockEntities for " + StoneyCore.MOD_ID);
    }
}