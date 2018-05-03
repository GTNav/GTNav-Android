package com.spybug.gtnav.interfaces;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

import java.util.List;

//Base DAO for interfacing with the Room database
public interface BaseDao<T> {

    /**
     * Insert an object in the database.
     *
     * @param obj the object to be inserted.
     */
    @Insert
    void insert(T obj);

    /**
     * Insert an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     */
    @Insert
    void insertAll(T... obj);

    /**
     * Insert a List of objects in the database.
     *
     * @param obj the list of objects to be inserted.
     */
    @Insert
    void insertAll(List<T> obj);

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    void update(T obj);

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Delete
    void delete(T obj);
}
