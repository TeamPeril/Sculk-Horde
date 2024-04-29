package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.SculkAncientNodeBlockEntity;
import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.*;
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
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import static com.github.sculkhorde.core.SculkHorde.savedData;


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

    public static final BooleanProperty DEFEATED = BooleanProperty.create("defeated");

    public SculkAncientNodeBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(DEFEATED, false));
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkAncientNodeBlock() {
        this(getProperties());
    }

    public static boolean isDefeated(BlockState blockState)
    {
        return blockState.hasProperty(DEFEATED) && blockState.is(ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get()) && blockState.getValue(DEFEATED);
    }
    public static void setDefeated(ServerLevel world, BlockState blockState, BlockPos position, boolean value)
    {
        if(!blockState.hasProperty(DEFEATED) || !blockState.is(ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get())) { return; }
        world.setBlock(position, blockState.setValue(DEFEATED, Boolean.valueOf(value)), 3);
    }

    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }

        boolean IsHordeCureItem = playerIn.getMainHandItem().is(ModItems.HEART_OF_PURITY.get());
        boolean IsHordeRevivalItem = playerIn.getMainHandItem().is(ModItems.HEART_OF_THE_HORDE.get());


        if(IsHordeCureItem && !savedData.isHordeDefeated() && !ModConfig.SERVER.disable_defeating_sculk_horde.get())
        {
            savedData.setHordeState(ModSavedData.HordeState.DEFEATED);
            level.players().forEach(player -> player.displayClientMessage(Component.literal("The Ancient Sculk Node has been Defeated!"), true));
            level.players().forEach(player -> level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F));

            //Spawn Explosion that Does No Damage
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 0.0F, Level.ExplosionInteraction.NONE);

            setDefeated((ServerLevel) playerIn.level(), blockState, pos, true);

            playerIn.getMainHandItem().grow(-1);

            for(int i = 0; i < 10; i++)
            {
                CursorSurfacePurifierEntity purifier = new CursorSurfacePurifierEntity(level);
                purifier.setPos(pos.getCenter());
                purifier.setMaxTransformations(100);
                purifier.setMaxRange(30);
                purifier.setMaxLifeTimeMillis(TimeUnit.MINUTES.toMillis(5));
                purifier.setTickIntervalMilliseconds(10);
                level.addFreshEntity(purifier);
            }

            return InteractionResult.CONSUME;
        }

        if(IsHordeRevivalItem && !savedData.isHordeActive())
        {
            savedData.setHordeState(ModSavedData.HordeState.ACTIVE);
            setDefeated((ServerLevel) playerIn.level(), blockState, pos, false);
            playerIn.getMainHandItem().grow(-1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
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
        Properties prop = Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.GRASS);
        return prop;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(DEFEATED, false);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(DEFEATED);
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
