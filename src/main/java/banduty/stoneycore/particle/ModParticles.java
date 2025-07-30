package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.RegistryKeys;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(StoneyCore.MOD_ID, RegistryKeys.PARTICLE_TYPE);

    public static final RegistrySupplier<DefaultParticleType> MUZZLES_SMOKE_PARTICLE =
            PARTICLE_TYPES.register("muzzles_smoke_particle", FabricParticleTypes::simple);
    public static final RegistrySupplier<DefaultParticleType> MUZZLES_FLASH_PARTICLE =
            PARTICLE_TYPES.register("muzzles_flash_particle", FabricParticleTypes::simple);

    public static void registerParticles() {
        PARTICLE_TYPES.register();
        StoneyCore.LOGGER.info("Registering Particles for " + StoneyCore.MOD_ID);
    }
}
