package com.github.sculkhorde.common.block;

import java.util.List;

import javax.annotation.Nullable;

import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import com.github.sculkhorde.common.blockentity.SoulHarvesterBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

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

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SoulHarvesterBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SoulHarvesterBlock() {
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
            /*
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof SoulHarvesterBlockEntity) {
                NetworkHooks.openScreen(((ServerPlayer)pPlayer), (SoulHarvesterBlockEntity)entity, pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
             */
        BlockEntity entity = pLevel.getBlockEntity(pPos);
        //entity.

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


    // Block Entity Related

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {

        if(level.isClientSide)
        {
            return null;
        }


        return createTickerHelper(blockEntityType, ModBlockEntities.SOUL_HARVESTER_BLOCK_ENTITY.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SculkSummonerBlockEntity(blockPos, state);
    }

    /* Animation */

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
