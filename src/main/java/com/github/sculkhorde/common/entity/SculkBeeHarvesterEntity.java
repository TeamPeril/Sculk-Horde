package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class SculkBeeHarvesterEntity extends Bee implements GeoEntity, FlyingAnimal {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added common/entity/ SculkBeeHarvesterEntity.java<br>
     * Added client/model/entity/ SculkBeeHarvesterModel.java<br>
     * Added client/renderer/entity/ SculkBeeHarvesterRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 20F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.25F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    /** Constructors **/

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBeeHarvesterEntity(EntityType<? extends SculkBeeHarvesterEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkBeeHarvesterEntity(Level worldIn)
    {
        this(EntityRegistry.SCULK_BEE_HARVESTER.get(), worldIn);
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    /**----------Accessor Methods----------**/


    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FLYING_SPEED, 0.6F);
    }

    /**----------Modifier Methods----------**/


    /**----------Event Methods----------**/

    /**
     * Prepares an array of goals to give to registerGoals() for the goalSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    private Goal[] goalSelectorPayload()
    {

        Goal[] goals =
                {

                };
        return goals;
    }

    /**
     * Prepares an array of goals to give to registerGoals() for the targetSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    private Goal[] targetSelectorPayload()
    {
        Goal[] goals =
                {
                        // Commented this out because it interferes with the bee's ability to go back into hive.
                        //new TargetAttacker(this).setAlertAllies(),
                };
        return goals;
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/
    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericFlyController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * We override this and keep it blank so that this mob doesnt not despawn
     */
    @Override
    public void checkDespawn() {}

    public boolean dampensVibrations() {
        return true;
    }

    /**
     * If a sculk living entity despawns, refund it's current health to the sculk hoard
     */
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.gravemind.getGravemindMemory().addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }

}
