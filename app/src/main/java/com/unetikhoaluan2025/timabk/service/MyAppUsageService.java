package com.unetikhoaluan2025.timabk.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.NotificationCompat;

public class MyAppUsageService extends AccessibilityService {

    private String currentPackage = "";
    private long startTime = 0;
    private Handler handler = new Handler();
    private Runnable checkUsageRunnable;

    private boolean notified15 = false;
    private boolean notified60 = false;
    private boolean notified180 = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String newPackage = event.getPackageName() != null ? event.getPackageName().toString() : "";
            if (!newPackage.equals(currentPackage)) {
                currentPackage = newPackage;
                startTime = System.currentTimeMillis();

                // Reset cờ thông báo
                notified15 = false;
                notified60 = false;
                notified180 = false;

                // Bắt đầu theo dõi
                startCheckingUsage();
            }
        }
    }

    private void startCheckingUsage() {
        if (checkUsageRunnable != null) {
            handler.removeCallbacks(checkUsageRunnable);
        }

        checkUsageRunnable = new Runnable() {
            @Override
            public void run() {
                long duration = System.currentTimeMillis() - startTime;
                long minutes = duration / (60 * 1000);

                if (!notified15 && minutes >= 15) {
                    showNotification("Bạn đã dùng liên tục 15 phút", "Hãy cân nhắc nghỉ ngơi một chút.");
                    notified15 = true;
                }

                if (!notified60 && minutes >= 60) {
                    showNotification("Bạn đã dùng hơn 60 phút", "Đã đến lúc nghỉ ngơi rồi!");
                    notified60 = true;
                }

                if (!notified180 && minutes >= 180) {
                    showNotification("Bạn đã dùng quá 180 phút", "Hãy tạm dừng – không nên dùng quá lâu.");
                    notified180 = true;
                }

                // Gọi lại sau 1 phút
                handler.postDelayed(this, 60 * 1000);
            }
        };

        handler.post(checkUsageRunnable);
    }

    private void showNotification(String title, String message) {
        String channelId = "app_usage_warning";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo kênh thông báo nếu Android >= 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Cảnh báo sử dụng", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo cảnh báo khi sử dụng ứng dụng quá lâu");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        setServiceInfo(info);
        Log.d("MyAppUsageService", "Service connected");
    }

    @Override
    public void onInterrupt() {
        // Không cần xử lý gì khi bị gián đoạn
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyAppUsageService", "Service created");
    }


}

