package com.github.sculkhorde.common.potion;

import com.github.sculkhorde.core.ModCreativeModeTab;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class PuritySplashPotionItem extends PurityThrowablePotionItem {

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering items in ItemRegistry.java can look cleaner
     */
    public PuritySplashPotionItem() {this(getProperties());}
    public PuritySplashPotionItem(Properties properties) {
        super(properties);
    }

    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getProperties()
    {
        return new Properties()
                .rarity(Rarity.EPIC)
                .tab(ModCreativeModeTab.SCULK_HORDE_TAB);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_43243_, Player p_43244_, InteractionHand p_43245_) {
        p_43243_.playSound((Player)null, p_43244_.getX(), p_43244_.getY(), p_43244_.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (p_43243_.getRandom().nextFloat() * 0.4F + 0.8F));
        return super.use(p_43243_, p_43244_, p_43245_);
    }
}
