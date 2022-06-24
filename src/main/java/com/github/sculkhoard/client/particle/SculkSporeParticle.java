package com.github.sculkhoard.client.particle;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SculkSporeParticle extends SpriteTexturedParticle {

    protected SculkSporeParticle(ClientWorld level, double xCord, double yCord, double zCord, double xDirection, double yDirection, double zDirection) {
        super(level, xCord, yCord, zCord, xDirection, yDirection, zDirection);


    }

    @Override
    public IParticleRenderType getRenderType() {
        return null;
    }
}
