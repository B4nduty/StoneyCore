package banduty.stoneycore.entity;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.entity.custom.SCBulletEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface ModEntities {
    DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, StoneyCore.MOD_ID);

    RegistryObject<EntityType<SCBulletEntity>> SC_BULLET =
            ENTITY_TYPES.register("sc_bullet", () ->
                    EntityType.Builder.<SCBulletEntity>of(SCBulletEntity::new, MobCategory.MISC)
                            .sized(0.05f, 0.05f)
                            .clientTrackingRange(64)
                            .updateInterval(10)
                            .build("sc_bullet")
            );

    static void registerEntities(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        StoneyCore.LOG.info("Registering Entities for " + StoneyCore.MOD_ID);
    }
}