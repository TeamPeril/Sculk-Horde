package com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockIDOnlyCurableTableEntry implements IBlockInfestationEntry
{
    protected String normalVariantID;
    protected String infectedVariantID;

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

    // Default constructor
    public BlockIDOnlyCurableTableEntry(float priority, String normalVariantIn, String infectedVariantIn)
    {
        normalVariantID = normalVariantIn;
        infectedVariantID = infectedVariantIn;
        this.priority = priority;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        // WE do false because we want these blocks to be only curable, not infectable.
        return false;
    }

    public boolean isInfectedVariant(BlockState blockState)
    {
        return BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString().equals(infectedVariantID);
    }

    public BlockState getNormalVariant(LevelReader level, BlockPos blockPos)
    {
        return getNormalVariant(level.getBlockState(blockPos));
    }

    public BlockState getInfectedVariant(LevelReader level, BlockPos blockPos)
    {
        return getInfectedVariant(level.getBlockState(blockPos));
    }

    public BlockState getNormalVariant(BlockState infectedBlockState)
    {
        // In this case we need to copy all the properties again
        //BlockState normalState = normalVariant.defaultBlockState();

        // Use the normalVariantID to get the normal variant block
        Block normalVariantBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(normalVariantID));
        BlockState normalVariantBlockState = normalVariantBlock.defaultBlockState();


        for(Property<?> prop : infectedBlockState.getProperties()) {
            normalVariantBlockState = copyBlockProperty(infectedBlockState, normalVariantBlockState, prop);
        }

        return normalVariantBlockState;
    }

    public BlockState getInfectedVariant(BlockState blockState)
    {
        // Use the normalVariantID to get the normal variant block
        Block infectedVariantBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(infectedVariantID));
        BlockState infectedVariantBlockState = infectedVariantBlock.defaultBlockState();

        // copy block properties of normal block to infected block
    	for(Property<?> prop : blockState.getProperties()) {
    		infectedVariantBlockState = copyBlockProperty(blockState, infectedVariantBlockState, prop);
    	}

        return infectedVariantBlockState;
    }
}