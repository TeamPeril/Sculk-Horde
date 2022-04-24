package com.github.sculkhoard.common.block;

import com.github.sculkhoard.common.tileentity.SculkBrainTile;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class SculkBrainBlock extends Block implements IForgeBlock {

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.PLANT;
    public static MaterialColor MAP_COLOR = CrustBlock.MAP_COLOR;

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 50f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 10f;

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
    public static int HARVEST_LEVEL = 3;

    //The current circle radius, this increments at an interval
    private int infectCircleRadius = 1;
    //Once we are done spreading, how long should we wait before trying again?
    private final long spreadRoutineIntervalInMinutes = 60;
    //The time which this node has finished the spread routine
    private long finishedInfectionRoutineAt = 0;
    //Whether we are currently doing the spread routine
    private boolean currentlySpreading = true;


    private boolean currentlyRepairing = false;
    ArrayList<BlockPos> repairList = new ArrayList<BlockPos>();
    private int repairRadius = 1;
    private int innerMembraneRadius = 2;
    private int outerShellRadius = innerMembraneRadius + 1;
    //Once we are done repairing, how long should we wait before trying again?
    private final long repairRoutineIntervalInMinutes = 1;
    //The time which this node has finished the repair routine
    private long finishedRepairRoutineAt = 0;




    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkBrainBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkBrainBlock() {
        this(getProperties());
    }

    /**
     * This function is called when this block is placed. <br>
     * Will set the nbt data maxSpreadAttempts.
     * @param world The world the block is in
     * @param bp The position the block is in
     * @param blockState The state of the block
     * @param entity The entity that placed it
     * @param itemStack The item stack it was placed from
     */
    @Override
    public void setPlacedBy(World world, BlockPos bp, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        SculkHoard.gravemind.sculkNodePositions.add(bp);
        super.setPlacedBy(world, bp, blockState, entity, itemStack);
    }

    /**
     * Determines if this block will randomly tick or not.
     * @param blockState The current blockstate
     * @return True/False
     */
    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

    /**
     * Gets called every time the block randomly ticks.
     * @param blockState The current Blockstate
     * @param serverWorld The current ServerWorld
     * @param bp The current Block Position
     * @param random ???
     */
    @Override
    public void randomTick(BlockState blockState, ServerWorld serverWorld, BlockPos bp, Random random) {

        boolean DEBUG_THIS = false;
        SculkHoard.entityFactory.addSculkAccumulatedMass(1);//Add 1 sculk mass to the hoard
        //make sure the gravemind knows about this position
        if(!SculkHoard.gravemind.isSculkNodePositionRecorded(bp)) SculkHoard.gravemind.sculkNodePositions.add(bp);

        infectionRoutine(serverWorld, bp);
        repairShellRoutine(serverWorld, bp);

    }

    /**
     * Will infect blocks in an increasing sized sphere. Once reaches max radius,
     * will stop and pause for a specified amount of time, then restart.
     * @param serverWorld The world
     * @param bp The block position
     */
    public void infectionRoutine(ServerWorld serverWorld, BlockPos bp)
    {
        long timeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - finishedInfectionRoutineAt, TimeUnit.NANOSECONDS);
        if(!currentlySpreading && timeElapsed >= spreadRoutineIntervalInMinutes)
        {
            currentlySpreading = true;
            if(DEBUG_MODE) System.out.println("Sculk Node at " + bp + " will restart the spread routine.");
        }

        if(currentlySpreading)
        {
            //TODO Make a new function that does a sphere-ring hybrid instead of a full circle
            SculkHoard.infestationConversionTable.convertToInfectedQueue.addAll(
                    BlockAlgorithms.getBlockPosInCircle((ServerWorld) serverWorld, bp, infectCircleRadius, false, false)
            );
            infectCircleRadius++;
            if(infectCircleRadius > SculkHoard.gravemind.sculk_node_infect_radius)
            {
                infectCircleRadius = 1;
                finishedInfectionRoutineAt = System.nanoTime();
                currentlySpreading = false;
                if(DEBUG_MODE) System.out.println("Sculk Node at " + bp + " has completed its spread routine.");
            }
        }
    }


    /**
     * Will infect blocks in an increasing sized sphere. Once reaches max radius,
     * will stop and pause for a specified amount of time, then restart.
     * @param serverWorld The world
     * @param bp The block position
     */
    public void repairShellRoutine(ServerWorld serverWorld, BlockPos bp)
    {
        long timeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - finishedRepairRoutineAt, TimeUnit.NANOSECONDS);
        if(!currentlyRepairing && timeElapsed >= repairRoutineIntervalInMinutes)
        {
            //TODO: Check if needs repairs
            repairList = BlockAlgorithms.getBlockPosInCircle((ServerWorld) serverWorld, bp, outerShellRadius, false, true);

            for(int i = 0; i < repairList.size(); i++)
            {
                //If the current position is not filled with the appropraite shell block
                if(isBlockValid(serverWorld, bp, repairList.get(i)))
                {
                    repairList.remove(i);
                    i--;
                }
            }


            if(! repairList.isEmpty())
            {
                if(DEBUG_MODE) System.out.println("Sculk Node at " + bp + " will start the repair routine.");
                currentlyRepairing = true;
            }
            else
            {
                currentlyRepairing = false;
            }

        }
        else if(currentlyRepairing)
        {

            if(repairList.isEmpty())
            {
                currentlyRepairing = false;
                if(DEBUG_MODE) System.out.println("Sculk Node at " + bp + " has completed its repair routine.");
                finishedRepairRoutineAt = System.nanoTime();
            }
            else
            {
                BlockPos repairPosition = repairList.get(0);
                double deltaX = repairPosition.getX() - bp.getX();
                double deltaY = repairPosition.getY() - bp.getY();
                double deltaZ = repairPosition.getZ() - bp.getZ();
                double distanceFromCenter = Math.sqrt(Math.pow( deltaX ,2) + Math.pow( deltaY ,2) + Math.pow( deltaZ ,2));
                if(distanceFromCenter <= innerMembraneRadius)
                    serverWorld.setBlockAndUpdate(repairPosition, BlockRegistry.SCULK_ARACHNOID.get().defaultBlockState());
                else
                    serverWorld.setBlockAndUpdate(repairPosition, BlockRegistry.SCULK_DURA_MATTER.get().defaultBlockState());
                repairList.remove(0);
            }
        }
    }

    /**
     * Given a block position, will check if this position is an outer shell or inenr shell position.
     * Then it wil check if the block in that position is the correct one
     * @param world The world
     * @param center The center of the node
     * @param target The target block we are checking
     * @return
     */
    public boolean isBlockValid(ServerWorld world, BlockPos center, BlockPos target)
    {
        double deltaX = target.getX() - center.getX();
        double deltaY = target.getY() - center.getY();
        double deltaZ = target.getZ() - center.getZ();
        double distanceFromCenter = Math.sqrt(Math.pow( deltaX ,2) + Math.pow( deltaY ,2) + Math.pow( deltaZ ,2));

        if(distanceFromCenter <= innerMembraneRadius && world.getBlockState(target).getBlock() == BlockRegistry.SCULK_ARACHNOID.get()) //If inner shell
            return true;
        else if(distanceFromCenter > innerMembraneRadius && world.getBlockState(target).getBlock() == BlockRegistry.SCULK_DURA_MATTER.get()) //If outer shell
            return true;
        return false;
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
                .sound(SoundType.GRASS);
        return prop;
    }

    /**
     * A function called by forge to create the tile entity.
     * @param state The current blockstate
     * @param world The world the block is in
     * @return Returns the tile entity.
     */
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityRegistry.SCULK_BRAIN_TILE.get().create();
    }

    /**
     * Returns If true we have a tile entity
     * @param state The current block state
     * @return True
     */
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    /**
     * Just returns the tile entity
     * @param world The world to check
     * @param thisBlockPos The position to check
     * @return The tile entity
     */
    public SculkBrainTile getTileEntity(World world, BlockPos thisBlockPos)
    {
        //Get tile entity for this block
        TileEntity tileEntity = world.getBlockEntity(thisBlockPos);
        SculkBrainTile thisTile = null;
        if(thisTile == null)
        {
            if(!(thisTile instanceof SculkBrainTile))
            {
                thisTile = (SculkBrainTile) world.getBlockEntity(thisBlockPos);
            }
            else
            {
                System.out.println("Error: Tile of wrong instance.");
            }
        }
        else
        {
            System.out.println("Error: Tile is null.");
        }
        return thisTile;
    }

    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
        TileEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof SculkBrainTile)
            ((SculkBrainTile)tile).loadAllChunks();
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        TileEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof SculkBrainTile)
            ((SculkBrainTile)tile).unloadAllChunks();
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

}
