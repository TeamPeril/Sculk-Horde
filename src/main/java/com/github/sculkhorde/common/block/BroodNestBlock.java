package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.BroodNestBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;

public class BroodNestBlock extends BaseEntityBlock implements IForgeBlock {

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
    public BroodNestBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any());
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public BroodNestBlock() {
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
                .mapColor(MapColor.COLOR_CYAN)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.SCULK_CATALYST);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
            return blockstate;

        }
        return null;
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

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide)
        {
            return null;
        }

        return BaseEntityBlock.createTickerHelper(blockEntityType, ModBlockEntities.BROOD_NEST_BLOCK_ENTITY.get(), (level1, pos, state, entity) -> {
            entity.tick();
            VibrationSystem.Ticker.tick(level1, entity.getVibrationData(), entity.getVibrationUser());
        });
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof BroodNestBlockEntity) {
            ((BroodNestBlockEntity)blockentity).summonBroodHatcher();
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean canSurvive(BlockState p_60525_, LevelReader p_60526_, BlockPos p_60527_) {
        return true;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new BroodNestBlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
}
