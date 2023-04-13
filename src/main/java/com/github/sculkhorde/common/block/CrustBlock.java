package com.github.sculkhorde.common.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.common.extensions.IForgeBlock;

public class CrustBlock extends Block implements IForgeBlock {

    /**
     * MATERIAL is simply what the block is made up. This affects its behavior & interactions.<br>
     * MAP_COLOR is the color that will show up on a map to represent this block
     */
    public static Material MATERIAL = Material.DIRT;
    public static MaterialColor MAP_COLOR = MaterialColor.COLOR_BLUE;

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
    public static float BLAST_RESISTANCE = 0.6f;

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
     *  All these variables below define the effect given to the player when they step on it.<br>
     *
     *  EFFECT_TICKS is how many ticks the effect will last. There are 20 ticks in a second.<br>
     *  EFFECT_STRENGTH is the level of the effect<br>
     *  STEP_ON_EFFECT is the actual instance of the effect given to the entity
     */
    public static int EFFECT_TICKS = 200;
    public static int EFFECT_STRENGTH = 3;
    public static MobEffectInstance STEP_ON_EFFECT = new MobEffectInstance(
            MobEffects.DIG_SLOWDOWN,
            EFFECT_TICKS,
            EFFECT_STRENGTH);

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public CrustBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public CrustBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return BlockBehaviour.Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .requiresCorrectToolForDrops()
                .sound(SoundType.GRASS);
    }
}
