package com.github.sculkhorde.common.item;

import java.util.List;

import com.github.sculkhorde.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhorde.core.ModCreativeModeTab;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

public class CustomItemProjectile extends Item implements IForgeItem {

    /** CONSTRUCTORS **/

    /**
     * The Constructor that takes in properties
     * @param properties The Properties
     */
    public CustomItemProjectile(Properties properties) {
        super(properties);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering items in ItemRegistry.java can look cleaner
     */
    public CustomItemProjectile() {
        this(getProperties());
    }

    /** ACCESSORS **/

    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getProperties()
    {
        return new Properties().stacksTo(16).tab(ModCreativeModeTab.SCULK_HORDE_TAB);
    }

    public float getPower()
    {
        return 1.5f;
    }

    public float getInaccuracy()
    {
        return 0.0f;
    }

    public float getPitchOffset()
    {
        return 0.0f;
    }

    public float getDamage()
    {
        return 5f;
    }

    public CustomItemProjectileEntity getCustomItemProjectileEntity(Level level, Player player)
    {
        return new CustomItemProjectileEntity(level, player, getDamage());
    }

    /** MODIFIERS **/

    /** EVENTS **/


    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {#onItemUse}.
     */
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F);
        if (!pLevel.isClientSide) {
            CustomItemProjectileEntity projectile = getCustomItemProjectileEntity(pLevel, pPlayer);
            projectile.setItem(itemstack);
            projectile.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), getPitchOffset(), getPower(), getInaccuracy());
            pLevel.addFreshEntity(projectile);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.isCreative()) {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    //This changes the text you see when hovering over an item
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        tooltip.add(Component.translatable("tooltip.sculkhorde.custom_item_projectile")); //Text that displays if not holding shift

    }
}
