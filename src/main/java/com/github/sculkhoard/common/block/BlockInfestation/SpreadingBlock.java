package com.github.sculkhoard.common.block.BlockInfestation;

import com.github.sculkhoard.common.block.BlockAlgorithms;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

public class SpreadingBlock extends Block implements IForgeBlock {

    private boolean DEBUG_THIS = false;

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

    private static int tickTracker = 0;
    private static int tickInterval = 20 * 20; //ticks_per_second * seconds

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
        Properties prop = Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .harvestTool(PREFERRED_TOOL)
                .harvestLevel(HARVEST_LEVEL)
                .sound(SoundType.GRASS);
        return prop;
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

    protected boolean doesSpreadRandomly()
    {
        return true;
    }


    protected boolean isValidVictim(BlockState blockState)
    {
        //NOTE: I made this an if statement for the sake of efficiency
        if(blockState.getBlock() == Blocks.GRASS_BLOCK
        || blockState.getBlock() == Blocks.DIRT
        || blockState.getBlock() == Blocks.GRASS_PATH
        || blockState.getBlock() == Blocks.COARSE_DIRT
        || blockState.getBlock() == Blocks.FARMLAND)
        {
            return true;
        }
        return false;
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

    /** Other**/


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
            thisTile.setMaxSpreadAttempts(getDefaultMaxSpreadAttempts());//Set to default

        if(chooseSpreadPosRandomly)
        {
            //Attempt to spread Randomly
            for(int spreadAttempts = 0; spreadAttempts < thisTile.getMaxSpreadAttempts(); spreadAttempts ++)
            {
                //If max spread attempts has not been reached, spread
                if (thisTile.getMaxSpreadAttempts() - thisTile.getSpreadAttempts() > 0)
                {
                    if(DEBUG_THIS) System.out.println("Attempting to Spread to Random Positions? " + chooseSpreadPosRandomly);
                    attemptToSpreadHere(thisTile, serverWorld, BlockAlgorithms.getRandomNeighbor(serverWorld, targetPos)); //Attempt to spread to this position
                }
                //If this block has run out of spread attempts, convert
                else if ((thisTile.getMaxSpreadAttempts()-1) - thisTile.getSpreadAttempts() <= 0)
                {
                    isSpreadingComplete = true;
                    break;
                }
            }
        }
        else
        {
            //Get all neighbors and check if we can spread to all possible positions
            ArrayList<BlockPos> allNeighbors = BlockAlgorithms.getNeighborsCube(targetPos);
            for(int i = 0; i < allNeighbors.size(); i++)
                attemptToSpreadHere(thisTile, serverWorld, allNeighbors.get(i)); //Attempt to spread to this position
            isSpreadingComplete = true;
        }

        //Once done spreading, convert to dormant variant
        if(isSpreadingComplete)
        {
            SculkHoard.infestationConversionTable.convertToDormant(serverWorld, targetPos);
        }
    }

    /**
     * Prints out debug variables to player when right-clicked.
     * @param pState Block state
     * @param pLevel The World of the block
     * @param pPos The position of the block
     * @param pPlayer The player who used it
     * @param pHand The hand they used
     * @param pHit What they are looking at
     * @return If the action was successful or not.
     */
    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {

        if(DEBUG_THIS)
        {
            if(!pLevel.isClientSide()) spreadRoutine((ServerWorld) pLevel, pPos, false);
            SpreadingTile tile = getTileEntity(pLevel, pPos);

            String debug = "Block at (" +
                    pPos.getX() + ", " +
                    pPos.getY() + ", " +
                    pPos.getZ() + ") " +
                    "maxSpreadAttempts: " + (tile.getMaxSpreadAttempts()) +
                    " spreadAttempts: " + (tile.getSpreadAttempts()) +
                    " tickTracker: " + tickTracker;
            //if(pLevel.isClientSide())
            pPlayer.displayClientMessage(new StringTextComponent(debug), false);
        }
        return ActionResultType.SUCCESS;
    }


    @Override
    public void tick(BlockState blockState, ServerWorld serverWorld, BlockPos bp, Random random) {
        tickTracker++;
        spreadRoutine(serverWorld, bp, doesSpreadRandomly());
        super.tick(blockState, serverWorld, bp, random);
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
        super.setPlacedBy(world, bp, blockState, entity, itemStack);
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
