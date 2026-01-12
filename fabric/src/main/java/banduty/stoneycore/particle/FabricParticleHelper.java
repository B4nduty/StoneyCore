package banduty.stoneycore.particle;

import net.minecraft.core.particles.SimpleParticleType;

public class FabricParticleHelper implements ParticleHelper {
    @Override
    public SimpleParticleType getMuzzlesSmokeParticle() {
        return ModParticles.MUZZLES_SMOKE_PARTICLE;
    }

    @Override
    public SimpleParticleType getMuzzlesFlashParticle() {
        return ModParticles.MUZZLES_FLASH_PARTICLE;
    }
}
