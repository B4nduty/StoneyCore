package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public interface ModParticles {
    SimpleParticleType MUZZLES_SMOKE_PARTICLE =
            registerSimpleParticleType("muzzles_smoke_particle");
    SimpleParticleType MUZZLES_FLASH_PARTICLE =
            registerSimpleParticleType("muzzles_flash_particle");

    private static SimpleParticleType registerSimpleParticleType(String name) {
        SimpleParticleType particleType = FabricParticleTypes.simple();
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(StoneyCore.MOD_ID, name), particleType);
    }

    static void registerParticles() {
        StoneyCore.LOG.info("Registering Particles for " + StoneyCore.MOD_ID);
    }
}
