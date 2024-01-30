package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

public class SculkSummonerBlock extends BaseEntityBlock implements IForgeBlock, SimpleWaterloggedBlock {

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
    public static final BooleanProperty VIBRATION_COOLDOWN = BooleanProperty.create("vibration_cooldown");
    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkSummonerBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(VIBRATION_COOLDOWN, false)
                .setValue(WATERLOGGED, false));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkSummonerBlock() {
        this(getProperties());
    }

    /** ~~~~~~~~ Properties ~~~~~~~~ **/

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.copy(Blocks.SCULK_SHRIEKER)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .noLootTable()
                .noOcclusion()
                .sound(SoundType.SCULK_SHRIEKER);
        return prop;
    }


    /**
     * Determines what the blockstate should be for placement.
     * @param context
     * @return
     */
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(VIBRATION_COOLDOWN, false).setValue(WATERLOGGED, false);

    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(VIBRATION_COOLDOWN).add(WATERLOGGED);
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
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_)
    {
        return Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    }

    /** ~~~~~~~~ Events ~~~~~~~~ **/

    /**
     * This is the description the item of the block will display when hovered over.
     * @param stack The item stack
     * @param iBlockReader A block reader
     * @param tooltip The tooltip
     * @param flagIn The flag
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_summoner.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_summoner.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }

    @Nullable
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel p_222165_, T p_222166_) {
        if (p_222166_ instanceof SculkSummonerBlockEntity blockEntity) {
            return blockEntity.getListener();
        } else {
            return null;
        }
    }

    // Block Entity Related

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {

        if(level.isClientSide)
        {
            return null;
        }


        if(blockState.getValue(VIBRATION_COOLDOWN))
        {
            return BaseEntityBlock.createTickerHelper(blockEntityType, ModBlockEntities.SCULK_SUMMONER_BLOCK_ENTITY.get(), SculkSummonerBlockEntity::tickOnCoolDown);
        }


        return BaseEntityBlock.createTickerHelper(blockEntityType, ModBlockEntities.SCULK_SUMMONER_BLOCK_ENTITY.get(), (level1, pos, state, entity) -> {
            VibrationSystem.Ticker.tick(level1, entity.getVibrationData(), entity.getVibrationUser());
        });
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SculkSummonerBlockEntity(blockPos, state);
    }

    /* Animation */

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // Water Log Handling, stole this from BaseRailBlock
    public BlockState updateShape(BlockState p_152151_, Direction p_152152_, BlockState p_152153_, LevelAccessor p_152154_, BlockPos p_152155_, BlockPos p_152156_) {
        if (p_152151_.getValue(WATERLOGGED)) {
            p_152154_.scheduleTick(p_152155_, Fluids.WATER, Fluids.WATER.getTickDelay(p_152154_));
        }

        return super.updateShape(p_152151_, p_152152_, p_152153_, p_152154_, p_152155_, p_152156_);
    }

    public FluidState getFluidState(BlockState p_152158_) {
        return p_152158_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152158_);
    }
}
