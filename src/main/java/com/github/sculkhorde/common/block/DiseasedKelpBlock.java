package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.*;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

public class DiseasedKelpBlock extends Block implements IForgeBlock, LiquidBlockContainer {

    /*
     *  NOTE:
     *      In order for this block to render correctly, you must
     *      edit ClientModEventSubscriber.java to tell Minecraft
     *      to render this like a cutout.
     */

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
     * Denotes whether this is the end block or not.
     */
    public static final BooleanProperty END = BooleanProperty.create("end");

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public DiseasedKelpBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any().setValue(END, false));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public DiseasedKelpBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.copy(ModBlocks.GRASS.get());
    }

    /**
     * Necessary for this to work.
     * @param builder
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(END);
    }

    /// #### ACCESSORS ####
    public static boolean isKelp(BlockState blockState)
    {
        if(blockState.is(Blocks.KELP) || blockState.is(Blocks.KELP_PLANT))
        {
            return true;
        }

        return false;
    }

    public static boolean isDiseasedKelp(BlockState blockState)
    {
        if(blockState.is(ModBlocks.DISEASED_KELP_BLOCK.get()))
        {
            return true;
        }

        return false;
    }

    public static boolean isKelpOrDiseasedKelp(BlockState blockState)
    {
        return isKelp(blockState) || isDiseasedKelp(blockState);
    }

    public static boolean isDiseasedKelpEndBlock(BlockState blockState)
    {
        if(!isDiseasedKelp(blockState) || !blockState.hasProperty(END))
        {
            return false;
        }

        return blockState.getValue(END);
    }

    public static boolean isKelpEndBlock(BlockState blockState)
    {
        if(!isKelp(blockState))
        {
            return false;
        }

        return blockState.is(Blocks.KELP_PLANT);
    }


    public boolean isBlockAboveKelpOrDiseasedKelp(LevelReader level, BlockPos pos)
    {
        return isKelpOrDiseasedKelp(level.getBlockState(pos.above()));
    }

    public boolean isBlockBelowKelpOrDiseasedKelp(LevelReader level, BlockPos pos)
    {
        return isKelpOrDiseasedKelp(level.getBlockState(pos.below()));
    }

    /** MODIFIERS **/



    public static void setEndBlock(Level level, BlockPos pos, Boolean setEndBlock)
    {
        BlockState blockState = level.getBlockState(pos);

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
        if(isDiseasedKelp(blockState))
        {
            if(setEndBlock && !isDiseasedKelpEndBlock(blockState))
            {
                BlockAlgorithms.setBlockMisc(level, pos, blockState.setValue(END, true));
            }
            else if(!setEndBlock)
            {
                BlockAlgorithms.setBlockMisc(level, pos, blockState.setValue(END, false));
            }
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {

        if (isBlockAboveKelpOrDiseasedKelp(level, pos)) {
            setEndBlock((Level) level, pos, false);
        } else {
            setEndBlock((Level) level, pos, true);
        }
    }

    /** Makes entities slow and damages them. I stole this code from the berry bush.<br>
     * @param blockState The current blockstate
     * @param world The world this block si in
     * @param blockPos The position of this block
     * @param entity The entity inside
     */
    public void entityInside(BlockState blockState, Level world, BlockPos blockPos, Entity entity) {
        // If the entity is not a living entity, don't do anything
        if (world.isClientSide)
        {
            return;
        }


        if(entity instanceof LivingEntity livingEntity)
        {
            // If the entity is a sculk, don't do anything
            if(EntityAlgorithms.isInvalidTargetForSculkHorde(livingEntity))
            {
                return;
            }

            LivingEntity vicitim = (livingEntity);

            if(vicitim.getMaxHealth() / 2 >= vicitim.getHealth())
            {
                return;
            }

            livingEntity.makeStuckInBlock(blockState, new Vec3(0.8F, 0.75D, (double)0.8F));
            livingEntity.hurt(livingEntity.damageSources().generic(), 1.0F);
            EntityAlgorithms.applyEffectToTarget((livingEntity), ModMobEffects.SCULK_INFECTION.get(), TickUnits.convertSecondsToTicks(30), 0);

        }
        else if(entity instanceof ItemEntity item)
        {
            if(!ModConfig.SERVER.isItemEdibleToCursors(item))
            {
                return;
            }

            entity.discard();
            int massToAdd = ((ItemEntity)entity).getItem().getCount();
            ModSavedData.getSaveData().addSculkAccumulatedMass(massToAdd);
            SculkHorde.statisticsData.addTotalMassFromInfestedCursorItemEating(massToAdd);
        }

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {

        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.diseased_kelp_block.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.diseased_kelp_block.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter p_54766_, BlockPos p_54767_, BlockState p_54768_, Fluid p_54769_) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor p_54770_, BlockPos p_54771_, BlockState p_54772_, FluidState p_54773_) {
        return false;
    }

    public BlockState updateShape(BlockState oldState, Direction dir, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos pos2) {
        return !oldState.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(oldState, dir, newState, level, pos, pos2);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {

        if(levelReader.getFluidState(blockPos).isEmpty())
        {
            return false;
        }

        return levelReader.getBlockState(blockPos.below()).isSolid()
                || isBlockBelowKelpOrDiseasedKelp(levelReader, blockPos);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        boolean isTop = !isBlockAboveKelpOrDiseasedKelp(level, pos);
        return this.defaultBlockState().setValue(END, isTop);
    }

    @Override
    public void onPlace(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean idk) {
        super.onPlace(newState, level, pos, newState, idk);

        if(level.isClientSide)
        {
            return;
        }

        if (isBlockAboveKelpOrDiseasedKelp(level, pos)) {
            setEndBlock(level, pos, false);
        } else {
            setEndBlock(level, pos, true);
        }

        if(isDiseasedKelp(level.getBlockState(pos.below())))
        {
            setEndBlock(level, pos.below(), false);
        }
        else if(isKelp(level.getBlockState(pos.below())))
        {
            level.setBlock(pos.below(), Blocks.KELP_PLANT.defaultBlockState(), 16);
        }
    }


    public FluidState getFluidState(BlockState p_54319_) {
        return Fluids.WATER.getSource(false);
    }

    // #### Collision Code ####
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.1D, 0.0D, 16.0D, 15.9D, 16.0D);
    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState p_221566_, BlockGetter p_221567_, BlockPos p_221568_) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
        if (collisionContext instanceof EntityCollisionContext entityCollisionContext){
            if (entityCollisionContext.getEntity() instanceof LivingEntity livingEntity){
                return Shapes.empty();
            }
        }
        return super.getCollisionShape(state, getter, pos, collisionContext);
    }

    @Override
    public boolean isPathfindable(BlockState p_154258_, BlockGetter p_154259_, BlockPos p_154260_, PathComputationType p_154261_) {
        return true;
    }
}
