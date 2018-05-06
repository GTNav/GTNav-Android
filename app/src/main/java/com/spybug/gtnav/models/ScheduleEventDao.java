package com.spybug.gtnav.models;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.spybug.gtnav.interfaces.BaseDao;

import java.util.List;

@Dao
public abstract class ScheduleEventDao implements BaseDao<ScheduleEvent> {

    /**
     * Get all events.
     */
    @Query("SELECT * FROM SCHEDULE_EVENT ORDER BY time")
    public abstract List<ScheduleEvent> getAll();

    /**
     * Delete all events.
     */
    @Query("DELETE FROM SCHEDULE_EVENT")
    public abstract void deleteAll();

    /**
     * Delete events of same repeated event groupId
     */
    @Query("DELETE FROM SCHEDULE_EVENT WHERE group_id = :groupId")
    public abstract void deleteEventGroup(int groupId);
}
