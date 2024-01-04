package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.SoulHarvesterBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class SoulHarvesterBlock extends BaseEntityBlock implements IForgeBlock {

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

    public static final BooleanProperty IS_PREPARED = BooleanProperty.create("is_prepared");

    public static final BooleanProperty IS_ACTIVE = BooleanProperty.create("is_active");

    public static final int MAX_EXPERIENCE = 100;
    public static final IntegerProperty EXPERIENCE_HARVESTED = IntegerProperty.create("experience_harvested", 0, MAX_EXPERIENCE);

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SoulHarvesterBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(IS_PREPARED, false)
                .setValue(IS_ACTIVE, false)
                .setValue(EXPERIENCE_HARVESTED, 0));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SoulHarvesterBlock() {
        this(getProperties());
    }

    /** ~~~~~~~~ Properties ~~~~~~~~ **/

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(IS_PREPARED, false)
                .setValue(IS_ACTIVE, false)
                .setValue(EXPERIENCE_HARVESTED, 0);

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(IS_PREPARED).add(IS_ACTIVE).add(EXPERIENCE_HARVESTED);
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.copy(Blocks.SCULK_SHRIEKER)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .noOcclusion()
                .sound(SoundType.SCULK_CATALYST);
        return prop;
    }


    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof SoulHarvesterBlockEntity) {
                ((SoulHarvesterBlockEntity) blockEntity).drops();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        if (pLevel.isClientSide())
        {
            return InteractionResult.sidedSuccess(pLevel.isClientSide());
        }

        BlockEntity entity = pLevel.getBlockEntity(pPos);
        if(entity instanceof SoulHarvesterBlockEntity) {
            NetworkHooks.openScreen(((ServerPlayer)pPlayer), (SoulHarvesterBlockEntity)entity, pPos);
        } else {
            throw new IllegalStateException("Our Container provider is missing!");
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
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

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.soul_harvester")); //Text that displays if not holding shift

    }
    

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SoulHarvesterBlockEntity(blockPos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level levelIn, BlockState blockStateIn, BlockEntityType<T> blockEntityTypeIn) {
        if(levelIn.isClientSide()) {
            return null;
        } else {
            return createTickerHelper(blockEntityTypeIn, ModBlockEntities.SOUL_HARVESTER_BLOCK_ENTITY.get(), SoulHarvesterBlockEntity::serverTick);
        }
    }

    /* Animation */

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

}
