package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.ModParticles;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.Random;

//Not an actual block, just a parent class

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SculkFloraBlock extends BushBlock implements IForgeBlock {

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 3f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 6f;

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
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkFloraBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkFloraBlock() {
        this(getProperties());
    }


    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.copy(Blocks.POPPY)
                .mapColor(MapColor.TERRACOTTA_BLUE)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.GRASS)
                .noCollission()
                .instabreak()
                .requiresCorrectToolForDrops();

    }

    // SPawn particles
    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        super.animateTick(stateIn, worldIn, pos, rand);

        if (worldIn.isClientSide)
        {
            Random random = new Random();
            if (random.nextInt(10) == 0)
            {
                worldIn.addParticle(ModParticles.SCULK_CRUST_PARTICLE.get(), pos.getX(), pos.getY(), pos.getZ(), (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3);
            }
        }

    }



    /**
     * Determines what block the spike can be placed on <br>
     * Goes through a list of valid blocks and checks if the
     * given block is in that list.<br>
     * @param blockState The block it is trying to be placed on
     * @param iBlockReader An interface for objects like the world
     * @param pos The Position
     * @return True/False
     */
    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter iBlockReader, BlockPos pos) {

        return SculkHorde.blockInfestationTable.isCurable(blockState);
    }


    /**
     * Determines Block Hitbox <br>
     * Stole from NetherRootsBlock.java
     * @param p_220053_1_
     * @param p_220053_2_
     * @param p_220053_3_
     * @param p_220053_4_
     * @return
     */
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    }


    /**
     * Determines if an AI can walk through this block
     * @param blockState The block state of the block
     * @param iBlockReader ???
     * @param blockPos The block position
     * @param pathType ???
     * @return ???
     */
    public boolean isPathfindable(BlockState blockState, BlockGetter iBlockReader, BlockPos blockPos, PathComputationType pathType) {
        return pathType == PathComputationType.AIR && !this.hasCollision ? true : super.isPathfindable(blockState, iBlockReader, blockPos, pathType);
    }

    /**
     * I Stole this from berry bush, makes light pass through it.
     * @param blockState
     * @param iBlockReader
     * @param pos
     * @return
     */
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter iBlockReader, BlockPos pos) {
        return true;
    }

    /**
     * Causes Model to be offset
     * @return
     */
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }


    public boolean canBeReplacedByLeaves(BlockState state, LevelReader world, BlockPos pos) {
        return true;
    }


    public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
        return true;
    }

    public boolean canBeReplacedByLogs(BlockState state, LevelReader world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return SculkHorde.blockInfestationTable.isCurable(levelReader.getBlockState(blockPos.below()));
    }
}
