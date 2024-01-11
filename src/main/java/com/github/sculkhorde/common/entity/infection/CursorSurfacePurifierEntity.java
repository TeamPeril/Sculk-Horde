package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockInfestationHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CursorSurfacePurifierEntity extends CursorEntity{

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorSurfacePurifierEntity(Level worldIn)
    {
        this(ModEntities.CURSOR_SURFACE_PURIFIER.get(), worldIn);
    }

    public CursorSurfacePurifierEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }


    @Override
    public boolean canBeManuallyTicked()
    {
        return false; // Purifiers should never be manually ticked
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isTarget(BlockPos pos)
    {
        return BlockInfestationHelper.isCurable((ServerLevel) level(), pos);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    @Override
    protected void transformBlock(BlockPos pos)
    {
        BlockInfestationHelper.tryToCureBlock((ServerLevel) this.level(), pos);

        // Get all infector cursor entities in area and kill them
        Predicate<CursorSurfaceInfectorEntity> isCursor = Objects::nonNull;
        List<CursorSurfaceInfectorEntity> Infectors = this.level().getEntitiesOfClass(CursorSurfaceInfectorEntity.class, this.getBoundingBox().inflate(5.0D), isCursor);
        for(CursorSurfaceInfectorEntity infector : Infectors)
        {
            infector.discard();
            this.discard();
            break;
        }
    }

    @Override
    protected void spawnParticleEffects()
    {
        this.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, this.getRandomX(1.5D), this.getRandomY(), this.getRandomZ(1.5D), 0.0D, 0.0D, 0.0D);
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {

        if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        if(state.isAir())
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositons.containsKey(pos.asLong()))
        {
            return true;
        }

        if(!BlockAlgorithms.isExposedToAir((ServerLevel) this.level(), pos))
        {
            return true;
        }

        return false;
    }
}
