package com.unetikhoaluan2025.timabk.ui.tktg;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsageRecords(List<UsageRecord> records);

    @Query("SELECT * FROM usage_records WHERE date = :date")  // ✅ Sửa "UsageRecord" thành "usage_records"
    LiveData<List<UsageRecord>> getUsageRecordsByDate(String date);

    @Query("DELETE FROM usage_records WHERE date != :date")  // ✅ Sửa "UsageRecord" thành "usage_records"
    void deleteOldRecords(String date);
}
