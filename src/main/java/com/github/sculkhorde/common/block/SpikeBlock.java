package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.EffectRegistry;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SpikeBlock extends SculkFloraBlock implements IForgeBlock {

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
        return Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .requiresCorrectToolForDrops()
                .sound(SoundType.SLIME_BLOCK)
                .noCollission();
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
        if(EntityAlgorithms.isSculkLivingEntity.test((LivingEntity) entity))
        {
            return;
        }

        entity.makeStuckInBlock(blockState, new Vec3((double)0.8F, 0.75D, (double)0.8F));

        if (entity.xOld != entity.getX() || entity.zOld != entity.getZ())
        {
            double d0 = Math.abs(entity.getX() - entity.xOld);
            double d1 = Math.abs(entity.getZ() - entity.zOld);
            if (d0 >= (double)0.003F || d1 >= (double)0.003F)
            {
                entity.hurt(entity.damageSources().generic(), 1.0F);
                ((LivingEntity) entity).addEffect(new MobEffectInstance(EffectRegistry.SCULK_INFECTION.get(), INFECT_DURATION, INFECT_LEVEL));
                world.destroyBlock(blockPos, false);
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
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.spike")); //Text that displays if holding shift
    }
}
