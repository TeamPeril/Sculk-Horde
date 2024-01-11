package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.infection.CursorEntity;
import com.github.sculkhorde.core.ModConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class CursorHandler {

    //private HashMap<UUID, CursorEntity> cursors = new HashMap<UUID, CursorEntity>();
    SortedCursorList cursors = new SortedCursorList();
    private int index = 0;

    private int tickDelay = 3;

    private boolean manualControlOfTickingEnabled = false;

    public void setManualControlOfTickingEnabled(boolean value) { manualControlOfTickingEnabled = value; }
    public boolean isManualControlOfTickingEnabled() { return manualControlOfTickingEnabled; }

    public void addCursor(CursorEntity entity)
    {
        cursors.insertCursor(entity);
    }
    public void computeIfAbsent(CursorEntity entity)
    {
        if(cursors.getIndexOfCursor(entity).isEmpty())
        {
            addCursor(entity);
        }
    }

    public int getSizeOfCursorList()
    {
        return cursors.list.size();
    }



    public void tickCursors()
    {
        ArrayList<CursorEntity> listOfCursors = cursors.getList();

        for(int i = 0; i < ModConfig.SERVER.cursors_to_tick_per_tick.get(); i++)
        {
            if(index >= listOfCursors.size())
            {
                index = 0;
                continue;
            }

            CursorEntity cursorAtIndex = listOfCursors.get(index);

            cursorAtIndex.chanceToThanosSnapThisCursor();

            if(cursorAtIndex.canBeManuallyTicked())
            {
                cursorAtIndex.cursorTick();
                index++;
            }
        }
    }

    public void serverTick()
    {
        if(tickDelay < ModConfig.SERVER.delay_between_cursor_tick_interval.get())
        {
            tickDelay++;
            return;
        }

        tickDelay = 0;
        cursors.clean();
        int cursorPopulationAmount = getSizeOfCursorList();
        int cursorPopulationThreshold = ModConfig.SERVER.cursors_threshold_for_activation.get();

        if(cursorPopulationAmount >= cursorPopulationThreshold)
        {
            setManualControlOfTickingEnabled(true);
            tickCursors();
            return;
        }

        setManualControlOfTickingEnabled(false);
    }

    public class SortedCursorList
    {
        private ArrayList<CursorEntity> list;

        public SortedCursorList()
        {
            list = new ArrayList<>();
        }

        public ArrayList<CursorEntity> getList()
        {
            return list;
        }

        public boolean shouldCursorBeDeleted(CursorEntity entity)
        {
            return entity == null || entity.isRemoved();
        }

        public void clean()
        {
            for(int i = 0; i < list.size(); i++)
            {
                if(shouldCursorBeDeleted(list.get(i)))
                {
                    list.remove(i);
                    i--;
                }
            }
        }

        public void insertCursor(CursorEntity entity)
        {
            int positionToInsert = 0;

            for(int index = 0; index < list.size(); index++)
            {
                CursorEntity cursorAtIndex = list.get(index);
                positionToInsert = index;

                if(entity.getUUID().compareTo(cursorAtIndex.getUUID()) >= 0)
                {
                    break;
                }
            }
            list.add(positionToInsert, entity);
        }

        public void removeCursor(CursorEntity entity)
        {
            Optional<Integer> position = getIndexOfCursor(entity);
            if(position.isEmpty())
            {
                return;
            }

            // This is some weird fuck shit
            list.remove(position.get().intValue());
        }

        public Optional<Integer> getIndexOfCursor(CursorEntity entity) {
            UUID uuid = entity.getUUID();

            int left = 0;
            int right = list.size() - 1;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                int res = list.get(mid).getUUID().compareTo(uuid);

                // Check if UUID is present at mid
                if (res == 0)
                    return Optional.of(mid);

                // If UUID greater, ignore left half
                if (res > 0)
                    left = mid + 1;

                    // If UUID is smaller, ignore right half
                else
                    right = mid - 1;
            }

            return Optional.empty();
        }
    }
}
