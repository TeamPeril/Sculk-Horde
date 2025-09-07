package com.github.sculkhorde.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class AncientDialectParticle extends TextureSheetParticle
{
    protected AncientDialectParticle(ClientLevel clientLevel, double x, double y, double z, double xDirection, double yDirection, double zDirection) {
        super(clientLevel, x, y, z, xDirection, yDirection, zDirection);
    }

    protected AncientDialectParticle(ClientLevel clientLevel, double x, double y, double z) {
        super(clientLevel, x, y, z);
    }

    @Override
    public void tick()
    {
        if (this.age++ >= this.lifetime)
        {
            this.remove();
            return;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.xd = 0;
        this.yd += 0.04 * gravity;
        this.zd = 0;

        this.move(this.xd, yd, zd);

        if (this.onGround) {
            this.xd *= 0.699999988079071;
            this.zd *= 0.699999988079071;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Provider(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
            RandomSource random = clientWorld.random;
            AncientDialectParticle particle = new AncientDialectParticle(clientWorld, d, e, f, g, h, i);
            particle.pickSprite(this.spriteProvider);
            particle.quadSize *= random.nextFloat() * 0.4F + 0.5F;
            particle.lifetime = random.nextIntBetweenInclusive(1,3);
            particle.setLifetime(20 * 10);
            return particle;
        }
    }
}
