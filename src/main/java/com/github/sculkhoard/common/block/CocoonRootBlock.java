package com.github.sculkhoard.common.block;

import com.github.sculkhoard.core.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.Random;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

public class CocoonRootBlock extends SculkFloraBlock implements IForgeBlock {

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.PLANT;
    public static MaterialColor MAP_COLOR = MaterialColor.TERRACOTTA_BLUE;

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 0.6f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 0.5f;

    /**
     * PREFERRED_TOOL determines what type of tool will break the block the fastest and be able to drop the block if possible
     */
    public static ToolType PREFERRED_TOOL = ToolType.HOE;

    /**
     *  Harvest Level Affects what level of tool can mine this block and have the item drop<br>
     *
     *  -1 = All<br>
     *  0 = Wood<br>
     *  1 = Stone<br>
     *  2 = Iron<br>
     *  3 = Diamond<br>
     *  4 = Netherite
     */
    public static int HARVEST_LEVEL = 3;

    /**
     * This is used to keep track of the growth state. <br>
     * initial = when there is a root, but no goup blocks on top.
     * child = when there is a root and a single goup block on top.
     * mature = when there is a root and two goup blocks directly above it.
     * This is when it will spawn a mob.
     */
    public enum growthStage{immature, mature}


    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public CocoonRootBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public CocoonRootBlock() {
        this(getProperties());
    }


    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .harvestTool(PREFERRED_TOOL)
                .harvestLevel(HARVEST_LEVEL)
                .sound(SoundType.STONE)
                .noOcclusion();
        return prop;
    }


    /**
     * Determines if this block will randomly tick or not.
     * @param blockState The current blockstate
     * @return True/False
     */
    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    /**
     * Gets called every time the block randomly ticks.
     * @param blockState The current Blockstate
     * @param serverWorld The current ServerWorld
     * @param bp The current Block Position
     * @param random ???
     */
    @Override
    public void randomTick(BlockState blockState, ServerWorld serverWorld, BlockPos bp, Random random)
    {
        grow(serverWorld, bp);
    }

    /**
     * This function causes the root to increment a single growth stage. <br>
     * If the current state is immature, it will increment to mature and place a cocoon. <br>
     * @param serverWorld The world to spawn it in.
     * @param bp The BlockPos of the cocoon.
     * @return Returns whether the growth was successful or not.
     */
    public boolean grow(ServerWorld serverWorld, BlockPos bp)
    {
        if(getGrowthStage(serverWorld, bp) == growthStage.immature)
        {
            BlockRegistry.COCOON.get().placeBlockHere(serverWorld, bp.above());
            return  true;
        }
        return false;
    }

    /**
     * This function checks to see what growth state the cocoon is in
     * based on how many goup blocks is above it. This prevents the need
     * for using variables in the tile entity to determine this.
     * @param serverWorld The world the cocoon is in
     * @param bp The BlockPos of the cocoon
     * @return The current growth state of the cocoon
     */
    private growthStage getGrowthStage(ServerWorld serverWorld, BlockPos bp)
    {
        Block aboveBlock = serverWorld.getBlockState(bp.above()).getBlock();
        if(aboveBlock.is(BlockRegistry.COCOON.get()))
            return growthStage.mature;
        return growthStage.immature;
    }


    /**
     * Determines if a specified mob type can spawn on this block, returning false will
     * prevent any mob from spawning on the block.
     *
     * @param state The current state
     * @param world The current world
     * @param pos Block position in world
     * @param type The Mob Category Type
     * @return True to allow a mob of the specified category to spawn, false to prevent it.
     */
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, EntityType<?> entityType)
    {
        return false;
    }

    /**
     * Determines if this block can be placed on a given block
     * @param blockState The block it is trying to be placed on
     * @param iBlockReader An interface for objects like the world
     * @param blockPos The pos of the block we are trying to place this on
     * @return
     */
    @Override
    public boolean mayPlaceOn(BlockState blockState, IBlockReader iBlockReader, BlockPos blockPos)
    {
        boolean DEBUG_THIS = false;

        boolean blockIsValid = false;

        //Check To see if the floor block is valid
        Block[] validBlocks = {BlockRegistry.CRUST.get()};
        for(Block b : validBlocks)
        {
            if(blockState.getBlock() == b) blockIsValid = true;
        }

        if(DEBUG_MODE && DEBUG_THIS)
            System.out.println(
                    "\n" + "Attempted to Place " + this.getClass().toString()
                    + " at " + blockPos.toString() + "\n"
                    + "blockIsValid " + blockIsValid + "\n"
            );
        return blockIsValid;
    }
}
