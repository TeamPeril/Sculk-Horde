package com.github.sculkhoard.common.item;

import com.github.sculkhoard.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.util.ForgeEventSubscriber;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeItem;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

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
        return new Properties()
                .tab(SculkHoard.SCULK_GROUP);
    }

    @Override
    public UseAction getUseAnimation(final ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(final ItemStack stack) {
        return 72000;
    }

    /** MODIFIERS **/

    /** EVENTS **/


    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {#onItemUse}.
     */
    public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pLevel.playSound((PlayerEntity)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        if (!pLevel.isClientSide) {
            CustomItemProjectileEntity projectile = new CustomItemProjectileEntity(pLevel, pPlayer, 5f);
            projectile.setItem(itemstack);
            projectile.shootFromRotation(pPlayer, pPlayer.xRot, pPlayer.yRot, 0.0F, 1.5F, 1.0F);
            pLevel.addFreshEntity(projectile);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.abilities.instabuild) {
            itemstack.shrink(1);
        }

        return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
    }
}
