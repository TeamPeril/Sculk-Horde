package com.github.sculkhorde.util.hitboxes;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class HitboxUtil {

    /**
     * Spawns smoke particles on all 12 edges of a given AABB to visualize its outline.
     * This is a utility method and does not require a RotatableHitbox instance.
     *
     * @param level The ServerLevel to spawn particles in.
     * @param aabb The AABB to outline.
     * @param color The color of the particles.
     */
    public static void drawAABBOutline(ServerLevel level, AABB aabb, Vector3f color) {
        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        // Vertices of the AABB
        Vec3 v0 = new Vec3(minX, minY, minZ);
        Vec3 v1 = new Vec3(maxX, minY, minZ);
        Vec3 v2 = new Vec3(minX, maxY, minZ);
        Vec3 v3 = new Vec3(maxX, maxY, minZ);
        Vec3 v4 = new Vec3(minX, minY, maxZ);
        Vec3 v5 = new Vec3(maxX, minY, maxZ);
        Vec3 v6 = new Vec3(minX, maxY, maxZ);
        Vec3 v7 = new Vec3(maxX, maxY, maxZ);

        // Particle type with the specified color
        DustParticleOptions particle = new DustParticleOptions(color, 1.0F);

        // Draw the 12 edges
        drawParticleLineStatic(level, particle, v0, v1);
        drawParticleLineStatic(level, particle, v0, v2);
        drawParticleLineStatic(level, particle, v0, v4);
        drawParticleLineStatic(level, particle, v1, v3);
        drawParticleLineStatic(level, particle, v1, v5);
        drawParticleLineStatic(level, particle, v2, v3);
        drawParticleLineStatic(level, particle, v2, v6);
        drawParticleLineStatic(level, particle, v3, v7);
        drawParticleLineStatic(level, particle, v4, v5);
        drawParticleLineStatic(level, particle, v4, v6);
        drawParticleLineStatic(level, particle, v5, v7);
        drawParticleLineStatic(level, particle, v6, v7);
    }

    /**
     * Helper method to draw a line of particles between two points.
     * Made static for use with drawAABBOutline.
     */
    private static void drawParticleLineStatic(ServerLevel level, DustParticleOptions particle, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start);
        double length = direction.length();
        int numParticles = (int) (length * 10);
        if (numParticles == 0) return;

        Vec3 step = direction.scale(1.0D / numParticles);
        for (int i = 0; i <= numParticles; i++) {
            Vec3 pos = start.add(step.scale(i));
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    public static AABB createBoundingBoxCubeAtBlockPos(Vec3 origin, int squareLength)
    {
        double halfLength = squareLength/2;
        AABB boundingBox = new AABB(origin.x() - halfLength, origin.y() - halfLength, origin.z() - halfLength, origin.x() + halfLength, origin.y() + halfLength, origin.z() + halfLength);
        return boundingBox;
    }

    public static AABB createBoundingBoxRectableAtBlockPos(Vec3 origin, int width, int height, int length)
    {
        double halfWidth = width/2;
        double halfHeight = height/2;
        double halfLength = length/2;

        AABB boundingBox = new AABB(origin.x() - halfWidth, origin.y() - halfHeight, origin.z() - halfLength, origin.x() + halfWidth, origin.y() + halfHeight, origin.z() + halfLength);
        return boundingBox;
    }
}
