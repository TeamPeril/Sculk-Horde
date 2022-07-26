package com.github.sculkhoard.common.block.BlockInfestation;

import com.github.sculkhoard.common.block.BlockAlgorithms;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpreadingBlock extends Block implements IForgeBlock {

    private final boolean DEBUG_THIS = false;

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.DIRT;
    public static MaterialColor MAP_COLOR = MaterialColor.COLOR_BROWN;

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
     * DEFAULT_MAX_SPREAD_ATTEMPTS - When first placed, what should the spread attempts be? <br>
     */
    private static final int DEFAULT_MAX_SPREAD_ATTEMPTS = 30;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SpreadingBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SpreadingBlock() {
        this(getProperties());
    }

    /** Accessors */

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .harvestTool(PREFERRED_TOOL)
                .harvestLevel(HARVEST_LEVEL)
                .sound(SoundType.GRASS);
    }

    /**
     * Returns an integer representing how many times this block can initially spread.
     * @return An int
     */
    public static int getDefaultMaxSpreadAttempts()
    {
        return DEFAULT_MAX_SPREAD_ATTEMPTS;
    }

    /**
     * Just returns the tile entity
     * @param world The world to check
     * @param thisBlockPos The position to check
     * @return The tile entity
     */
    @Nullable
    public SpreadingTile getTileEntity(World world, BlockPos thisBlockPos)
    {
        //Get tile entity for this block
        if(world.getBlockState(thisBlockPos).getBlock() instanceof SpreadingBlock)
        {
            SpreadingTile thisTile = (SpreadingTile) world.getBlockEntity(thisBlockPos);

            //If tile entity not yet exist, create it
            if(thisTile == null || !(thisTile instanceof SpreadingTile))
            {
                createTileEntity(world.getBlockState(thisBlockPos), world);
                thisTile = (SpreadingTile) world.getBlockEntity(thisBlockPos);
            }
            return thisTile;
        }
        return null;
    }


    /**
     * Returns what block state we want this block to convert into after its done spreading.
     * @return The BlockState this block will convert into
     */
    public BlockState getDormantVariant()
    {
        return BlockRegistry.CRUST.get().defaultBlockState();
    }


    /**
     * Returns what block state we want this block to convert into if converted back to original victim.
     * @return The BlockState this block will convert into
     */
    public BlockState getVictimVariant()
    {
        return Blocks.DIRT.defaultBlockState();
    }

    /** Other**/

    /**
     * This changes the behavior of how this block spreads. If true, will choose random blocks to spread to.
     * If false, will attempt to spread to all nearby blocks.
     * @return
     */
    protected boolean doesSpreadRandomly()
    {
        return true;
    }

    /**
     * This is the description the item of the block will display when hovered over.
     * @param stack The item stack
     * @param iBlockReader The world
     * @param tooltip ???
     * @param flagIn ???
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader iBlockReader, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(new TranslationTextComponent("tooltip.sculkhoard.spreading_block")); //Text that displays if holding shift

    }

    /**
     * Determines if a given block is a valid vicitim.
     * @param blockState The Block
     * @return true if valid, false otherwise
     */
    public boolean isValidVictim(BlockState blockState)
    {
        //NOTE: I made this an if statement for the sake of efficiency
        return blockState.getBlock() == Blocks.GRASS_BLOCK
                || blockState.getBlock() == Blocks.DIRT
                || blockState.getBlock() == Blocks.GRASS_PATH
                || blockState.getBlock() == Blocks.COARSE_DIRT
                || blockState.getBlock() == Blocks.FARMLAND;
    }

    /**
     * Attempt to spread to a specific block position.
     * @param thisTile The Tile Entity of this block
     * @param serverWorld The ServerWorld of this block
     * @param targetPos The Block Position of the target block
     */
    private void attemptToSpreadHere(SpreadingTile thisTile, ServerWorld serverWorld, BlockPos targetPos)
    {
        //Make sure nothing is null
        if(thisTile == null)
        {
            System.out.println("Error: thisTile not found.");
            return;
        }

        if(DEBUG_THIS) System.out.println("Adding one to getSpreadAttempts of current value: " + thisTile.getSpreadAttempts());
        thisTile.setSpreadAttempts(thisTile.getSpreadAttempts() + 1); //Add 1 to spread attempts

        //Create new block and if successful,
        if(SculkHoard.infestationConversionTable.convertToActiveSpreader(serverWorld, targetPos))
        {
            SpreadingTile childTile = (SpreadingTile) serverWorld.getWorldServer().getBlockEntity(targetPos); //Get new block tile entity
            //Exit immediately if error
            if((childTile == null || !(childTile instanceof SpreadingTile)))
            {
                System.out.println("Error: childTile not found.");
                return;
            }
            if(DEBUG_THIS) System.out.println("thisTile maxSpreadAttempts: " + thisTile.getMaxSpreadAttempts() + " before change.");
            if(DEBUG_THIS) System.out.println("childTile maxSpreadAttempts: " + childTile.getMaxSpreadAttempts() + " before change.");
            childTile.setMaxSpreadAttempts(thisTile.getMaxSpreadAttempts() - 1);
            if(DEBUG_THIS) System.out.println("childTile maxSpreadAttempts: " + childTile.getMaxSpreadAttempts() + " after change.");
        }
    }

    /**
     * Will attempt to spread to nearby neighbors.
     * @param serverWorld The world
     * @param targetPos The current block position
     */
    public void spreadRoutine(ServerWorld serverWorld, BlockPos targetPos, boolean chooseSpreadPosRandomly) {

        //Get tile entity for this block
        SpreadingTile thisTile = getTileEntity(serverWorld, targetPos);


        //When true, this active block gets converted into dormant
        boolean isSpreadingComplete = false;

        //Just exit if thisTile is incorrect
        if(thisTile == null || !(thisTile instanceof SpreadingTile))
        {
            if(DEBUG_THIS) System.out.println("Error: thisTile is null or of wrong instance type.");
            return;
        }

        //If this block has not attempted to spread before
        if(thisTile.getMaxSpreadAttempts() == -1)
        {
            thisTile.setMaxSpreadAttempts(getDefaultMaxSpreadAttempts());//Set to default
        }

        //If spreading randomly and if there is sculk mass
        if(chooseSpreadPosRandomly && SculkHoard.entityFactory.getSculkAccumulatedMass() > 0)
        {
            //If we have spreading attempts left
            if(thisTile.getSpreadAttempts() < thisTile.getMaxSpreadAttempts())
            {
                //Attempt to spread Randomly
                for(int spreadAttempts = 0; spreadAttempts < thisTile.getMaxSpreadAttempts(); spreadAttempts ++)
                {
                    //If max spread attempts has not been reached, spread
                    if (thisTile.getSpreadAttempts() < thisTile.getMaxSpreadAttempts())
                    {
                        attemptToSpreadHere(thisTile, serverWorld, BlockAlgorithms.getRandomNeighbor(serverWorld, targetPos)); //Attempt to spread to this position
                    }
                }
            }
            //If no attempts left
            else { isSpreadingComplete = true; }
        }
        //If we are checking every vailid position instead of random ones
        else if(!chooseSpreadPosRandomly && SculkHoard.entityFactory.getSculkAccumulatedMass() > 0)
        {
            //Get all neighbors and check if we can spread to all possible positions
            ArrayList<BlockPos> allNeighbors = BlockAlgorithms.getNeighborsCube(targetPos);
            for(int i = 0; i < allNeighbors.size(); i++)
            {
                attemptToSpreadHere(thisTile, serverWorld, allNeighbors.get(i)); //Attempt to spread to this position
            }
            isSpreadingComplete = true;
        }

        //Once done spreading, convert to dormant variant
        if(isSpreadingComplete || SculkHoard.entityFactory.getSculkAccumulatedMass() <= 0)
        {
            SculkHoard.infestationConversionTable.convertToDormant(serverWorld, targetPos);
        }

    }

    @Override
    public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        spreadRoutine(pLevel, pPos, doesSpreadRandomly());
        super.randomTick(pState, pLevel, pPos, pRandom);
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
     * A function called by forge to create the tile entity.
     * @param state The current blockstate
     * @param world The world the block is in
     * @return Returns the tile entity.
     */
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityRegistry.SPREADING_BLOCK_TILE.get().create();
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



}
