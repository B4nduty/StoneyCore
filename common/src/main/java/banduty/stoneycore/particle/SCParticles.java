package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public interface SCParticles {
    Supplier<SimpleParticleType> MUZZLES_SMOKE_PARTICLE = registerSimpleParticleType("muzzles_smoke_particle", true);
    Supplier<SimpleParticleType> MUZZLES_FLASH_PARTICLE = registerSimpleParticleType("muzzles_flash_particle", false);

    @SuppressWarnings("unchecked")
    private static Supplier<SimpleParticleType> registerSimpleParticleType(String name, boolean overrideLimiter) {
        return Services.PLATFORM.register(
                (Registry<SimpleParticleType>) (Registry<?>) BuiltInRegistries.PARTICLE_TYPE,
                name,
                () -> new SimpleParticleType(overrideLimiter) {}
        );
    }

    static void register() {
        StoneyCore.LOG.info("Registering Particles for " + StoneyCore.MOD_ID);
    }
}