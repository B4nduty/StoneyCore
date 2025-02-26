package banduty.stoneycore.entity;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCArrowEntity;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<SCArrowEntity> SC_ARROW = registerEntity("sc_arrow", SCArrowEntity::new, 0.5f, 0.5f);
    public static final EntityType<SCBulletEntity> SC_BULLET = registerEntity("sc_bullet", SCBulletEntity::new, 0.05f, 0.05f);

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.EntityFactory<T> factory, float width, float height) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(StoneyCore.MOD_ID, name),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory).dimensions(EntityDimensions.fixed(width, height)).build()
        );
    }

    public static void registerEntities() {
        StoneyCore.LOGGER.info("Registering Entities for " + StoneyCore.MOD_ID);
    }
}
