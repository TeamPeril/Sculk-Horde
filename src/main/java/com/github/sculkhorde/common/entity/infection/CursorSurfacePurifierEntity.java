package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.common.block.SculkFloraBlock;
import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ParticleRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.function.Predicate;

public class CursorSurfacePurifierEntity extends CursorSurfaceInfectorEntity{

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorSurfacePurifierEntity(Level worldIn)
    {
        super(EntityRegistry.CURSOR_SURFACE_PURIFIER, worldIn);
    }

    public CursorSurfacePurifierEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isTarget(BlockState state, BlockPos pos)
    {
        return SculkHorde.infestationConversionTable.infestationTable.isInfectedVariant(state);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    protected void transformBlock(BlockPos pos)
    {
        SculkHorde.infestationConversionTable.deinfectBlock((ServerLevel) this.level, pos);
        if(isSculkFlora.test(this.level.getBlockState(pos.above())))
        {
            this.level.setBlockAndUpdate(pos.above(), Blocks.GRASS.defaultBlockState());
        }
    }

    @Override
    protected void spawnParticleEffects()
    {

        this.level.addParticle(ParticleTypes.COMPOSTER, this.getRandomX(1.5D), this.getRandomY(), this.getRandomZ(1.5D), 0.0D, 0.0D, 0.0D);

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
        if(!state.isSolidRender(this.level, pos))
        {
            return true;
        }

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

        if(!BlockAlgorithms.isExposedToAir((ServerLevel) this.level, pos))
        {
            return true;
        }

        return false;
    }


    /**
     * Determines if a blockstate is considered to be sculk Flora
     * @return True if Valid, False otherwise
     */
    public static Predicate<BlockState> isSculkFlora = (b) ->
    {
        if (b.is(BlockRegistry.GRASS.get()))
        {
            return true;
        }

        if(b.is(BlockRegistry.GRASS_SHORT.get()))
        {
            return true;
        }

        if( b.is(BlockRegistry.SMALL_SHROOM.get()))
        {
            return true;
        }

        if( b.is(BlockRegistry.SCULK_SHROOM_CULTURE.get()))
        {
            return true;
        }

        if( b.is(BlockRegistry.SPIKE.get()))
        {
            return true;
        }

        if( b.is(BlockRegistry.SCULK_SUMMONER_BLOCK.get()))
        {
            return true;
        }

        return false;



    };
}
