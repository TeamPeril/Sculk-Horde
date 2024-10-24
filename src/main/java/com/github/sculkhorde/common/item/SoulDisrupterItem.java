package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SoulDisrupterItem extends Item implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public SoulDisrupterItem(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public SoulDisrupterItem() {this(getProperties());}

	/**
	 * Determines the properties of an item.<br>
	 * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
	 * @return The Properties of the item
	 */
	public static Properties getProperties()
	{
		return new Properties()
				.rarity(Rarity.RARE)
				.stacksTo(1);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
		{
			tooltip.add(Component.translatable("tooltip.sculkhorde.soul_disrupter.functionality"));
		}
		else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
		{
			tooltip.add(Component.translatable("tooltip.sculkhorde.soul_disrupter.lore"));
		}
		else
		{
			tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
		}
	}

	public int getCooldownTicks()
	{
		return 0;
	}


	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		BlockPos targetPos = EntityAlgorithms.playerTargetBlockPos(playerIn, false);

		if(worldIn.isClientSide())
		{
			// Do something in the future
			return InteractionResultHolder.fail(itemstack);
		}

		//If item is not on cool down
		if(!playerIn.getCooldowns().isOnCooldown(this) && targetPos != null)
		{

			playerIn.getCooldowns().addCooldown(this, getCooldownTicks());

			executePower(playerIn);

			playerIn.getItemInHand(handIn).shrink(1);
			playerIn.playSound(SoundEvents.ANVIL_BREAK);

			return InteractionResultHolder.pass(itemstack);
		}
		return InteractionResultHolder.fail(itemstack);
	}

	protected static HitResult getPlayerHitResult(Level level, Player player, ClipContext.Fluid fluid) {
		float f = player.getXRot();
		float f1 = player.getYRot();
		Vec3 vec3 = player.getEyePosition();
		float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
		float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
		float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
		float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double distance = 64;
		Vec3 vec31 = vec3.add((double)f6 * distance, (double)f5 * distance, (double)f7 * distance);
		return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, fluid, player));
	}

	public static void shootPurityBeam(Vec3 origin, Player player, float radius, float thickness)
	{

		// Perform ray trace
		HitResult hitResult = getPlayerHitResult(player.level(), player, ClipContext.Fluid.NONE);

		Vec3 hitVector = hitResult.getLocation();

		Vec3 targetVector = hitVector.subtract(origin);
		Vec3 direction = targetVector.normalize();

		Vec3 beamPath = hitVector.subtract(origin);


		Vec3 up = new Vec3(0, 1, 0);
		Vec3 right = direction.cross(up).normalize();
		Vec3 forward = direction.cross(right).normalize();


		// Create a hitbox along the beam path
		AABB hitbox = new AABB(origin, hitVector).inflate(radius);

		// Check for entities within the hitbox
		List<Entity> entitiesHit = player.level().getEntities(null, hitbox);

		for (Entity entity : entitiesHit) {
			// Handle entity hit logic here
			if(entity.getUUID() != player.getUUID())
			{
				if(entity instanceof LivingEntity livingEntity)
				{
					livingEntity.addEffect(new MobEffectInstance(ModMobEffects.SOUL_DISRUPTION.get(), TickUnits.convertMinutesToTicks(1), 0), player);
				}
			}
		}

		// Spawn Particles
		for (float i = 1; i < Mth.floor(beamPath.length()) + 1; i += 0.3F) {
			Vec3 vec33 = origin.add(direction.scale((double) i));

			// Create a circle of particles around vec33
			for (int j = 0; j < thickness; ++j) {
				double angle = 2 * Math.PI * j / thickness;
				double xOffset = radius * Math.cos(angle);
				double zOffset = radius * Math.sin(angle);
				Vec3 offset = right.scale(xOffset).add(forward.scale(zOffset));
				((ServerLevel) player.level()).sendParticles(ParticleTypes.TOTEM_OF_UNDYING, vec33.x + offset.x, vec33.y + offset.y, vec33.z + offset.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
		player.level().playSound(player,player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	public void executePower(Player player)
	{
		shootPurityBeam(player.getEyePosition(), player, 0.3F, 10);
	}
}
