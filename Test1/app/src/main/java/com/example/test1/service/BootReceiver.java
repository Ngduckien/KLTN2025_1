package com.unetikhoaluan2025.timabk.service;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.room.Room;

import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.notifications.NotificationScheduler;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Thiết bị đã khởi động, đặt lại các thông báo");

            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                // Truy cập database trực tiếp
                // Giả sử tên database là "app_database", thay thế nếu cần
                AppDatabase db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "app_database").build();

                // Truy vấn tất cả kế hoạch cần thông báo
                List<LapKeHoach> keHoachList = db.lapKeHoachDAO().getAllKeHoachWithNotificationsDirectly();

                handler.post(() -> {
                    // Đặt lại tất cả thông báo
                    NotificationScheduler.setupAllNotifications(context, keHoachList);
                });
                NotificationScheduler.setupAllNotifications(context, keHoachList);
                Intent serviceIntent = new Intent(context, UsageMonitorService.class);
                context.startService(serviceIntent);
            });
        }

    }
}
