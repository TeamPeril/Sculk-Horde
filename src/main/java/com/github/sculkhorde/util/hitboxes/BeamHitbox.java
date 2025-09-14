package com.github.sculkhorde.util.hitboxes;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.ParticleUtil;
import com.google.common.base.Predicates;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BeamHitbox implements IModHitbox{

    private final Vec3 startPoint;
    private final Vec3 endPoint;
    private final double radius;
    private final Vec3 direction;
    private final double length;

    /**
     * Constructs a new RotatableHitbox.
     * @param startPoint The starting position of the beam.
     * @param endPoint The ending position of the beam.
     * @param radius The radius of the beam.
     */
    public BeamHitbox(Vec3 startPoint, Vec3 endPoint, double radius) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.radius = radius;
        this.direction = endPoint.subtract(startPoint);
        this.length = endPoint.subtract(startPoint).length();
    }

    /**
     * Checks if this rotatable hitbox intersects with an entity's AABB.
     * The method calculates the closest point on the beam's line segment to the
     * entity's center and checks if the distance is within the combined radii.
     *
     * @param entity The entity to check for intersection.
     * @return true if the entity is hit by the beam, false otherwise.
     */
    public boolean intersects(Entity entity) {


        // Calculate the closest point on the beam's infinite line to the entity's center.
        Vec3 entityVector = entity.getEyePosition().subtract(startPoint);

        // Calculate the squared length of the beam's direction vector.
        double directionLengthSq = direction.lengthSqr();

        double t;
        // Check if the beam has length
        if (directionLengthSq != 0) {
            // Calculate the dot product and the parameter t
            t = entityVector.dot(direction) / directionLengthSq;
        } else {
            // The beam is just a point, so t should be 0.
            t = 0;
        }

        // Clamp t to the beam's segment [0, 1].
        t = Math.max(0.0, Math.min(1.0, t));

        // Project the closest point onto the beam's segment.
        Vec3 closestPoint = startPoint.add(direction.scale(t));


        // Calculate the distance from the entity's center to this closest point.
        //double distanceSq = closestPoint.distanceTo(entityCenter);
        double distanceSq = BlockAlgorithms.getDistance(entity.getEyePosition(), closestPoint);


        if(SculkHorde.isDebugMode() && distanceSq <= radius)
        {
            // DEBUG
            Vec3 EntityCenterToBeamVector = closestPoint.subtract(entity.getEyePosition());
            Vec3 EntityCenterToBeamVectorDirection = EntityCenterToBeamVector.normalize();
            ParticleUtil.spawnParticleBeam((ServerLevel) entity.level(), ParticleTypes.FLAME, entity.getEyePosition(), EntityCenterToBeamVectorDirection, (float) EntityCenterToBeamVector.length(), 0.2F, 5);

        }
        else if(SculkHorde.isDebugMode()  && distanceSq > radius)
        {
            // DEBUG
            Vec3 EntityCenterToBeamVector = closestPoint.subtract(entity.getEyePosition());
            Vec3 EntityCenterToBeamVectorDirection = EntityCenterToBeamVector.normalize();
            ParticleUtil.spawnParticleBeam((ServerLevel) entity.level(), ParticleTypes.SOUL_FIRE_FLAME, entity.getEyePosition(), EntityCenterToBeamVectorDirection, (float) EntityCenterToBeamVector.length(), 0.2F, 5);

        }

        return distanceSq <= radius;
    }

    /**
     * Gets all entities that intersect with this rotatable hitbox.
     * This method first creates a broad AABB to get a list of potential entities,
     * then uses the more precise intersects() method to filter the final results.
     *
     * @param level The level (world) to check for entities.
     * @param owner The entity that owns the beam (e.g., the player or mob).
     * This entity will be ignored to prevent self-hitting.
     * @param predicate An optional predicate to filter entities further (e.g., check for specific types).
     * @return A list of entities that are within the beam's hitbox.
     */
    public List<Entity> getEntitiesInHitbox(Level level, Entity owner, Predicate<Entity> predicate) {

        //AABB broadPhaseAABB = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(startPoint, (int) length * 2);

        // First, create an AABB that tightly encloses the line segment of the beam.
        // The AABB is defined by the minimum and maximum coordinates of the start and end points.
        double minX = Math.min(startPoint.x, endPoint.x);
        double minY = Math.min(startPoint.y, endPoint.y);
        double minZ = Math.min(startPoint.z, endPoint.z);

        double maxX = Math.max(startPoint.x, endPoint.x);
        double maxY = Math.max(startPoint.y, endPoint.y);
        double maxZ = Math.max(startPoint.z, endPoint.z);

        AABB broadPhaseAABB = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        // Now, expand the AABB by the beam's radius to account for its width.
        broadPhaseAABB = broadPhaseAABB.inflate(this.radius);

        // Get all entities within the broad AABB.
        // Use a filter to ignore the owner of the beam and other unwanted entities.
        Predicate<Entity> combinedPredicate = entity -> {
            boolean isNotOwner = owner == null || !entity.equals(owner);
            boolean passesCustomFilter = predicate == null || predicate.test(entity);
            return isNotOwner && passesCustomFilter;
        };

        List<Entity> potentialTargets = level.getEntities(owner, broadPhaseAABB, combinedPredicate);

        // Filter the potential targets using the precise intersects() method.
        List<Entity> finalTargets = new ArrayList<>();
        for (Entity entity : potentialTargets) {
            if (intersects(entity)) {
                finalTargets.add(entity);
            }
        }

        return finalTargets;
    }

    /**
     * Gets all entities that intersect with this rotatable hitbox.
     * This method first creates a broad AABB to get a list of potential entities,
     * then uses the more precise intersects() method to filter the final results.
     *
     * @param level The level (world) to check for entities.
     * @param owner The entity that owns the beam (e.g., the player or mob).
     * This entity will be ignored to prevent self-hitting.
     * @param predicate An optional predicate to filter entities further (e.g., check for specific types).
     * @return A list of entities that are within the beam's hitbox.
     */
    public List<LivingEntity> getLivingEntitiesInHitbox(Level level, LivingEntity owner, Predicate<LivingEntity> predicate) {

        //AABB broadPhaseAABB = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(startPoint, (int) length * 2);

        // First, create an AABB that tightly encloses the line segment of the beam.
        // The AABB is defined by the minimum and maximum coordinates of the start and end points.
        double minX = Math.min(startPoint.x, endPoint.x);
        double minY = Math.min(startPoint.y, endPoint.y);
        double minZ = Math.min(startPoint.z, endPoint.z);

        double maxX = Math.max(startPoint.x, endPoint.x);
        double maxY = Math.max(startPoint.y, endPoint.y);
        double maxZ = Math.max(startPoint.z, endPoint.z);

        AABB broadPhaseAABB = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        // Now, expand the AABB by the beam's radius to account for its width.
        broadPhaseAABB = broadPhaseAABB.inflate(this.radius);

        if(SculkHorde.isDebugMode()) {
            HitboxUtil.drawAABBOutline((ServerLevel) level, broadPhaseAABB, ColorUtil.hexToVector3F(ColorUtil.getRandomHexAcidColor(level.getRandom())));
        }

        // Get all entities within the broad AABB.
        // Use a filter to ignore the owner of the beam and other unwanted entities.
        Predicate<Entity> combinedPredicate = entity -> {
            boolean isNotOwner = owner == null || !entity.equals(owner);
            boolean passesCustomFilter = predicate == null || predicate.test((LivingEntity) entity);
            return isNotOwner && passesCustomFilter;
        };

        List<Entity> potentialTargets = level.getEntities(owner, broadPhaseAABB, Predicates.alwaysTrue());

        // Filter the potential targets using the precise intersects() method.
        List<LivingEntity> finalTargets = new ArrayList<>();
        for (Entity entity : potentialTargets)
        {
            if(entity instanceof LivingEntity livingEntity)
            {
                if(!combinedPredicate.test(livingEntity))
                {
                    continue;
                }

                if(intersects(livingEntity))
                {
                    finalTargets.add(livingEntity);
                }
            }
        }

        return finalTargets;
    }
}
