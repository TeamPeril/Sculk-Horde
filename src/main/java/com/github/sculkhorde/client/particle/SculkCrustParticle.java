package com.github.sculkhorde.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SculkCrustParticle extends SpriteTexturedParticle
{


    protected SculkCrustParticle(ClientWorld p_i232447_1_, double p_i232447_2_, double p_i232447_4_, double p_i232447_6_) {
        super(p_i232447_1_, p_i232447_2_, p_i232447_4_, p_i232447_6_);
    }

    protected SculkCrustParticle(ClientWorld p_i232448_1_, double p_i232448_2_, double p_i232448_4_, double p_i232448_6_, double p_i232448_8_, double p_i232448_10_, double p_i232448_12_) {
        super(p_i232448_1_, p_i232448_2_, p_i232448_4_, p_i232448_6_, p_i232448_8_, p_i232448_10_, p_i232448_12_);
    }

    @Override
    public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {

    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {

        private final IAnimatedSprite animatedSprite;

        public Factory(IAnimatedSprite animatedSprite) {
            this.animatedSprite = animatedSprite;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return  new SculkCrustParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
