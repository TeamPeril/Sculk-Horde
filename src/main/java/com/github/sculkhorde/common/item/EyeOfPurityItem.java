package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.NodeUtil;
import com.github.sculkhorde.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;
import java.util.Optional;

public class EyeOfPurityItem extends Item implements IForgeItem {

    /**
     * The Constructor that takes in properties
     * @param properties The Properties
     */
    public EyeOfPurityItem(Properties properties) {
        super(properties);

        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = blockSource.getPos().relative(direction);
                ServerLevel serverlevel = blockSource.getLevel();


                Optional<ModSavedData.NodeEntry> node = NodeUtil.getClosestNode(serverlevel, blockpos);
                if(node.isEmpty())
                {
                    SoundUtil.playSoundInLevel(serverlevel, blockpos, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS);
                    return itemStack;
                }

                BlockPos desiredPos = node.get().getPosition();
                spawnEyeOfPurity(serverlevel, blockpos.getCenter(), desiredPos, itemStack);
                itemStack.shrink(1);

                return itemStack;
            }
        });
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering items in ItemRegistry.java can look cleaner
     */
    public EyeOfPurityItem() {
        this(getProperties());
    }

    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getProperties()
    {
        return new Properties();
    }

    //This changes the text you see when hovering over an item
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.eye_of_purity")); //Text that displays if not holding shift

    }


    public static Entity spawnEyeOfPurity(ServerLevel level, Vec3 pos, BlockPos desiredPos, ItemStack itemStack)
    {
        EyeOfEnder eyeofender = new EyeOfEnder(level, pos.x, pos.y, pos.z);
        eyeofender.setItem(itemStack);
        eyeofender.signalTo(desiredPos);
        //level.gameEvent(GameEvent.PROJECTILE_SHOOT, eyeofender.position(), GameEvent.Context.of(playerIn));
        level.addFreshEntity(eyeofender);
        return eyeofender;
    }

    public InteractionResultHolder<ItemStack> use(Level levelIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(levelIn, playerIn, ClipContext.Fluid.NONE);
        if (blockhitresult.getType() == HitResult.Type.BLOCK)
        {
            return InteractionResultHolder.pass(itemstack);
        }

        playerIn.startUsingItem(handIn);
        if (levelIn.isClientSide()) {
            return InteractionResultHolder.consume(itemstack);
        }

        ServerLevel serverlevel = (ServerLevel)levelIn;
        Optional<ModSavedData.NodeEntry> node = NodeUtil.getClosestNode(serverlevel, playerIn.blockPosition());
        if(node.isEmpty())
        {
            SoundUtil.playSoundInLevel(levelIn, playerIn.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS);
            return InteractionResultHolder.consume(itemstack);
        }

        BlockPos desiredPos = node.get().getPosition();

        spawnEyeOfPurity((ServerLevel) levelIn, playerIn.getEyePosition(), desiredPos, itemstack);

        levelIn.gameEvent(GameEvent.PROJECTILE_SHOOT, playerIn.getEyePosition(), GameEvent.Context.of(playerIn));

        levelIn.playSound((Player)null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (levelIn.getRandom().nextFloat() * 0.4F + 0.8F));
        levelIn.levelEvent((Player)null, 1003, playerIn.blockPosition(), 0);
        if (!playerIn.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));
        playerIn.swing(handIn, true);
        playerIn.playSound(SoundEvents.ENCHANTMENT_TABLE_USE);
        return InteractionResultHolder.success(itemstack);
    }
}
