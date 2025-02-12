package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModParticles;
import com.mojang.math.Vector3f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ParticleUtil {

    public static void spawnBurrowedBurstParticles(ServerLevel level, Vec3 position, int amount, float speed) {

        for (int i = 0; i < amount; i++) {
            spawnBurrowedParticle(level, position, speed);
        }
    }

    public static void spawnBurrowedParticle(ServerLevel level, Vec3 position, float speed)
    {
        spawnParticleOnServer(ModParticles.BURROWED_BURST_PARTICLE.get(), level, position, speed);
    }

    public static void spawnParticleOnServer(ParticleOptions particle, ServerLevel level, Vec3 position, float speed)
    {

        level.sendParticles(particle, position.x(), position.y(), position.z(), 1, 0, 0, 0, speed);
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
        level.addParticle(particle, position.x(), position.y(), position.z(), deltaMovement.x(), deltaMovement.y(), deltaMovement.z());
    }

    public static void spawnParticleBeam(ServerLevel level, ParticleOptions particle, Vec3 origin, Vec3 direction, float length, float radius, float thickness)
    {
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        Vec3 forward = direction.cross(right).normalize();

        // Spawn Particles
        for (float i = 1; i < Mth.floor(length) + 1; i += 0.3F) {
            Vec3 vec33 = origin.add(direction.scale((double) i));

            // Create a circle of particles around vec33
            for (int j = 0; j < thickness; ++j) {
                double angle = 2 * Math.PI * j / thickness;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);
                Vec3 offset = right.scale(xOffset).add(forward.scale(zOffset));
                level.sendParticles(particle, vec33.x + offset.x, vec33.y + offset.y, vec33.z + offset.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
