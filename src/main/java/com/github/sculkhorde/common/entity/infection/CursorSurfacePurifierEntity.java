package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockInfestationHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.IPlantable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
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
        return BlockInfestationHelper.isCurable(state);
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
        Predicate<CursorInfectorEntity> isCursor = Objects::nonNull;
        List<CursorInfectorEntity> Infectors = this.level().getEntitiesOfClass(CursorInfectorEntity.class, this.getBoundingBox().inflate(5.0D), isCursor);
        for(CursorInfectorEntity infector : Infectors)
        {
            infector.discard();
            this.discard();
            break;
        }
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
