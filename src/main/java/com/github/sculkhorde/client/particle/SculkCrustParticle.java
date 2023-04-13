package com.github.sculkhorde.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class SculkCrustParticle extends TextureSheetParticle
{
    private final double xStart;
    private final double yStart;
    private final double zStart;


    protected SculkCrustParticle(ClientLevel clientLevel, double x, double y, double z, double xDirection, double yDirection, double zDirection) {
        super(clientLevel, x, y, z, xDirection, yDirection, zDirection);
        this.xStart = this.x;
        this.yStart = this.y;
        this.zStart = this.z;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float f = (float)this.age / (float)this.lifetime;
            float f1 = -f + f * f * 2.0F;
            float f2 = 1.0F - f1;
            this.x = this.xStart + this.xd * (double)f2;
            this.y = this.yStart + this.yd * (double)f2 + (double)(1.0F - f);
            this.z = this.zStart + this.zd * (double)f2;
            this.setPos(this.x, this.y, this.z); // FORGE: update the particle's bounding box
        }
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed)
        {
            SculkCrustParticle particle = new SculkCrustParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            particle.pickSprite(this.sprite);

            /*
            RandomSource random = pLevel.random;
            double d0 = random.nextGaussian() * (double)1.0E-6F;
            double d1 = random.nextGaussian() * (double)1.0E-4F;
            double d2 = random.nextGaussian() * (double)1.0E-6F;
            SculkCrustParticle particle = new SculkCrustParticle(pLevel, pX, pY, pZ, d0, d1, d2);
            particle.pickSprite(this.sprite);
            particle.quadSize *= random.nextFloat() * 0.4F + 0.1F;
            particle.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
            particle.setLifetime(20 * 10);
            */
            return particle;
        }
    }
}
