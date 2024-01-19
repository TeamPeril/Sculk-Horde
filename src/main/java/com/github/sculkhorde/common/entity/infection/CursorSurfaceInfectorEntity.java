package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockInfestationHelper;
import net.minecraft.client.particle.SculkChargeParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

import static com.github.sculkhorde.util.BlockAlgorithms.isExposedToInfestationWardBlock;

public class CursorSurfaceInfectorEntity extends CursorEntity{
    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     *
     * @param worldIn The world to initialize this mob in
     */
    public CursorSurfaceInfectorEntity(Level worldIn) {
        this(ModEntities.CURSOR_SURFACE_INFECTOR.get(), worldIn);
    }

    public CursorSurfaceInfectorEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    /**
     * Returns true if the block is considered a target.
     * @param pos the block position
     * @return true if the block is considered a target
     */
    @Override
    protected boolean isTarget(BlockPos pos)
    {
        return BlockInfestationHelper.isInfectable((ServerLevel) level(), pos);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    @Override
    protected void transformBlock(BlockPos pos)
    {
        BlockInfestationHelper.tryToInfestBlock((ServerLevel) level(), pos);
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
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return true;
        }
        else if(state.isAir())
        {
            return true;
        }
        else if(isExposedToInfestationWardBlock((ServerLevel) this.level(), pos))
        {
            return true;
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        // Check if block is not beyond world border
        if(!level().isInWorldBounds(pos))
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositons.containsKey(pos.asLong()))
        {
            return true;
        }

        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir((ServerLevel) this.level(), pos);
        boolean isBlockNotSculkArachnoid = !state.is(ModBlocks.SCULK_ARACHNOID.get());
        boolean isBlockNotSculkDuraMatter = !state.is(ModBlocks.SCULK_DURA_MATTER.get());

        if(isBlockNotExposedToAir && isBlockNotSculkArachnoid && isBlockNotSculkDuraMatter)
        {
            return true;
        }

        return false;
    }

    @Override
    protected void spawnParticleEffects()
    {
        Random random = new Random();
        float maxOffset = 2;
        float randomXOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomYOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomZOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        this.level().addParticle(ParticleTypes.SCULK_SOUL, getX() + randomXOffset, getY() + randomYOffset, getZ() + randomZOffset, randomXOffset * 0.1, randomYOffset * 0.1, randomZOffset * 0.1);
    }
}
