package com.github.sculkhoard.common.block;

import com.github.sculkhoard.common.block.BlockInfestation.SpreadingBlock;
import com.github.sculkhoard.common.block.BlockInfestation.SpreadingTile;
import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.entity_factory.ReinforcementContext;
import com.github.sculkhoard.common.tileentity.SculkMassTile;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.Random;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;
import static com.github.sculkhoard.core.SculkHoard.entityFactory;

public class SculkMassBlock extends SculkFloraBlock implements IForgeBlock {

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.PLANT;
    public static MaterialColor MAP_COLOR = MaterialColor.COLOR_CYAN;

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
    public static int HARVEST_LEVEL = -1;
    public static final float SCULK_HOARD_MASS_TAX = (float) (1.0 / 3.0);
    public static double HEALTH_ABSORB_MULTIPLIER = 3;
    public static int infestedChildBlockMaxSpreadAttempts = 20;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkMassBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkMassBlock() {
        this(getProperties());
    }

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
                .sound(SoundType.SLIME_BLOCK)
                .noOcclusion()
                .noDrops();
    }

    /**
     * Spawns A Sculk Mass. The sculk will cause the body to age 30 years over a few moments
     * to generate mass for the sculk. One third of this mass is taxed and given to the
     * gravemind. The rest of it is used to spawn a random sculk mob.
     * @param world The world the mass is in.
     * @param originPos The position to spawn the mob
     * @param victimHealth How much health the victim has.
     */
    //@OnlyIn(Dist.DEDICATED_SERVER)
    public void spawn(World world, BlockPos originPos, float victimHealth)
    {
        boolean DEBUG_THIS = false;
        BlockPos placementPos = originPos.above();
        int MAX_ATTEMPTS = 64;
        int attempts = 0;
        SculkMassTile thisTile;

        //Try and find solid ground to place this block on
        while(world.getBlockState(placementPos.below()).canBeReplaced(Fluids.WATER) && attempts <= MAX_ATTEMPTS)
        {
            placementPos = placementPos.below();
            attempts++;
        }
        //If was able to find correct placement in under MAX_ATTEMPTS, then place it
        if(attempts < MAX_ATTEMPTS)
        {
            //If sculk mass does not yet exist, add it
            if(!world.getBlockState(placementPos).equals(this.defaultBlockState()))
            {
                world.setBlockAndUpdate(placementPos, this.defaultBlockState());
                thisTile = getTileEntity(world, placementPos);

                //Calcualate the total mass collected
                int totalMassPreTax = (int) (victimHealth * HEALTH_ABSORB_MULTIPLIER);
                int totalMassTax = (int) (totalMassPreTax * SCULK_HOARD_MASS_TAX);
                int totalMassAfterTax = totalMassPreTax - totalMassTax;

                //Pay Mass Tax to the Sculk Hoard
                SculkHoard.entityFactory.addSculkAccumulatedMass(totalMassTax);

                if(DEBUG_MODE && DEBUG_THIS)
                {
                    System.out.println(
                            "\n" + "totalMassPreTax: " + totalMassPreTax + "\n"
                            + "totalMassTax: " + totalMassTax + "\n"
                            + "totalMassAfterTax: " + totalMassAfterTax + "\n"
                            + "Global Sculk Mass: " + entityFactory.getSculkAccumulatedMass()
                    );
                }

                //Keep track in this tile the total sculk mass we collected
                //BUG: thisTile can be null, not sure why
                if(thisTile != null) thisTile.addStoredSculkMass(totalMassAfterTax);
                else
                {
                    System.out.println("Attempted to Access NULL tile at "
                            + placementPos
                            + "which is of blockstate "
                            + world.getBlockState(placementPos)
                    );
                }

                //Replace Block Under sculk mass with infested variant if possible
                if(SculkHoard.infestationConversionTable.convertToActiveSpreader((ServerWorld) world, originPos.below()))
                {
                    SpreadingBlock spreadingBlock = BlockRegistry.SPREADING_BLOCK.get();
                    if(spreadingBlock.getTileEntity(world, placementPos.below()) != null && spreadingBlock.getTileEntity(world, placementPos.below()) instanceof SpreadingTile)
                        spreadingBlock.getTileEntity(world, placementPos.below()).setMaxSpreadAttempts(infestedChildBlockMaxSpreadAttempts);
                    //TODO: Figure out why this line above is causing crashing
                }
            }
        }
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
     * Will Attempt to call in reiforcements depending on how much sculk mass
     * was absored.
     * @param blockState The current Blockstate
     * @param serverWorld The current ServerWorld
     * @param thisBlockPos The current Block Position
     * @param random ???
     */
    @Override
    public void randomTick(BlockState blockState, ServerWorld serverWorld, BlockPos thisBlockPos, Random random) {
        boolean DEBUG_THIS = false;
        SculkMassTile thisTile = getTileEntity(serverWorld, thisBlockPos);
        EntityFactory entityFactory = SculkHoard.entityFactory;
        ReinforcementContext context = new ReinforcementContext(thisBlockPos.getX(), thisBlockPos.getY(), thisBlockPos.getZ());
        context.sender = ReinforcementContext.senderType.SculkMass;
        context.budget = thisTile.getStoredSculkMass();

        //Attempt to call in reinforcements and then update stored sculk mass
        entityFactory.requestReinforcementSculkMass(serverWorld, thisBlockPos, context);
        if(context.isRequestViewed && context.isRequestApproved)
        {
            thisTile.setStoredSculkMass(context.remaining_balance);
            //Destroy if run out of sculk mass
            if(thisTile.getStoredSculkMass() <= 0)
            {
                serverWorld.destroyBlock(thisBlockPos, false);
            }
        }
    }

    /**
     * Just returns the tile entity
     * @param world The world to check
     * @param thisBlockPos The position to check
     * @return The tile entity
     */
    public SculkMassTile getTileEntity(World world, BlockPos thisBlockPos)
    {
        //Get tile entity for this block
        TileEntity tileEntity = world.getBlockEntity(thisBlockPos);
        SculkMassTile thisTile = null;
        try
        {
            thisTile = (SculkMassTile) world.getBlockEntity(thisBlockPos);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return thisTile;
    }

    /**
     * Returns the state that this block should transform into when right clicked by a tool.
     * For example: Used to determine if an axe can strip, a shovel can path, or a hoe can till.
     * Return null if vanilla behavior should be disabled.
     *
     * @param state The current state
     * @param world The world
     * @param pos The block position in world
     * @param player The player clicking the block
     * @param stack The stack being used by the player
     * @return The resulting state after the action has been performed
     */
    public BlockState getToolModifiedState(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack stack, ToolType toolType)
    {
        if(DEBUG_MODE)
        {
            TileEntity tile = world.getBlockEntity(pos);
            if(tile instanceof SculkMassTile && tile != null)
            {

                String debug_text = "Block at (" +
                        pos.getX() + ", " +
                        pos.getY() + ", " +
                        pos.getZ() + ") " +
                        "getStoredSculkMass: " + ((SculkMassTile) tile).getStoredSculkMass();
                player.displayClientMessage(new StringTextComponent(debug_text), false);
            }
            else
            {
                System.out.println("Error accessing tile entity");
            }
        }

        return null; //Just Return null because We Are Not Modifying it
    }

    /**
     * Determines what block the spike can be placed on <br>
     * Goes through a list of valid blocks and checks if the
     * given block is in that list.<br>
     * @param blockState The block it is trying to be placed on
     * @param iBlockReader ???
     * @param pos The Position
     * @return True/False
     */
    @Override
    protected boolean mayPlaceOn(BlockState blockState, IBlockReader iBlockReader, BlockPos pos) {
        return !blockState.canBeReplaced(Fluids.WATER);
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
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, EntityType<?> entityType) {
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
        return TileEntityRegistry.SCULK_MASS_TILE.get().create();
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
     * Causes Model to be offset
     * @return
     */
    public AbstractBlock.OffsetType getOffsetType() {
        return OffsetType.NONE;
    }

    /**
     * Determines Block Hitbox <br>
     * Stole from NetherRootsBlock.java
     * @param blockState
     * @param iBlockReader
     * @param blockPos
     * @param iSelectionContext
     * @return
     */
    public VoxelShape getShape(BlockState blockState, IBlockReader iBlockReader, BlockPos blockPos, ISelectionContext iSelectionContext) {
        //Block.box(xOffset, yOffset, zOffset, width, height, length)
        return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);
    }

}
