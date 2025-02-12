package com.github.sculkhorde.common.block.InfestationEntries;

import com.github.sculkhorde.common.blockentity.InfestedTagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Superclass for all infestation entries that store their normal variant in an {@link InfestedTagBlockEntity}.
 * <code>isNormalVariant</code> is the only method left unimplemented and functions as a condition.
 */

public abstract class BlockEntityInfestationTableEntry implements IBlockInfestationEntry {

    protected ITagInfestedBlock infectedVariant;
    protected Block defaultNormalVariant;

    protected float priority = 0;

    @Override
    public float getPriority() {
        return priority;
    }

    // This is needed to tell java that the properties are of the same type
    private static <T extends Comparable<T>> BlockState copyBlockProperty(BlockState from, BlockState to, Property<T> property) {
        // copy only if from and to have the same Property (this solution is a little bit hacky but I don't no a better way)
        try {
            return to.setValue(property, from.getValue(property));
        } catch(IllegalArgumentException e) {
            return to;
        }
    }

    private static BlockState copyBlockProperties(BlockState from, BlockState to) {
        for (Property<?> prop : from.getProperties()) {
            to = copyBlockProperty(from, to, prop);
        }
        return to;
    }

    public BlockEntityInfestationTableEntry(float priority, ITagInfestedBlock infectedVariantIn, Block defaultNormalVariantIn) {
        this.priority = priority;
        this.infectedVariant = infectedVariantIn;
        this.defaultNormalVariant = defaultNormalVariantIn;
    }

    @Override
    public boolean isInfectedVariant(BlockState blockState) {
        return ((Block)infectedVariant).defaultBlockState().is(blockState.getBlock());
    }

    @Override
    public BlockState getNormalVariant(Level level, BlockPos blockPos)
    {
        ITagInfestedBlockEntity blockEntity = infectedVariant.getTagInfestedBlockEntity(level, blockPos);
        if(blockEntity == null)
        {
            return level.getBlockState(blockPos);
        }
        return copyBlockProperties(level.getBlockState(blockPos), infectedVariant.getTagInfestedBlockEntity(level, blockPos).getNormalBlockState(defaultNormalVariant));
    }

    @Override
    public BlockState getInfectedVariant(Level level, BlockPos blockPos)
    {
        // copy block properties of normal block to infected block
        BlockState infectedState = ((Block)infectedVariant).defaultBlockState();

        infectedState = copyBlockProperties(level.getBlockState(blockPos), infectedState);

        return infectedState;
    }
}
