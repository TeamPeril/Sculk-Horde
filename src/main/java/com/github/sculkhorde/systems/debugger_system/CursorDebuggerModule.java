package com.github.sculkhorde.systems.debugger_system;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.entity.Entity;

public class CursorDebuggerModule extends DebuggerModule {

    public CursorDebuggerModule()
    {
        super();
    }


    @Override
    public void serverTick() {
        super.serverTick();
        if(isDebuggingEnabled())
        {
            SculkHorde.debugSlimeSystem.serverTick();
        }
        else if(!SculkHorde.debugSlimeSystem.debugSlimes.isEmpty())
        {
            SculkHorde.debugSlimeSystem.debugSlimes.forEach(Entity::discard);
            SculkHorde.debugSlimeSystem.debugSlimes.clear();
        }

    }
}
