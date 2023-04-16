package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.BlockEntityRegistry;
import com.github.sculkhorde.core.gravemind.Gravemind;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.List;


/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class SculkNodeBlock extends BaseEntityBlock implements IForgeBlock {

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.PLANT;
    public static MaterialColor MAP_COLOR = CrustBlock.MAP_COLOR;

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 50f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 10f;

    /**
     *  Harvest Level Affects what level of tool can mine this block and have the item drop<br>
     *
     *  -1 = All<br>
     *  0 = Wood<br>
     *  1 = Stone<br>
     *  2 = Iron<br>
     *  3 = Diamond<br>
     *  4 = Netherite
     */
    public static int HARVEST_LEVEL = 3;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkNodeBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkNodeBlock() {
        this(getProperties());
    }


    public static void FindAreaAndPlaceNode(ServerLevel level, BlockPos searchOrigin)
    {
        BlockPos newOrigin = new BlockPos(searchOrigin.getX(), level.getMinBuildHeight() + 35, searchOrigin.getZ());
        level.setBlockAndUpdate(newOrigin, BlockRegistry.SCULK_NODE_BLOCK.get().defaultBlockState());
        Gravemind.getGravemindMemory().addNodeToMemory(newOrigin);
        EntityType.LIGHTNING_BOLT.spawn(level, newOrigin, MobSpawnType.SPAWNER);
    }

    /**
     * This function is called when this block is placed. <br>
     * @param world The world the block is in
     * @param bp The position the block is in
     * @param blockState The state of the block
     * @param entity The entity that placed it
     * @param itemStack The item stack it was placed from
     */
    @Override
    public void setPlacedBy(Level world, BlockPos bp, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack)
    {
        super.setPlacedBy(world, bp, blockState, entity, itemStack);
        //If world isnt client side and we are in the overworld
        if(!world.isClientSide() && world.equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            Gravemind.getGravemindMemory().addNodeToMemory(bp);
        }
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
        Properties prop = Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.GRASS);
        return prop;
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof SculkNodeBlockEntity && !worldIn.isClientSide())
        {
            ((SculkNodeBlockEntity)tile).forceLoadChunksInRadius((ServerLevel) worldIn, pos, worldIn.getChunk(pos).getPos().x, worldIn.getChunk(pos).getPos().z);
        }

        if(worldIn.isClientSide())
        {
            // Play Sound that Can be Heard by all players
            worldIn.playSound(null, pos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);

            // Display Text On Player Screens
            for (Player player : worldIn.players()) {
                player.displayClientMessage(Component.translatable("message.sculk_horde.node_placed"), true);
            }
        }

    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof SculkNodeBlockEntity && !worldIn.isClientSide())
        {
            ((SculkNodeBlockEntity)tile).unloadChunksInRadius((ServerLevel) worldIn, pos, worldIn.getChunk(pos).getPos().x, worldIn.getChunk(pos).getPos().z);
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
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
        tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_brain")); //Text that displays if holding shift
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityRegistry.SCULK_NODE_BLOCK_ENTITY.get(), SculkNodeBlockEntity::tick);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SculkNodeBlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

}
