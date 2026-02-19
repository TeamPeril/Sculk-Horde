package com.github.sculkhorde.systems.debugger_system;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DebuggerSystem {

    List<DebuggerModule> modules = new ArrayList<>();
    public final UUID uuid = UUID.randomUUID();

    public static CursorDebuggerModule cursorDebuggerModule = new CursorDebuggerModule();

    public DebuggerSystem()
    {
        addModule(cursorDebuggerModule);
    }

    public void addModule(DebuggerModule module)
    {
        if(!modules.contains(module))
        {
            modules.add(module);
        }
    }

    public List<DebuggerModule> getModules()
    {
        return modules;
    }

    public void serverTick()
    {
        for (DebuggerModule subSystem : modules) {
            if(subSystem.isActive)
                subSystem.serverTick();
        }
    }
}
