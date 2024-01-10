package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.infection.CursorEntity;

import java.util.HashMap;
import java.util.UUID;

public class CursorHandler {

    private HashMap<UUID, CursorEntity> cursors = new HashMap<UUID, CursorEntity>();
    private int index = 0;

    private final int DELAY_BETWEEN_TICKS = 3;
    private int tickDelay = DELAY_BETWEEN_TICKS;

    private final int CURSORS_TO_TICK_PER_INTERVAL = 30;

    public void addCursor(CursorEntity entity)
    {
        cursors.put(entity.getUUID(), entity);
    }

    public void removeCursor(CursorEntity entity)
    {
        cursors.remove(entity.getUUID());
    }

    public boolean isCursorInList(CursorEntity entity)
    {
        return cursors.get(entity.getUUID()) != null;
    }

    public void computeIfAbsent(CursorEntity entity)
    {
        if(!isCursorInList(entity))
        {
            addCursor(entity);
        }
        else
        {
            entity.cursorTick();
        }

    }

    public boolean shouldCursorBeDeleted(CursorEntity entity)
    {
        return entity == null || entity.isRemoved();
    }

    public int getSizeOfCursorList()
    {
        return cursors.size();
    }

    public void serverTick()
    {
        if(tickDelay < DELAY_BETWEEN_TICKS)
        {
            tickDelay++;
            return;
        }

        Object[] listOfCursors = cursors.values().toArray();

        tickDelay = 0;

        for(int i = 0; i < CURSORS_TO_TICK_PER_INTERVAL; i++)
        {
            if(index >= listOfCursors.length)
            {
                index = 0;
                continue;
            }

            CursorEntity cursorAtIndex = (CursorEntity) listOfCursors[index];

            if(shouldCursorBeDeleted(cursorAtIndex))
            {
                removeCursor(cursorAtIndex);
            }
            else
            {
                cursorAtIndex.cursorTick();
                index++;
            }
        }
    }

}
