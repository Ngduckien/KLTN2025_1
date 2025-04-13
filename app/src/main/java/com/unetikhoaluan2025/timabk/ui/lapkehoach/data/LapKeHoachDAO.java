package com.unetikhoaluan2025.timabk.ui.lapkehoach.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LapKeHoachDAO {
    // Thêm một kế hoạch mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertKeHoach(LapKeHoach lapKeHoach);

    // Cập nhật thông tin kế hoạch
    @Update
    int updateKeHoach(LapKeHoach lapKeHoach);

    // Xóa một kế hoạch
    @Delete
    int deleteKeHoach(LapKeHoach lapKeHoach);

    // Lấy kế hoạch theo ID
    @Query("SELECT * FROM kehoach WHERE id = :keHoachId")
    LiveData<LapKeHoach> getKeHoachById(int keHoachId);

    // Lấy danh sách kế hoạch của một người dùng
    @Query("SELECT * FROM kehoach WHERE userid = :userId ORDER BY ngay_bat_dau ASC")
    LiveData<List<LapKeHoach>> getAllKeHoachByUserId(String userId);

    // Lấy danh sách kế hoạch đang hoạt động (chưa hoàn thành)
    @Query("SELECT * FROM kehoach WHERE userid = :userId AND is_completed = 0 ORDER BY ngay_bat_dau ASC")
    LiveData<List<LapKeHoach>> getActiveKeHoachByUserId(String userId);

    // Lấy danh sách kế hoạch đã hoàn thành
    @Query("SELECT * FROM kehoach WHERE userid = :userId AND is_completed = 1 ORDER BY ngay_bat_dau DESC")
    LiveData<List<LapKeHoach>> getCompletedKeHoachByUserId(String userId);


    // Đánh dấu một kế hoạch là đã hoàn thành
    @Query("UPDATE kehoach SET is_completed = 1 WHERE id = :keHoachId")
    int markKeHoachAsCompleted(int keHoachId);

    // Đánh dấu một kế hoạch là chưa hoàn thành
    @Query("UPDATE kehoach SET is_completed = 0 WHERE id = :keHoachId")
    int markKeHoachAsIncomplete(int keHoachId);

    // Lấy số lượng kế hoạch hiện có của một người dùng
    @Query("SELECT COUNT(*) FROM kehoach WHERE userid = :userId")
    int getKeHoachCountByUserId(String userId);

    // Tìm kiếm kế hoạch theo tên
    @Query("SELECT * FROM kehoach WHERE userid = :userId AND ten_ke_hoach LIKE '%' || :keyword || '%' ORDER BY ngay_bat_dau ASC")
    LiveData<List<LapKeHoach>> searchKeHoachByName(String userId, String keyword);

    // Xóa tất cả kế hoạch đã hoàn thành của một người dùng
    @Query("DELETE FROM kehoach WHERE userid = :userId AND is_completed = 1")
    int deleteAllCompletedKeHoachByUserId(String userId);

    // Lấy danh sách kế hoạch có nhắc nhở trong ngày
    @Query("SELECT * FROM kehoach WHERE userid = :userId AND ngay_nhac_nho = :reminderDate AND is_completed = 0")
    LiveData<List<LapKeHoach>> getKeHoachWithReminderByDate(String userId, long reminderDate);

    @Query("SELECT * FROM kehoach WHERE (ngay_nhac_nho IS NOT NULL AND thoi_gian_nhac_nho IS NOT NULL) AND is_completed = 0")
    List<LapKeHoach> getAllKeHoachWithNotificationsDirectly();
}
