package com.github.sculkhoard.common.item;

import com.github.sculkhoard.common.entity.EntityAlgorithms;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AntiSculkMatter extends Item implements IForgeItem {

    /**
     * The Constructor that takes in properties
     * @param properties The Properties
     */
    public AntiSculkMatter(Properties properties) {
        super(properties);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering items in ItemRegistry.java can look cleaner
     */
    public AntiSculkMatter() {
        this(getProperties());
    }

    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getProperties()
    {
        return new Item.Properties()
                .tab(SculkHoard.SCULK_GROUP)
                .durability(1)
                .rarity(Rarity.EPIC);
    }

    //This changes the text you see when hovering over an item
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this

        //If User presses left shift, else
        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))	{
            tooltip.add(new TranslationTextComponent("tooltip.sculkhoard.dev_wand.shift")); //Text that displays if holding shift
        } else {
            tooltip.add(new TranslationTextComponent("tooltip.sculkhoard.dev_wand")); //Text that displays if not holding shift
        }
    }

    @Override
    public Rarity getRarity(ItemStack itemStack) {
        return Rarity.EPIC;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        //Get the item the player is holding
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        //Do a ray trace to see what block the player is looking at
        BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) getPlayerPOVHitResult(worldIn, playerIn, RayTraceContext.FluidMode.NONE);
        //If our ray trace hits a block
        if (blockraytraceresult.getType() == RayTraceResult.Type.BLOCK)
        {
            BlockPos blockpos = blockraytraceresult.getBlockPos();
            if(!worldIn.isClientSide()) SculkHoard.infestationConversionTable.convertToVictim((ServerWorld) worldIn, blockpos);
            return ActionResult.pass(itemstack);
        }
        else
        {
            return ActionResult.fail(itemstack);
        }
    }
}
