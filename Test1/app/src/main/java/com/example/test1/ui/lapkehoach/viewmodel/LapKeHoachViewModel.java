package com.unetikhoaluan2025.timabk.ui.lapkehoach.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.notifications.NotificationScheduler;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoachDAO;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LapKeHoachViewModel extends AndroidViewModel {
    // Trường dữ liệu
    private final LapKeHoachDAO lapKeHoachDAO;
    private final ExecutorService executorService;
    private final MutableLiveData<Boolean> operationStatus = new MutableLiveData<>();
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> keHoachCount = new MutableLiveData<>();
    private LapKeHoachViewModel repository;

    public LapKeHoachViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        lapKeHoachDAO = database.lapKeHoachDAO();
        executorService = Executors.newFixedThreadPool(4);
    }


    // ===== CÁC THAO TÁC TRUY CẬP DỮ LIỆU =====

    public void fetchKeHoachCount(String userId) {
        executorService.execute(() -> {
            int count = lapKeHoachDAO.getKeHoachCountByUserId(userId);
            keHoachCount.postValue(count);
        });
    }
    public LiveData<Integer> getKeHoachCount() {
        return keHoachCount;
    }
    // Thêm kế hoạch mới
    public void insertKeHoach(LapKeHoach lapKeHoach) {

        executorService.execute(() -> {
            long id = lapKeHoachDAO.insertKeHoach(lapKeHoach);
            operationStatus.postValue(id > 0);
            if (id > 0) {
                operationMessage.postValue("Kế hoạch đã được thêm thành công");
            } else {
                operationMessage.postValue("Không thể thêm kế hoạch");
            }
        });
        if (lapKeHoach.getThoiGianNhacNho() != null && lapKeHoach.getNgayNhacNho() != null) {
            NotificationScheduler.scheduleKeHoachNotification(getApplication(), lapKeHoach);
        }
    }

    // Cập nhật kế hoạch
    public void updateKeHoach(LapKeHoach lapKeHoach) {
        executorService.execute(() -> {
            int rowsUpdated = lapKeHoachDAO.updateKeHoach(lapKeHoach);
            boolean success = rowsUpdated > 0;
            operationStatus.postValue(success);
            if (success) {
                operationMessage.postValue("Kế hoạch đã được cập nhật");
            } else {
                operationMessage.postValue("Không thể cập nhật kế hoạch");
            }
        });
        NotificationScheduler.cancelKeHoachNotification(getApplication(), lapKeHoach.getId());

        if (!lapKeHoach.isCompleted() && lapKeHoach.getThoiGianNhacNho() != null && lapKeHoach.getNgayNhacNho() != null) {
            NotificationScheduler.scheduleKeHoachNotification(getApplication(), lapKeHoach);
        }
    }

    // Xóa kế hoạch
    public void deleteKeHoach(LapKeHoach lapKeHoach) {
        NotificationScheduler.cancelKeHoachNotification(getApplication(), lapKeHoach.getId());
        executorService.execute(() -> {
            int rowsDeleted = lapKeHoachDAO.deleteKeHoach(lapKeHoach);
            boolean success = rowsDeleted > 0;
            operationStatus.postValue(success);
            if (success) {
                operationMessage.postValue("Kế hoạch đã được xóa");
            } else {
                operationMessage.postValue("Không thể xóa kế hoạch");
            }
        });
    }
    // Đánh dấu kế hoạch là hoàn thành
    public void completeKeHoach(LapKeHoach keHoach) {
        keHoach.setCompleted(true);

        // Hủy thông báo khi đánh dấu hoàn thành
        NotificationScheduler.cancelKeHoachNotification(getApplication(), keHoach.getId());

        // Cập nhật vào database
        updateKeHoach(keHoach);
    }

    // Phương thức để khôi phục tất cả thông báo (gọi khi ứng dụng khởi động)
    public void restoreAllNotifications() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<LapKeHoach> keHoachList = lapKeHoachDAO.getAllKeHoachWithNotificationsDirectly();

            handler.post(() -> {
                NotificationScheduler.setupAllNotifications(getApplication(), keHoachList);
            });
        });
    }

    // Đánh dấu kế hoạch là đã hoàn thành
    public void markKeHoachAsCompleted(int keHoachId) {
        executorService.execute(() -> {
            int rowsUpdated = lapKeHoachDAO.markKeHoachAsCompleted(keHoachId);
            boolean success = rowsUpdated > 0;
            operationStatus.postValue(success);
            if (success) {
                operationMessage.postValue("Kế hoạch đã được đánh dấu hoàn thành");
            } else {
                operationMessage.postValue("Không thể cập nhật trạng thái kế hoạch");
            }
        });
    }

    // Đánh dấu kế hoạch là chưa hoàn thành
    public void markKeHoachAsIncomplete(int keHoachId) {
        executorService.execute(() -> {
            int rowsUpdated = lapKeHoachDAO.markKeHoachAsIncomplete(keHoachId);
            boolean success = rowsUpdated > 0;
            operationStatus.postValue(success);
            if (success) {
                operationMessage.postValue("Kế hoạch đã được đánh dấu chưa hoàn thành");
            } else {
                operationMessage.postValue("Không thể cập nhật trạng thái kế hoạch");
            }
        });
    }

    // Lấy kế hoạch theo ID
    public LiveData<LapKeHoach> getKeHoachById(int keHoachId) {
        return lapKeHoachDAO.getKeHoachById(keHoachId);
    }

    // Lấy tất cả kế hoạch của người dùng
    public LiveData<List<LapKeHoach>> getAllKeHoachByUserId(String userId) {
        return lapKeHoachDAO.getAllKeHoachByUserId(userId);
    }

    // Lấy kế hoạch đang hoạt động
    public LiveData<List<LapKeHoach>> getActiveKeHoachByUserId(String userId) {
        return lapKeHoachDAO.getActiveKeHoachByUserId(userId);
    }

    // Lấy kế hoạch đã hoàn thành
    public LiveData<List<LapKeHoach>> getCompletedKeHoachByUserId(String userId) {
        return lapKeHoachDAO.getCompletedKeHoachByUserId(userId);
    }

    // Tìm kiếm kế hoạch theo tên
    public LiveData<List<LapKeHoach>> searchKeHoachByName(String userId, String keyword) {
        return lapKeHoachDAO.searchKeHoachByName(userId, keyword);
    }

    // Xóa tất cả kế hoạch đã hoàn thành
    public void deleteAllCompletedKeHoach(String userId) {
        executorService.execute(() -> {
            int rowsDeleted = lapKeHoachDAO.deleteAllCompletedKeHoachByUserId(userId);
            boolean success = rowsDeleted > 0;
            operationStatus.postValue(success);
            if (success) {
                operationMessage.postValue("Đã xóa tất cả kế hoạch đã hoàn thành");
            } else {
                operationMessage.postValue("Không có kế hoạch nào được xóa");
            }
        });
    }

    // Lấy danh sách kế hoạch có nhắc nhở trong ngày
    public LiveData<List<LapKeHoach>> getKeHoachWithReminderByDate(String userId, long reminderDate) {
        return lapKeHoachDAO.getKeHoachWithReminderByDate(userId, reminderDate);
    }

    // ===== TRẠNG THÁI THAO TÁC =====

    // Trạng thái thao tác
    public LiveData<Boolean> getOperationStatus() {
        return operationStatus;
    }

    // Thông báo thao tác
    public LiveData<String> getOperationMessage() {
        return operationMessage;
    }

    // Dọn dẹp tài nguyên khi ViewModel bị hủy
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    public void toggleKeHoachStatus(int keHoachId) {
        executorService.execute(() -> {
            LapKeHoach keHoach = lapKeHoachDAO.getKeHoachById(keHoachId).getValue(); // Dùng hàm mới
            if (keHoach != null) {
                boolean isCompleted = keHoach.isCompleted();
                int result = isCompleted
                        ? lapKeHoachDAO.markKeHoachAsIncomplete(keHoachId)
                        : lapKeHoachDAO.markKeHoachAsCompleted(keHoachId);

                boolean success = result > 0;
                operationStatus.postValue(success);
                operationMessage.postValue(
                        success ? (isCompleted ? "Kế hoạch đã được đánh dấu chưa hoàn thành" : "Kế hoạch đã được đánh dấu hoàn thành")
                                : "Không thể cập nhật trạng thái kế hoạch"
                );
            } else {
                operationStatus.postValue(false);
                operationMessage.postValue("Không tìm thấy kế hoạch");
            }
        });
    }

}