package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ParticleUtil {

    public static void spawnBurrowedBurstParticles(ServerLevel level, Vector3f position, int amount, float speed) {

        for (int i = 0; i < amount; i++) {
            spawnBurrowedParticle(level, position, speed);
        }
    }

    public static void spawnBurrowedParticle(ServerLevel level, Vector3f position, float speed)
    {
        spawnParticleOnServer(ModParticles.BURROWED_BURST_PARTICLE.get(), level, position, speed);
    }

    public static void spawnParticleOnServer(ParticleOptions particle, ServerLevel level, Vector3f position, float speed)
    {

        level.sendParticles(particle, position.x, position.y, position.z, 1, 0, 0, 0, speed);
    }



    public static void spawnColoredDustParticleOnClient(ClientLevel level, String hexColor, float alpha, Vector3f position, Vector3f deltaMovement)
    {
        spawnParticleOnClient(new DustParticleOptions(ColorUtil.hexToVector3F(hexColor), alpha), level, position, deltaMovement);
    }

    public static void spawnSolidColoredDustParticleOnClient(ClientLevel level, String hexColor, Vector3f position, Vector3f deltaMovement)
    {
        spawnColoredDustParticleOnClient(level, hexColor, 1.0F, position, deltaMovement);
    }

    public static void spawnSnowflakeParticleOnClient(ClientLevel level, Vector3f position, Vector3f deltaMovement)
    {
        spawnParticleOnClient(ParticleTypes.SNOWFLAKE, level, position, deltaMovement);
    }

    public static void spawnFlameParticleOnClient(ClientLevel level, Vector3f position, Vector3f deltaMovement)
    {
        spawnParticleOnClient(ParticleTypes.FLAME, level, position, deltaMovement);
    }

    public static void spawnParticleOnClient(ParticleOptions particle, ClientLevel level, Vector3f position, Vector3f deltaMovement)
    {
        level.addParticle(particle, position.x, position.y, position.z, deltaMovement.x, deltaMovement.y, deltaMovement.z);
    }

    public static void spawnBlockParticleOnClient(BlockState blockState, ClientLevel level, Vector3f position, Vector3f deltaMovement)
    {
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                position.x, position.y, position.z, deltaMovement.x, deltaMovement.y, deltaMovement.z);
    }

    public static void spawnParticleBeam(ServerLevel level, ParticleOptions particle, Vec3 origin, Vec3 direction, float length, float radius, float thickness)
    {
        // Normalize the direction vector once to be used throughout the function
        Vec3 directionNormalized = direction.normalize();

        // Determine a robust "up" vector for the cross product
        // to handle cases where the beam is vertical
        Vec3 up;
        if (Math.abs(directionNormalized.y) > 0.999D) {
            // If direction is nearly vertical, use the Z-axis as a reference
            up = new Vec3(0, 0, 1);
        } else {
            // Otherwise, use the standard Y-axis
            up = new Vec3(0, 1, 0);
        }

        Vec3 right = up.cross(directionNormalized).normalize();
        Vec3 forward = directionNormalized.cross(right).normalize();

        // Determine the number of steps to take along the beam's length
        // A step of 0.3 is a good, but arbitrary, value.
        float stepSize = 0.3F;
        int numSteps = (int) (length / stepSize);
        if (numSteps == 0) {
            numSteps = 1; // Ensure at least one particle for very short beams
        }

        // Spawn Particles along the beam's length
        for (int i = 0; i <= numSteps; i++) {
            // Calculate the current position along the beam
            double t = (double) i / numSteps;
            Vec3 currentPoint = origin.add(directionNormalized.scale(length * t));

            // Create a circle of particles around the current point
            for (int j = 0; j < thickness; ++j) {
                double angle = 2 * Math.PI * j / thickness;
                double xOffset = radius * Math.cos(angle);
                double yOffset = radius * Math.sin(angle);

                Vec3 offset = right.scale(xOffset).add(forward.scale(yOffset));
                level.sendParticles(particle, currentPoint.x + offset.x, currentPoint.y + offset.y, currentPoint.z + offset.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static void spawnPurityDustParticlesOnClient(ClientLevel level, BlockPos pos) {
        double offsetX, offsetY, offsetZ;

        for (int i = 0; i < 50; i++) {
            offsetX = getRandomFloat(level.random, -0.9F, 0.9F);
            offsetY = getRandomFloat(level.random, -0.9F, 0.9F);
            offsetZ = getRandomFloat(level.random, -0.9F, 0.9F);

            Vector3f newParticlePos = new Vector3f((float) (pos.getX() + 0.5 + offsetX), (float) (pos.getY() + 0.5 + offsetY), (float) (pos.getZ() + 0.5 + offsetZ));

            spawnColoredDustParticleOnClient(level, ColorUtil.getRandomPurityColor(level.random), 1.0F, newParticlePos, new Vector3f(0));
        }

    }

    protected static float getRandomFloat(RandomSource random, float min, float max) {
        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }
        if (min >= max) {
            throw new IllegalArgumentException("Min must be less than Max");
        }

        float range = max - min;
        return random.nextFloat() * range + min;
    }
}
