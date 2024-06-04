package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
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
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public DiseasedKelpBlock(Properties prop) {
        super(prop);
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
        return Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .requiresCorrectToolForDrops()
                .sound(SoundType.SLIME_BLOCK);
    }

    /** Makes entities slow and damages them. I stole this code from the berry bush.<br>
     * @param blockState The current blockstate
     * @param world The world this block si in
     * @param blockPos The position of this block
     * @param entity The entity inside
     */
    public void entityInside(BlockState blockState, Level world, BlockPos blockPos, Entity entity) {
        // If the entity is not a living entity, don't do anything
        if (!(entity instanceof LivingEntity) || world.isClientSide)
        {
            return;
        }

        // If the entity is a sculk, don't do anything
        if(EntityAlgorithms.isLivingEntityExplicitDenyTarget((LivingEntity) entity))
        {
            return;
        }

        LivingEntity vicitim = ((LivingEntity) entity);

        if(vicitim.getMaxHealth() / 2 >= vicitim.getHealth())
        {
            return;
        }

        entity.makeStuckInBlock(blockState, new Vec3(0.8F, 0.75D, (double)0.8F));
        entity.hurt(entity.damageSources().generic(), 1.0F);
        EntityAlgorithms.applyEffectToTarget(((LivingEntity) entity), ModMobEffects.DISEASED_CYSTS.get(), TickUnits.convertSecondsToTicks(10), 0);
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
        return levelReader.getBlockState(blockPos.below()).isSolid()
                || levelReader.getBlockState(blockPos.below()).is(this)
                || levelReader.getBlockState(blockPos.below()).is(Blocks.KELP)
                || levelReader.getBlockState(blockPos.below()).is(Blocks.KELP_PLANT);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return true;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_54302_) {
        FluidState fluidstate = p_54302_.getLevel().getFluidState(p_54302_.getClickedPos());
        return fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8 ? super.getStateForPlacement(p_54302_) : null;
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
