package banduty.stoneycore.entity;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public interface SCEntities {

    Supplier<EntityType<SCBulletEntity>> SC_BULLET = registerEntity("sc_bullet",
            () -> EntityType.Builder.<SCBulletEntity>of(SCBulletEntity::new, MobCategory.MISC)
                    .sized(0.05f, 0.05f)
                    .clientTrackingRange(64)
                    .updateInterval(10).build("sc_bullet")
    );

    @SuppressWarnings("unchecked")
    private static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType<T>> entitySupplier) {
        return Services.PLATFORM.register((Registry<EntityType<T>>) (Registry<?>) BuiltInRegistries.ENTITY_TYPE, name, entitySupplier);
    }

    static void register() {
        StoneyCore.LOG.info("Registering Entities for " + StoneyCore.MOD_ID);
    }
}