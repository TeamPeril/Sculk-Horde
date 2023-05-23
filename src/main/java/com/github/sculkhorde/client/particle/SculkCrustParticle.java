package com.github.sculkhorde.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class SculkCrustParticle extends TextureSheetParticle
{
    protected SculkCrustParticle(ClientLevel clientLevel, double x, double y, double z, double xDirection, double yDirection, double zDirection) {
        super(clientLevel, x, y, z, xDirection, yDirection, zDirection);
    }

    protected SculkCrustParticle(ClientLevel clientLevel, double x, double y, double z) {
        super(clientLevel, x, y, z);
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Provider(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
            RandomSource random = clientWorld.random;
            double d0 = random.nextGaussian() * (double)1.0E-6F;
            double d1 = random.nextGaussian() * (double)1.0E-4F;
            double d2 = random.nextGaussian() * (double)1.0E-6F;
            SculkCrustParticle particle = new SculkCrustParticle(clientWorld, d, e, f, g, h, i);
            particle.pickSprite(this.spriteProvider);
            particle.quadSize *= random.nextFloat() * 0.4F + 0.1F;
            particle.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
            particle.setLifetime(20 * 10);
            return particle;
        }
    }
}
