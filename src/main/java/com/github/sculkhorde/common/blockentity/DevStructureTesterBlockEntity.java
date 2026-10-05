package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.structures.procedural.ProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.StructureUtil;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.hitboxes.BeamHitbox;
import com.google.common.base.Predicates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class DevStructureTesterBlockEntity extends BlockEntity
{
    StructureUtil.StructurePlacer structurePlacer;
    public long tickedAt = 0;

    public long TICK_COOLDOWN = TickUnits.convertSecondsToTicks(3F);

    private ProceduralStructure proceduralStructure;


    /**
     * The Constructor that takes in properties
     * @param blockPos The Position
     * @param blockState The Properties
     */
    public DevStructureTesterBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.DEV_STRUCTURE_TESTER_BLOCK_ENTITY.get(), blockPos, blockState);
        //system = new NodeAtmosphereInfestationSystem(this);
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DevStructureTesterBlockEntity blockEntity)
    {
        if(level.isClientSide()) { return; }

        ServerLevel serverLevel = (ServerLevel) level;

        if(Math.abs(level.getGameTime() - blockEntity.tickedAt) < blockEntity.TICK_COOLDOWN)
        {
            return;
        }

        blockEntity.tickedAt = level.getGameTime();

        //ParticleUtil.spawnParticleBeam(serverLevel, ParticleTypes.FLAME, blockPos.getCenter(), new Vec3(0, 1, 0), 3, 1, 3);
        Vec3 beamDirection = new Vec3(2, 1, 2).normalize();
        int beamLength = serverLevel.random.nextIntBetweenInclusive(3,15);
        int beamRadius = 3;
        Vec3 beamEndpoint = blockPos.getCenter().add(beamDirection.normalize().scale(beamLength));

        ParticleUtil.spawnParticleBeam(serverLevel, ParticleTypes.END_ROD, blockPos.getCenter(), beamDirection, beamLength, beamRadius, 30);
        BeamHitbox hitbox = new BeamHitbox(blockPos.getCenter(), beamEndpoint, beamRadius);

        List<LivingEntity> entities = hitbox.getLivingEntitiesInHitbox(level, null, Predicates.alwaysTrue());

        for(LivingEntity e : entities)
        {
            Vec3 EntityToBlockVector = blockPos.getCenter().subtract(e.getEyePosition());
            Vec3 EntityToBlockVectorDirection = EntityToBlockVector.normalize();


            //ParticleUtil.spawnParticleBeam(serverLevel, ParticleTypes.FLAME, e.getEyePosition(), EntityToBlockVectorDirection, (float) EntityToBlockVector.length(), 0.2F, 5);
        }


    }
}
