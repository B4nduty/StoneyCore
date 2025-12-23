package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(StoneyCore.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> MUZZLES_SMOKE_PARTICLE =
            PARTICLE_TYPES.register("muzzles_smoke_particle", FabricParticleTypes::simple);
    public static final RegistrySupplier<SimpleParticleType> MUZZLES_FLASH_PARTICLE =
            PARTICLE_TYPES.register("muzzles_flash_particle", FabricParticleTypes::simple);

    public static void registerParticles() {
        PARTICLE_TYPES.register();
        StoneyCore.LOGGER.info("Registering Particles for " + StoneyCore.MOD_ID);
    }
}
