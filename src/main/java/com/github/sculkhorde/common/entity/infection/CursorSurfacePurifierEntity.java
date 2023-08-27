package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.function.Predicate;

public class CursorSurfacePurifierEntity extends CursorEntity{

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorSurfacePurifierEntity(Level worldIn)
    {
        super(ModEntities.CURSOR_SURFACE_PURIFIER.get(), worldIn);
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
    @Override
    protected void transformBlock(BlockPos pos)
    {
        SculkHorde.infestationConversionTable.deinfectBlock((ServerLevel) this.level(), pos);
        if(shouldBeRemovedFromAboveBlock.test(this.level().getBlockState(pos.above())))
        {
            this.level().setBlockAndUpdate(pos.above(), Blocks.GRASS.defaultBlockState());
        }

        // Get all infector cursor entities in area and kill them
        Predicate<CursorInfectorEntity> isCursor = Objects::nonNull;
        level().getEntitiesOfClass(CursorInfectorEntity.class, this.getBoundingBox().inflate(5.0D), isCursor).forEach(entity -> entity.discard());
    }

    @Override
    protected void spawnParticleEffects()
    {
        this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.5D), this.getRandomY(), this.getRandomZ(1.5D), 0.0D, 0.0D, 0.0D);
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
        // I'm doing this because cursors will get stuck on infested logs.
        // TODO FIX INFESTED LOG SHITTERY
        if(state.is(ModBlocks.INFESTED_LOG.get()))
        {
            return true;
        }
        else if(!state.isSolidRender(this.level(), pos))
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

        if(!BlockAlgorithms.isExposedToAir((ServerLevel) this.level(), pos))
        {
            return true;
        }

        return false;
    }


    /**
     * Determines if a blockstate is considered to be sculk Flora
     * @return True if Valid, False otherwise
     */
    public static Predicate<BlockState> shouldBeRemovedFromAboveBlock = (b) ->
    {
        if (b.is(ModBlocks.GRASS.get()))
        {
            return true;
        }

        if(b.is(ModBlocks.GRASS_SHORT.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SMALL_SHROOM.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SCULK_SHROOM_CULTURE.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SPIKE.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SCULK_SUMMONER_BLOCK.get()))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_CATALYST))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_SHRIEKER))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_VEIN))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_SENSOR))
        {
            return true;
        }

        if(b.is(ModBlocks.TENDRILS.get()))
        {
            return true;
        }

        return false;
    };
}
