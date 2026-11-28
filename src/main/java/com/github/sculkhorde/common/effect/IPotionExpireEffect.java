package com.github.sculkhorde.common.effect;

import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

public interface IPotionExpireEffect {

    void onPotionExpire(MobEffectEvent.Expired event);
}
