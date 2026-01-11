package banduty.stoneycore.particle;

import net.minecraft.core.particles.SimpleParticleType;

public class ForgeParticleHelper implements ParticleHelper {
    @Override
    public SimpleParticleType getMuzzlesSmokeParticle() {
        return ModParticles.MUZZLES_SMOKE_PARTICLE.get();
    }

    @Override
    public SimpleParticleType getMuzzlesFlashParticle() {
        return ModParticles.MUZZLES_FLASH_PARTICLE.get();
    }
}
