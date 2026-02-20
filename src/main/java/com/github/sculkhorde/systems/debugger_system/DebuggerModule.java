package com.github.sculkhorde.systems.debugger_system;

import com.github.sculkhorde.core.SculkHorde;

import java.util.UUID;

public class DebuggerModule {

    public final UUID uuid = UUID.randomUUID();
    boolean isActive = true;
    boolean debuggingEnabled = false;
    boolean loggingEnabled = true;

    public DebuggerModule()
    {

    }

    public void serverTick()
    {

    }

    public void setActive(boolean active)
    {
        this.isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setLoggingEnabled(boolean value)
    {
        this.loggingEnabled = value;
    }

    public boolean isNonErrorLoggingEnabled()
    {
        return loggingEnabled && isActive() && SculkHorde.isDebugMode();
    }

    public boolean isDebuggingEnabled() {
        return debuggingEnabled && isActive() && SculkHorde.isDebugMode();
    }

    public void setDebuggingEnabled(boolean debuggingEnabled) {
        this.debuggingEnabled = debuggingEnabled;
    }

    public void logDebug(String msg)
    {
        if(isNonErrorLoggingEnabled() && isDebuggingEnabled())
        {
            SculkHorde.LOGGER.debug(getClass().getSimpleName() + " | " + msg);
        }
    }

    public void logInfo(String msg)
    {
        if(isNonErrorLoggingEnabled())
        {
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | " + msg);
        }
    }

    public void logError(String msg)
    {
        SculkHorde.LOGGER.error(getClass().getSimpleName() + " | " + msg);
    }

    public void logWarn(String msg)
    {
        if(isNonErrorLoggingEnabled())
        {
            SculkHorde.LOGGER.warn(getClass().getSimpleName() + " | " + msg);
        }
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof DebuggerModule subDebuggerSystem)
        {
            return this.uuid.equals(subDebuggerSystem.uuid);
        }

        return false;
    }
}
