package banduty.stoneycore.entity;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public interface ModEntities {
    EntityType<SCBulletEntity> SC_BULLET =
            registerEntity("sc_bullet",
                    FabricEntityTypeBuilder.<SCBulletEntity>create(MobCategory.MISC, SCBulletEntity::new)
                            .dimensions(EntityDimensions.fixed(0.05f, 0.05f))
                            .trackRangeBlocks(64)
                            .trackedUpdateRate(10)
                            .build()
            );

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType<T> entityType) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(StoneyCore.MOD_ID, name), entityType);
    }

    static void registerEntities() {
        StoneyCore.LOG.info("Registering Entities for " + StoneyCore.MOD_ID);
    }
}