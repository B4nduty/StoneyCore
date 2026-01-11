package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public interface ModParticles {
    DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(StoneyCore.MOD_ID, Registries.PARTICLE_TYPE);

    RegistrySupplier<SimpleParticleType> MUZZLES_SMOKE_PARTICLE =
            PARTICLE_TYPES.register("muzzles_smoke_particle", FabricParticleTypes::simple);
    RegistrySupplier<SimpleParticleType> MUZZLES_FLASH_PARTICLE =
            PARTICLE_TYPES.register("muzzles_flash_particle", FabricParticleTypes::simple);

    static void registerParticles() {
        PARTICLE_TYPES.register();
        StoneyCore.LOG.info("Registering Particles for " + StoneyCore.MOD_ID);
    }
}
