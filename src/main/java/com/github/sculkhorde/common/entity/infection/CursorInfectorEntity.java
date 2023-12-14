package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** This Entity is used to traverse the world and infect blocks.
 * Once spawned, it will use breadth-first search to find the nearest block to infect.
 * Once it has found a block to infect, it will infect it and then move on to the next block.
 * This will continue until it has either reached its max distance or max infections.
 */
public class CursorInfectorEntity extends CursorEntity
{


    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorInfectorEntity(Level worldIn) {super(ModEntities.CURSOR_INFECTOR.get(), worldIn);}

    public CursorInfectorEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
        /*
         * BUG: This is not working properly. The entity is not being removed after 30 seconds.
         * When the entity is spawned, the creationTickTime is not altered in the statement below.
         * TODO Fix this bug.
         */
        creationTickTime = System.currentTimeMillis();
    }



    /**
     * Returns true if the block is considered a target.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered a target
     */
    @Override
    protected boolean isTarget(BlockState state, BlockPos pos)
    {
        return SculkHorde.blockInfestationTable.isInfectable(state);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    @Override
    protected void transformBlock(BlockPos pos)
    {
        SculkHorde.blockInfestationTable.infectBlock((ServerLevel) this.level, pos);
    }

    @Override
    protected void spawnParticleEffects()
    {
    }
}
