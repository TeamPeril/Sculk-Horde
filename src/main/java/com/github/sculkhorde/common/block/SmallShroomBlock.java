package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.entity.SculkLivingEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

public class SmallShroomBlock extends SculkFloraBlock implements IForgeBlock {

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
    public static Material MATERIAL = Material.PLANT;
    public static MaterialColor MAP_COLOR = MaterialColor.COLOR_CYAN;

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
     * PREFERRED_TOOL determines what type of tool will break the block the fastest and be able to drop the block if possible
     */
    public static ToolType PREFERRED_TOOL = ToolType.HOE;

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
    public SmallShroomBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SmallShroomBlock() {
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
                .instabreak();
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
                    //entity.hurt(DamageSourceRegistry.SCULK_SPIKE, 1.0F);
                }
            }
        }
    }
}
