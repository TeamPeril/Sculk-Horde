package com.github.sculkhorde.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class SculkCrustParticle extends TextureSheetParticle
{
    protected SculkCrustParticle(ClientLevel clientLevel, double x, double y, double z, double xDirection, double yDirection, double zDirection) {
        super(clientLevel, x, y, z, xDirection, yDirection, zDirection);
        this.setSize(0.02f, 0.02f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.xd = xDirection * (double) 0.2f + (Math.random() * 2.0 - 1.0) * (double) 0.02f;
        this.yd = yDirection * (double) 0.2f + (Math.random() * 2.0 - 1.0) * (double) 0.02f;
        this.zd = zDirection * (double) 0.2f + (Math.random() * 2.0 - 1.0) * (double) 0.02f;
        this.lifetime = (int) (20.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.yd += 0.002;
        if (this.level != null) {
            this.move(this.xd, this.yd, this.zd);
        }
        this.xd *= 0.85f;
        this.yd *= 0.85f;
        this.zd *= 0.85f;
    }

    @Override
    public float getQuadSize(float tickDelta) {
        float f = ((float) this.age + tickDelta) / (float) this.lifetime;
        return this.quadSize * (1.0f - f * f * 0.5f);
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
            SculkCrustParticle particle = new SculkCrustParticle(clientWorld, d, e, f, g, h, i);
            particle.pickSprite(this.spriteProvider);
            return particle;
        }
    }
}
