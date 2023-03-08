package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.entity.SculkLivingEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;

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
     * PREFERRED_TOOL determines what type of tool will break the block the fastest and be able to drop the block if possible
     */
    public static ToolType PREFERRED_TOOL = ToolType.SHOVEL;

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
    public static EffectInstance STEP_ON_EFFECT = new EffectInstance(
            Effects.DIG_SLOWDOWN,
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
        return AbstractBlock.Properties.of(MATERIAL, MAP_COLOR)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .harvestTool(PREFERRED_TOOL)
                .harvestLevel(HARVEST_LEVEL)
                .sound(SoundType.GRASS);
    }

    /**
     * Gives LivingEntities an effect if they step on this block
     * @param worldIn The world
     * @param pos The Block Position
     * @param entity The entity that stepped on the block
     */
    @Override
    public void stepOn(World worldIn, BlockPos pos, Entity entity)
    {
        if(!worldIn.isClientSide())//Only do this on the client
        {
            if(entity instanceof LivingEntity && !(entity instanceof SculkLivingEntity))//Only apply to living entities
            {
                LivingEntity livingEntity = ((LivingEntity) entity); //Cast
                livingEntity.addEffect(new EffectInstance(STEP_ON_EFFECT)); //Give effect
            }
        }
        super.stepOn(worldIn, pos, entity); //Execute Parent Code
    }

    /**
     * Returns the state that this block should transform into when right clicked by a tool.
     * For example: Used to determine if an axe can strip, a shovel can path, or a hoe can till.
     * Return null if vanilla behavior should be disabled.
     *
     * @param state The current state
     * @param world The world
     * @param pos The block position in world
     * @param player The player clicking the block
     * @param stack The stack being used by the player
     * @return The resulting state after the action has been performed
     */
    public BlockState getToolModifiedState(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack stack, ToolType toolType)
    {
        if(DEBUG_MODE) System.out.println("Hi I am a Crust Block, I don't do Anything :)");

        return null; //Just Return null because We Are Not Modifying it
    }
}
