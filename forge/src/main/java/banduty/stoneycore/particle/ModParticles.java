package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface ModParticles {
    DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, StoneyCore.MOD_ID);

    RegistryObject<SimpleParticleType> MUZZLES_SMOKE_PARTICLE =
            PARTICLE_TYPES.register("muzzles_smoke_particle",
                    () -> new SimpleParticleType(false));

    RegistryObject<SimpleParticleType> MUZZLES_FLASH_PARTICLE =
            PARTICLE_TYPES.register("muzzles_flash_particle",
                    () -> new SimpleParticleType(false));

    static void registerParticles(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
        StoneyCore.LOG.info("Registering Particles for " + StoneyCore.MOD_ID);
    }
}