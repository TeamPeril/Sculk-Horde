package com.github.sculkhorde.systems.debugger_system;

import com.github.sculkhorde.core.SculkHorde;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DebuggerSystem {

    List<DebuggerModule> modules = new ArrayList<>();
    public final UUID uuid = UUID.randomUUID();

    public static CursorDebuggerModule cursorDebuggerModule = new CursorDebuggerModule();
    public static EntityDebuggerModule entityDebuggerModule = new EntityDebuggerModule();
    public static ChunkLoaderDebuggerModule chunkLoaderDebuggerModule = new ChunkLoaderDebuggerModule();
    public static EventDebuggerModule eventDebuggerModule = new EventDebuggerModule();
    public static StructureDebuggerModule structureDebuggerModule = new StructureDebuggerModule();

    public DebuggerSystem()
    {
        addModule(cursorDebuggerModule);
        addModule(entityDebuggerModule);
        addModule(chunkLoaderDebuggerModule);
        addModule(eventDebuggerModule);
        addModule(structureDebuggerModule);
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
