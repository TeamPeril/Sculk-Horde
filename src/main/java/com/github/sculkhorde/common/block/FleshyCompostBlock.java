package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.FleshyCompostBlockEntity;
import com.github.sculkhorde.common.blockentity.SculkMassBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;

public class FleshyCompostBlock extends BaseEntityBlock implements IForgeBlock, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

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

    public static final float SCULK_HOARD_MASS_TAX = (float) (1.0 / 3.0);
    public static double HEALTH_ABSORB_MULTIPLIER = 3;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public FleshyCompostBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(WATERLOGGED, false));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public FleshyCompostBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.of()
                .mapColor(MapColor.CLAY)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.SLIME_BLOCK)
                .noOcclusion();
    }

    public static void placeBlock(LivingEntity entity)
    {
        int maxDistanceToCheck = 30;
        BlockPos placementPos = entity.blockPosition();

        for(int i = 0; i < maxDistanceToCheck; i++)
        {
            if(entity.level().getBlockState(placementPos.below()).canBeReplaced(Fluids.WATER))
            {
                placementPos = placementPos.below();
            }
        }
        FleshyCompostBlock flesh = ModBlocks.PASTY_ORGANIC_MASS.get();
        flesh.spawn(entity.level(), placementPos.below(), entity);

    }

    public void spawn(Level world, BlockPos originPos, LivingEntity livingEntity)
    {
        BlockPos placementPos = originPos.above();

        if(world.getBlockState(placementPos).isAir() || world.getBlockState(placementPos).canBeReplaced(Fluids.WATER))
        {
            world.setBlockAndUpdate(placementPos, this.defaultBlockState());
            if(world.getBlockEntity(placementPos) instanceof FleshyCompostBlockEntity blockEntity)
            {
                blockEntity.setStoredSculkMass((int) livingEntity.getMaxHealth());
            }
        }
        else
        {
            SculkHorde.savedData.addSculkAccumulatedMass((int) livingEntity.getMaxHealth());
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        BlockState blockstate = this.defaultBlockState();
        if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
            return blockstate.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));

        }
        return null;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED);
    }

    public BlockState updateShape(BlockState p_152151_, Direction p_152152_, BlockState p_152153_, LevelAccessor p_152154_, BlockPos p_152155_, BlockPos p_152156_) {
        if (p_152151_.getValue(WATERLOGGED)) {
            p_152154_.scheduleTick(p_152155_, Fluids.WATER, Fluids.WATER.getTickDelay(p_152154_));
        }

        return super.updateShape(p_152151_, p_152152_, p_152153_, p_152154_, p_152155_, p_152156_);
    }

    public FluidState getFluidState(BlockState p_152158_) {
        return p_152158_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152158_);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
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
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter iBlockReader, BlockPos blockPos, CollisionContext iSelectionContext) {
        //Block.box(xOffset, yOffset, zOffset, width, height, length)
        return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);
    }


    @Override
    public boolean canSurvive(BlockState p_60525_, LevelReader p_60526_, BlockPos p_60527_) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canBeReplaced(BlockState p_60535_, Fluid p_60536_) {
        return false;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new FleshyCompostBlockEntity(blockPos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.FLESHY_COMPOST_BLOCK_ENTITY.get(), FleshyCompostBlockEntity::tick);
    }
}
