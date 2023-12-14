package com.github.sculkhorde.common.block;

import static com.github.sculkhorde.core.SculkHorde.savedData;

import javax.annotation.Nullable;

import com.github.sculkhorde.common.blockentity.SculkAncientNodeBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.ModSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.extensions.IForgeBlock;


/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class SculkAncientNodeBlock extends BaseEntityBlock implements IForgeBlock {
    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = -1.0F;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 3600000.0F;

    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 2);
    public static final int STATE_RECIEVE_VIBRATION = 0;
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_DEFEATED = 2;

    public SculkAncientNodeBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(STATE, STATE_RECIEVE_VIBRATION));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkAncientNodeBlock() {
        this(getProperties());
    }

    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }

        boolean ItemIsPureSouls = playerIn.getMainHandItem().is(ModItems.PURE_SOULS.get());
        boolean ItemIsCryingSouls = playerIn.getMainHandItem().is(ModItems.CRYING_SOULS.get());


        if(ItemIsPureSouls && !savedData.isHordeDefeated())
        {
            if(!areAllNodesDestroyed())
            {
                playerIn.displayClientMessage(Component.literal("The Ancient Sculk Node cannot be destroyed until all remaining Sculk Nodes are!"), true);
                level.playSound(playerIn, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.MASTER);
                return InteractionResult.FAIL;
            }

            savedData.setHordeState(ModSavedData.HordeState.DEFEATED);
            level.players().forEach(player -> player.displayClientMessage(Component.literal("The Ancient Sculk Node has been Defeated!"), true));
            level.players().forEach(player -> level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F));

            //Spawn Explosion that Does No Damage
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 0.0F, Level.ExplosionInteraction.NONE);
            return InteractionResult.CONSUME;
        }

        if(ItemIsCryingSouls && !savedData.isHordeActive())
        {
            savedData.setHordeState(ModSavedData.HordeState.ACTIVE);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    public boolean areAllNodesDestroyed()
    {
        if(savedData == null) { return true; }
        return savedData.getNodeEntries().isEmpty();
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

    // Getters

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.of(Material.STONE)
                .color(MaterialColor.COLOR_BLUE)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.GRASS);
        return prop;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(STATE, STATE_RECIEVE_VIBRATION);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STATE);
    }

    /**
     * Gets ticked whenever it recieves a vibration. <br>
     * Note: I want to make it so this block ticks on a regular basis, but also when it recieves a vibration.
     * However, I don't know how to do that yet. Its either or, but not both.
     * @param level The level
     * @param blockState The current blockstate
     * @param blockEntityType The blockentity type
     * @return The ticker
     * @param <T> The blockentity type
     */
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return BaseEntityBlock.createTickerHelper(blockEntityType,
                ModBlockEntities.SCULK_ANCIENT_NODE_BLOCK_ENTITY.get(),
                SculkAncientNodeBlockEntity::tick);
    }

    @Nullable
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T blockEntityListener) {
        if (blockEntityListener instanceof SculkAncientNodeBlockEntity blockEntity) {
            return blockEntity.getListener();
        } else {
            return null;
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SculkAncientNodeBlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
}
