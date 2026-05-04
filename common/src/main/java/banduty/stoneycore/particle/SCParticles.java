package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public interface SCParticles {
    SimpleParticleType MUZZLES_SMOKE_PARTICLE = registerSimpleParticleType("muzzles_smoke_particle", true);
    SimpleParticleType MUZZLES_FLASH_PARTICLE = registerSimpleParticleType("muzzles_flash_particle", false);

    private static SimpleParticleType registerSimpleParticleType(String name, boolean overrideLimiter) {
        return (SimpleParticleType) Services.PLATFORM.register(BuiltInRegistries.PARTICLE_TYPE, name, () -> new SimpleParticleType(overrideLimiter) {}).get();
    }

    static void register() {
        StoneyCore.LOG.info("Registering Particles for " + StoneyCore.MOD_ID);
    }
}