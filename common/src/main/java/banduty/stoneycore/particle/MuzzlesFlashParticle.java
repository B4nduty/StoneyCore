package banduty.stoneycore.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class MuzzlesFlashParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public MuzzlesFlashParticle(ClientLevel clientLevel, double xCoord, double yCoord, double zCoord,
                                SpriteSet spriteSet, double xd, double yd, double zd) {
        super(clientLevel, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = 0f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.scale(2);
        this.lifetime = 10;
        this.spriteSet = spriteSet;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();

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
                return new MuzzlesFlashParticle(clientLevel, x, y, z, this.sprites, xd, yd, zd);
            }
        }
}