package com.github.sculkhoard.common.block;

import com.github.sculkhoard.common.tileentity.SculkBeeNestCellTile;
import com.github.sculkhoard.core.ItemRegistry;
import com.github.sculkhoard.core.ParticleRegistry;
import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class SculkBeeNestCellBlock extends Block implements IForgeBlock {

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
    public static int HARVEST_LEVEL = -1;

    /**
     * This property mature represents different variants of this block. <br>
     * mature = 0; This block has no harvestable resin. <br>
     * mature = 1; This block has harvestable resin. <br>
     * mature = 2; re-skin of above. <br>
     * mature = 3; re-skin of above. <br>
     */
    public static final IntegerProperty MATURE = IntegerProperty.create("mature", 0, 3);

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkBeeNestCellBlock(Properties prop)
    {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any().setValue(MATURE, 0));
    }

    /**
     * Necessary for this to work.
     * @param builder
     */
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(MATURE);
    }

    /**
     * Determines what the blockstate should be for placement.
     * @param context
     * @return
     */
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.defaultBlockState().setValue(MATURE, 0);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkBeeNestCellBlock() {
        this(getProperties());
    }


    /** PROPERTIES **/

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
     * Returns If true we have a tile entity
     * @param state The current block state
     * @return True
     */
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    /** ACCESSORS **/

    public boolean isMature(BlockState pState)
    {
         return pState.getValue(MATURE) != 0;
    }

    /** MODIFIERS **/

    public void setMature(World pLevel, BlockState pState, BlockPos pPos)
    {
        Random random = new Random();
        /**
         * Sets a block state into this world.Flags are as follows:
         * 1 will cause a block update.
         * 2 will send the change to clients.
         * 4 will prevent the block from being re-rendered.
         * 8 will force any re-renders to run on the main thread instead
         * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
         * 32 will prevent neighbor reactions from spawning drops.
         * 64 will signify the block is being moved.
         * Flags can be OR-ed
         */
        pLevel.setBlock(pPos, pState.setValue(MATURE, Integer.valueOf(random.nextInt(2) + 1)), 3);
    }

    public void resetMature(World pLevel, BlockState pState, BlockPos pPos)
    {
        /**
         * Sets a block state into this world.Flags are as follows:
         * 1 will cause a block update.
         * 2 will send the change to clients.
         * 4 will prevent the block from being re-rendered.
         * 8 will force any re-renders to run on the main thread instead
         * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
         * 32 will prevent neighbor reactions from spawning drops.
         * 64 will signify the block is being moved.
         * Flags can be OR-ed
         */
        pLevel.setBlock(pPos, pState.setValue(MATURE, Integer.valueOf(0)), 3);
    }


    /** EVENTS **/

    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit)
    {

        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (isMature(pState))
        {
            if (itemstack.getItem() == Items.SHEARS)
            {
                pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                dropResin(pLevel, pPos);
                itemstack.hurtAndBreak(1, pPlayer, (p_226874_1_) ->
                {
                    p_226874_1_.broadcastBreakEvent(pHand);
                });
                resetMature(pLevel, pState, pPos);
            }
        }
        return ActionResultType.sidedSuccess(pLevel.isClientSide);
    }

    public static void dropResin(World pLevel, BlockPos pPos) {
        popResource(pLevel, pPos, new ItemStack(ItemRegistry.SCULK_RESIN.get(), 1));
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
        return TileEntityRegistry.SCULK_BEE_NEST_CELL_TILE.get().create();
    }

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
     * of whether the block can receive random update ticks
     */
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pPos, Random randIn) {
        BlockPos blockpos = pPos.above();
        if (worldIn.getBlockState(blockpos).isAir() && !worldIn.getBlockState(blockpos).isSolidRender(worldIn, blockpos)) {
            if (randIn.nextInt(50) == 0) {
                double d0 = (double)pPos.getX() + randIn.nextDouble();
                double d1 = (double)pPos.getY() + 1.0D;
                double d2 = (double)pPos.getZ() + randIn.nextDouble();
                //worldIn.addParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                worldIn.addParticle(ParticleRegistry.SCULK_CRUST_PARTICLE.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
                worldIn.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundCategory.BLOCKS, 0.2F + randIn.nextFloat() * 0.2F, 0.9F + randIn.nextFloat() * 0.15F, false);
            }

            if (randIn.nextInt(100) == 0) {
                worldIn.playLocalSound(pPos.getX(), pPos.getY(), pPos.getZ(), SoundEvents.LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + randIn.nextFloat() * 0.2F, 0.9F + randIn.nextFloat() * 0.15F, false);
            }
        }


    }
}
