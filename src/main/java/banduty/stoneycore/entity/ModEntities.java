package banduty.stoneycore.entity;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKeys;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(StoneyCore.MOD_ID, RegistryKeys.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<SCBulletEntity>> SC_BULLET =
            ENTITY_TYPES.register("sc_bullet", () ->
                    FabricEntityTypeBuilder.<SCBulletEntity>create(SpawnGroup.MISC, SCBulletEntity::new)
                            .dimensions(EntityDimensions.fixed(0.05f, 0.05f))
                            .trackRangeBlocks(64)
                            .trackedUpdateRate(10)
                            .build()
            );

    public static void registerEntities() {
        ENTITY_TYPES.register();
        StoneyCore.LOGGER.info("Registering Entities for " + StoneyCore.MOD_ID);
    }
}
