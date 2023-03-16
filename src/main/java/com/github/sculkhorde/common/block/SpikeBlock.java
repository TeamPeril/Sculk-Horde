package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.entity.SculkLivingEntity;
import com.github.sculkhorde.core.DamageSourceRegistry;
import com.github.sculkhorde.core.EffectRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SpikeBlock extends SculkFloraBlock implements IForgeBlock {

    /*
     *  NOTE:
     *      In order for this block to render correctly, you must
     *      edit ClientModEventSubscriber.java to tell Minecraft
     *      to render this like a cutout.
     */

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.STONE;
    public static MaterialColor MAP_COLOR = MaterialColor.TERRACOTTA_WHITE;

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 4f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 6f;

    /**
     * PREFERRED_TOOL determines what type of tool will break the block the fastest and be able to drop the block if possible
     */
    public static ToolType PREFERRED_TOOL = ToolType.PICKAXE;

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

   // public static Effect INFECT_EFFECT = EffectRegistry.SCULK_INFECTION.get();
    public static int INFECT_DURATION = 500;
    public static int INFECT_LEVEL = 1;

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SpikeBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SpikeBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .harvestTool(PREFERRED_TOOL)
                .harvestLevel(HARVEST_LEVEL)
                .sound(SoundType.SLIME_BLOCK)
                .noCollission()
                .air();
    }

    /** Makes entities slow and damages them. I stole this code from the berry bush.<br>
     * @param blockState The current blockstate
     * @param world The world this block si in
     * @param blockPos The position of this block
     * @param entity The entity inside
     */
    public void entityInside(BlockState blockState, World world, BlockPos blockPos, Entity entity) {
        if (entity instanceof LivingEntity &&  !(entity instanceof SculkLivingEntity))
        {
            entity.makeStuckInBlock(blockState, new Vector3d((double)0.8F, 0.75D, (double)0.8F));

            if (!world.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ()))
            {
                double d0 = Math.abs(entity.getX() - entity.xOld);
                double d1 = Math.abs(entity.getZ() - entity.zOld);
                if (d0 >= (double)0.003F || d1 >= (double)0.003F)
                {
                    entity.hurt(DamageSourceRegistry.SCULK_SPIKE, 1.0F);
                    ((LivingEntity) entity).addEffect(new EffectInstance(EffectRegistry.SCULK_INFECTION.get(), INFECT_DURATION, INFECT_LEVEL));
                    world.destroyBlock(blockPos, false);
                }
            }
        }
    }


    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
     * of whether the block can receive random update ticks
     */
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
        VoxelShape voxelshape = this.getShape(pState, pLevel, pPos, ISelectionContext.empty());
        Vector3d vector3d = voxelshape.bounds().getCenter();
        double d0 = (double)pPos.getX() + vector3d.x;
        double d1 = (double)pPos.getZ() + vector3d.z;

        for(int i = 0; i < 3; ++i) {
            if (pRand.nextBoolean()) {
                pLevel.addParticle(ParticleTypes.MYCELIUM, d0 + pRand.nextDouble() / 5.0D, (double)pPos.getY() + (0.5D - pRand.nextDouble()), d1 + pRand.nextDouble() / 5.0D, 0.0D, 0.0D, 0.0D);
            }
        }

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
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader iBlockReader, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(new TranslationTextComponent("tooltip.sculkhorde.spike")); //Text that displays if holding shift
    }
}
