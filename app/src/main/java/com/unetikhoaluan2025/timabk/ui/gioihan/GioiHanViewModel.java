package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unetikhoaluan2025.timabk.data.AppDatabase;

import java.util.List;

public class GioiHanViewModel extends AndroidViewModel {
    private final AppLimitDao appLimitDao;
    private LiveData<List<AppLimit>> limitedApps; // Chỉ lấy ứng dụng đã đặt giới hạn

    public GioiHanViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        appLimitDao = db.appLimitDao();
        limitedApps = appLimitDao.getLimitedApps(); // Lấy các ứng dụng có isSelected = true
    }

    public LiveData<List<AppLimit>> getLimitedApps() {
        return limitedApps;
    }
    public void removeAppLimit(String appName) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            appLimitDao.deleteByAppName(appName); // Xóa ứng dụng khỏi database
        });
    }
    public void updateAppLimit(String appName, int newLimit) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppLimit app = appLimitDao.getAppByName(appName); // Lấy ứng dụng từ database
            if (app != null) {
                app.setLimitTime(newLimit);
                appLimitDao.updateAppLimit(app); // Cập nhật trong database
            }
        });
    }
    public void fetchLimitedApps() {
        limitedApps = appLimitDao.getLimitedApps();
    }

}
