package banduty.stoneycore.particle;

import banduty.stoneycore.StoneyCore;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class MuzzlesSmokeParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public MuzzlesSmokeParticle(ClientLevel clientLevel, double xCoord, double yCoord, double zCoord,
                                SpriteSet spriteSet, double xd, double yd, double zd) {
        super(clientLevel, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = Math.max(0.99f + (StoneyCore.getConfig().visualOptions().getMuzzlesSmokeParticlesTime() / 20000f), 0.997f);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.xd += (clientLevel.random.nextDouble() - 0.5) * 0.02;
        this.yd += clientLevel.random.nextDouble() * 0.02;
        this.zd += (clientLevel.random.nextDouble() - 0.5) * 0.02;

        this.scale(1.8f);
        this.lifetime = StoneyCore.getConfig().visualOptions().getMuzzlesSmokeParticlesTime() * 20;
        this.spriteSet = spriteSet;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 1f - ((float)this.age / (float)this.lifetime);

        if (!this.removed) {
            this.setSpriteFromAge(this.spriteSet);
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Factory(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {

        public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel clientLevel,
                                       double x, double y, double z, double xd, double yd, double zd) {
                return new MuzzlesSmokeParticle(clientLevel, x, y, z, this.sprites, xd, yd, zd);
            }
        }
}