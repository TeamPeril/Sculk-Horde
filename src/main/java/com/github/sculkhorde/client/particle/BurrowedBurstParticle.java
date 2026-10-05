package com.github.sculkhorde.client.particle;

import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BurrowedBurstParticle extends TextureSheetParticle
{
    final float initialParticleScale;
    protected BurrowedBurstParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz) {
        this(clientLevel, x, y, z, dx, dy, dz, 1.0F);
    }

    protected BurrowedBurstParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz, float scale) {
        super(clientLevel, x, y, z, 0.0D, 0.0D,0.0D);
        this.xd *= 0.1D;
        this.yd *= 0.1D;
        this.zd *= 0.1D;
        this.xd += dx;
        this.yd += dy;
        this.zd += dz;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.quadSize *= 0.75F;
        this.quadSize *= scale;
        this.initialParticleScale = this.quadSize;
        this.lifetime = TickUnits.convertSecondsToTicks(3);
        this.hasPhysics = true;
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.move(this.xd, this.yd, this.zd);

        this.xd *= 0.7D;
        this.yd *= 0.7D;
        this.zd *= 0.7D;
        this.yd -= 0.02D;

        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BurrowedBurstParticle particle = new BurrowedBurstParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
