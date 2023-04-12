package com.github.sculkhorde.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class SculkCrustParticle extends TextureSheetParticle
{


    protected SculkCrustParticle(ClientLevel p_i232447_1_, double p_i232447_2_, double p_i232447_4_, double p_i232447_6_) {
        super(p_i232447_1_, p_i232447_2_, p_i232447_4_, p_i232447_6_);
    }

    protected SculkCrustParticle(ClientLevel p_i232448_1_, double p_i232448_2_, double p_i232448_4_, double p_i232448_6_, double p_i232448_8_, double p_i232448_10_, double p_i232448_12_) {
        super(p_i232448_1_, p_i232448_2_, p_i232448_4_, p_i232448_6_, p_i232448_8_, p_i232448_10_, p_i232448_12_);
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

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {


            Random random = pLevel.random;
            double d0 = random.nextGaussian() * (double)1.0E-6F;
            double d1 = random.nextGaussian() * (double)1.0E-4F;
            double d2 = random.nextGaussian() * (double)1.0E-6F;
            SculkCrustParticle particle = new SculkCrustParticle(pLevel, pX, pY, pZ, d0, d1, d2);
            particle.pickSprite(this.sprite);
            particle.quadSize *= random.nextFloat() * 0.4F + 0.1F;
            particle.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
            particle.setLifetime(20 * 10);
            return particle;
        }
    }
}
