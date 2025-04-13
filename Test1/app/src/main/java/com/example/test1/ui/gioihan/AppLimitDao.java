package com.unetikhoaluan2025.timabk.ui.gioihan;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppLimitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppLimit appLimit);

    @Update
    void update(AppLimit appLimit);

    @Query("SELECT * FROM app_limits WHERE isSelected = 1")
    LiveData<List<AppLimit>> getLimitedApps();

    @Update
    void updateAppLimit(AppLimit appLimit);

     @Query("DELETE FROM app_limits WHERE appName = :appName")
    void deleteByAppName(String appName);
    @Query("SELECT * FROM app_limits WHERE appName = :appName LIMIT 1")
    AppLimit getAppByName(String appName);
    @Query("SELECT * FROM app_limits WHERE packageName = :packageName LIMIT 1")
    AppLimit getAppBypackageName(String packageName);
    @Query("SELECT * FROM app_limits WHERE packageName IS NOT NULL")
    List<AppLimit> getLimitedAppsDirect();


}
