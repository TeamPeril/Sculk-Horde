package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.core.BlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.List;
import java.util.Random;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CocoonBlock extends SculkFloraBlock implements IForgeBlock {

    boolean DEBUG_THIS = DEBUG_MODE && false;

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

    /**
     * ACTIVATION_DISTANCE - The distance at which this is able to detect mobs.
     * possibleLivingEntityTargets - A list of nearby targets which are worth infecting.
     * possibleAggressorTargets - A list of nearby targets which should be considered hostile.
     */
    private final int ACTIVATION_DISTANCE = 32;
    private List<LivingEntity> possibleLivingEntityTargets;
    private List<LivingEntity> possibleAggressorTargets;

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

    /**
     * Allows Light to pass through the block.
     * @param blockState The current blockstate
     * @param blockReader ???
     * @param blockPos The current block position
     * @return true if light should pass through, false otherwise
     */
    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockReader, BlockPos blockPos) {
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
    public boolean canCreatureSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType)
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
    public boolean mayPlaceOn(BlockState blockState, BlockGetter iBlockReader, BlockPos blockPos)
    {
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
        if(DEBUG_THIS)
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
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor){
        destroy((LevelAccessor) world, pos, this.defaultBlockState());
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
     * Gets called every time the block randomly ticks. <br>
     * The cocoon is dumb and only sends out requests. Gravemind handles the logistics.
     * @param blockState The current Blockstate
     * @param serverWorld The current ServerWorld
     * @param bp The current Block Position
     * @param random ???
     */
    @Override
    public void randomTick(BlockState blockState, ServerLevel serverWorld, BlockPos bp, Random random)
    {
        //Create bounding cube to detect targets
        AABB searchArea = EntityAlgorithms.getSearchAreaRectangle(bp.getX(), bp.getY(), bp.getZ(), ACTIVATION_DISTANCE, 5, ACTIVATION_DISTANCE);

        //Get targets inside bounding box.
        possibleAggressorTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox(serverWorld, searchArea);
        //EntityAlgorithms.filterOutNonTargets(possibleAggressorTargets, true, false, true);
        //if(true) System.out.println(Arrays.toString(possibleAggressorTargets.toArray()));

        possibleLivingEntityTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox(serverWorld, searchArea);
        //EntityAlgorithms.filterOutNonTargets(possibleLivingEntityTargets, false, true, false);
        //if(true) System.out.println(Arrays.toString(possibleLivingEntityTargets.toArray()));

        //Give gravemind context to our request to make more informed situations
        ReinforcementRequest context = new ReinforcementRequest(bp);
        context.sender = ReinforcementRequest.senderType.SculkCocoon;
        if(!possibleAggressorTargets.isEmpty())
        {
            context.is_aggressor_nearby = true;
            //if(DEBUG_THIS) System.out.println("Players Detected");
        }
        if(!possibleLivingEntityTargets.isEmpty())
        {
            context.is_non_sculk_mob_nearby = true;
            //if(DEBUG_THIS) System.out.println("Non-Sculk Mobs Detected");
        }

        //If there is some sort of enemy near by, request reinforcement
        if(context.is_non_sculk_mob_nearby || context.is_aggressor_nearby)
        {
            //Request reinforcement from entity factory (this request gets approved or denied by gravemind)
            if(DEBUG_THIS) System.out.println("Sending out Reinforcement Request");
            SculkHorde.entityFactory.requestReinforcementAny(serverWorld, bp, false, context);

            //If the gravemind has viewed
            if(context.isRequestViewed)
            {
                //If our request is approved, delete cocoon
                if(context.isRequestApproved)
                {
                    serverWorld.destroyBlock(bp,false);
                }
            }
        }

    }
}
