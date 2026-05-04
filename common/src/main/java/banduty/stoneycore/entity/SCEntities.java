package banduty.stoneycore.entity;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public interface SCEntities {

    EntityType<SCBulletEntity> SC_BULLET = registerEntity("sc_bullet",
            EntityType.Builder.<SCBulletEntity>of(SCBulletEntity::new, MobCategory.MISC)
                    .sized(0.05f, 0.05f)
                    .clientTrackingRange(64)
                    .updateInterval(10)
    );
    
    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        return (EntityType<T>) Services.PLATFORM.register(BuiltInRegistries.ENTITY_TYPE, name, () -> builder.build(name)).get();
    }

    static void register() {
        StoneyCore.LOG.info("Registering Entities for " + StoneyCore.MOD_ID);
    }
}