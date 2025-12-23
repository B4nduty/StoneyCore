package banduty.stoneycore.entity;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(StoneyCore.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<SCBulletEntity>> SC_BULLET =
            ENTITY_TYPES.register("sc_bullet", () ->
                    FabricEntityTypeBuilder.<SCBulletEntity>create(MobCategory.MISC, SCBulletEntity::new)
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
