package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class MuzzlesSmokeParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    public MuzzlesSmokeParticle(ClientWorld world, double xCoord, double yCoord, double zCoord,
                                SpriteProvider spriteSet, double xd, double yd, double zd) {
        super(world, xCoord, yCoord, zCoord, xd, yd, zd);

        this.velocityMultiplier = Math.max(0.99f + (StoneyCore.getConfig().getMuzzlesSmokeParticlesTime() / 20000f), 0.997f);
        this.velocityX = xd;
        this.velocityY = yd;
        this.velocityZ = zd;

        this.velocityX += (world.random.nextDouble() - 0.5) * 0.02;
        this.velocityY += world.random.nextDouble() * 0.02;
        this.velocityZ += (world.random.nextDouble() - 0.5) * 0.02;

        this.scale *= 0.6f;
        this.maxAge = StoneyCore.getConfig().getMuzzlesSmokeParticlesTime() * 20;
        this.spriteProvider = spriteSet;
        this.setSpriteForAge(spriteSet);

        this.red = 1f;
        this.green = 1f;
        this.blue = 1f;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.dead) {
            this.setSpriteForAge(this.spriteProvider);
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider spriteProvider) {
            this.sprites = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType particleType, ClientWorld clientWorld,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new MuzzlesSmokeParticle(clientWorld, x, y, z, this.sprites, xd, yd, zd);
        }
    }
}