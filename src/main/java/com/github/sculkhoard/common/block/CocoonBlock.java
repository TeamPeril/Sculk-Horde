package com.github.sculkhoard.common.block;

import com.github.sculkhoard.common.entity.entity_factory.ReinforcementContext;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;


import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

public class CocoonBlock extends SculkFloraBlock implements IForgeBlock {

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.VEGETABLE;
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
    public static ToolType PREFERRED_TOOL = ToolType.SHOVEL;

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
    public static int HARVEST_LEVEL = -1;

    private static final int ACTIVATION_DISTANCE = 64;

    private List<LivingEntity> possibleTargets;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public CocoonBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public CocoonBlock() {
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
                .noOcclusion()
                .sound(SoundType.SLIME_BLOCK);
        return prop;
    }



    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return true;
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
        boolean cocoonPosCanBeReplacedByWater = false;
        boolean cocoonPosHasNoNeighbors = true; //Assume false unless proven otherwise
        BlockPos cocoonPos = blockPos.above();

        cocoonPosCanBeReplacedByWater = iBlockReader.getBlockState(cocoonPos).canBeReplaced(Fluids.WATER);

        //Check To see if the floor block is valid
        Block[] validBlocks = {BlockRegistry.COCOON_ROOT.get()};
        for(Block b : validBlocks)
        {
            if(blockState.getBlock() == b) blockIsValid = true;
        }

        //Check to see if where we will place the cocoon, has no neighbors
        BlockPos[] cocoonPosNeighbors = {
                cocoonPos.north(),
                cocoonPos.east(),
                cocoonPos.south(),
                cocoonPos.west(),
                cocoonPos.above()
        };

        for(BlockPos bp : cocoonPosNeighbors)
        {
            if(!iBlockReader.getBlockState(bp).isAir())
                cocoonPosHasNoNeighbors = false;
        }
        if(DEBUG_MODE && DEBUG_THIS)
            System.out.println(
                    "\n" + "Attempted to Place " + this.getClass().toString()
                     + " at " + blockPos.toString() + "\n"
                     + "blockIsValid " + blockIsValid + "\n"
                     + "cocoonPosHasNoNeighbors " + cocoonPosHasNoNeighbors
            );
        return blockIsValid && cocoonPosCanBeReplacedByWater && cocoonPosHasNoNeighbors;
    }

    /**
     * Called when a tile entity on a side of this block changes is created or is destroyed.
     * @param world The world
     * @param pos Block position in world
     * @param neighbor Block position of neighbor
     */
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor){
        destroy((IWorld) world, pos, this.defaultBlockState());
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
        ReinforcementContext context = new ReinforcementContext(bp.getX(), bp.getY());
        /*
        possibleTargets =
                serverWorld.getLevel().getLoadedEntitiesOfClass(
                        LivingEntity,
                        this.getTargetSearchArea(this.getFollowDistance()),
                        (Predicate<? super LivingEntity>) null);
        */
        SculkHoard.entityFactory.requestReinforcementAny(1, serverWorld, bp, false, context);
        serverWorld.destroyBlock(bp,false);

    }


}
