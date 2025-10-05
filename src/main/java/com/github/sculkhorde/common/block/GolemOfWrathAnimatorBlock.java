package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.GolemOfWrathAnimatorBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class GolemOfWrathAnimatorBlock extends BaseEntityBlock implements IForgeBlock {

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
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public GolemOfWrathAnimatorBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public GolemOfWrathAnimatorBlock() {
        this(getProperties());
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

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .noLootTable()
                .sound(SoundType.AMETHYST);
        return prop;
    }

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
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.golem_of_wrath_animator_block.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.golem_of_wrath_animator_block.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }


    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hitResult) {


        if(level.getBlockEntity(pos) instanceof GolemOfWrathAnimatorBlockEntity blockEntity)
        {
            if(blockEntity.getGolemAsLivingEntity().isEmpty())
            {
                return InteractionResult.FAIL;
            }

            Optional<BlockPos> spawnPos = blockEntity.getSpawnPositionsInCube(level, pos, 20);
            if(spawnPos.isPresent())
            {
                blockEntity.getGolemAsLivingEntity().get().teleportTo(spawnPos.get().getX(), spawnPos.get().getY(), spawnPos.get().getZ());
            }
            else
            {
                blockEntity.getGolemAsLivingEntity().get().teleportTo(spawnPos.get().getX(), spawnPos.get().getY(), spawnPos.get().getZ());
            }
        }

        return InteractionResult.FAIL;
    }

    // Block Entity Related

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.GOLEM_OF_WRATH_ANIMATOR_BLOCK_ENTITY.get(), GolemOfWrathAnimatorBlockEntity::tick);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new GolemOfWrathAnimatorBlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }



}
