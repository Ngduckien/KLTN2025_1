package com.unetikhoaluan2025.timabk.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class UsageMonitorService extends Service {

    private static final String CHANNEL_ID = "AppUsageMonitorChannel";
    private static final String WARNING_CHANNEL_LOW = "warning_low";
    private static final String WARNING_CHANNEL_MEDIUM = "warning_medium";
    private static final String WARNING_CHANNEL_HIGH = "warning_high";

    // Các ngưỡng cảnh báo (30p, 60p, 180p)
    private static final long WARNING_THRESHOLD_1 = TimeUnit.MINUTES.toMillis(3);
    private static final long WARNING_THRESHOLD_2 = TimeUnit.MINUTES.toMillis(60);
    private static final long WARNING_THRESHOLD_3 = TimeUnit.MINUTES.toMillis(180);

    private static final long CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(1); // Kiểm tra mỗi 1 phút

    private Handler handler;
    private Runnable usageChecker;
    private NotificationManager notificationManager;
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        packageManager = getPackageManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        }

        createNotificationChannels();
        startForeground(1, createNotification());
        Log.d("UsageMonitorService", "Service đang chạy...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        initUsageChecker();
    }

    private void initUsageChecker() {
        handler = new Handler(Looper.getMainLooper());
        usageChecker = new Runnable() {
            @Override
            public void run() {
                checkAppUsage();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(usageChecker);
    }

    private void checkAppUsage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkAppUsageModern();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkAppUsageModern() {
        long now = System.currentTimeMillis();
        long start = now - TimeUnit.HOURS.toMillis(4); // hoặc từ 0h hôm nay

        UsageEvents events = usageStatsManager.queryEvents(start, now);

        String currentForegroundApp = null;
        long sessionStartTime = 0;

        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                currentForegroundApp = event.getPackageName();
                sessionStartTime = event.getTimeStamp();
            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND &&
                    event.getPackageName().equals(currentForegroundApp)) {
                // Đã đóng app → reset lại
                currentForegroundApp = null;
                sessionStartTime = 0;
            }
        }

        // Sau khi duyệt hết, nếu vẫn còn app đang foreground → tức là app vẫn đang mở
        if (currentForegroundApp != null && sessionStartTime > 0) {
            long duration = now - sessionStartTime;

            if (duration > WARNING_THRESHOLD_3) {
                showWarning(currentForegroundApp, duration, 3);
            } else if (duration > WARNING_THRESHOLD_2) {
                showWarning(currentForegroundApp, duration, 2);
            } else if (duration > WARNING_THRESHOLD_1) {
                showWarning(currentForegroundApp, duration, 1);
            }
        }
    }

    private void showWarning(String packageName, long usageTime, int warningLevel) {
        String appName = getAppName(packageName);
        long hours = TimeUnit.MILLISECONDS.toHours(usageTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(usageTime) % 60;

        String timeText;
        if (hours > 0) {
            timeText = String.format("%d giờ %d phút", hours, minutes);
        } else {
            timeText = String.format("%d phút", minutes);
        }

        String channelId;
        String title;
        int icon;
        int color;
        int priority;

        switch (warningLevel) {
            case 1:
                channelId = WARNING_CHANNEL_LOW;
                title = "Lưu ý sử dụng ứng dụng";
                icon = android.R.drawable.ic_dialog_info;
                color = android.R.color.holo_blue_light;
                priority = NotificationCompat.PRIORITY_DEFAULT;
                break;
            case 2:
                channelId = WARNING_CHANNEL_MEDIUM;
                title = "Cảnh báo sử dụng nhiều";
                icon = android.R.drawable.ic_dialog_alert;
                color = android.R.color.holo_orange_dark;
                priority = NotificationCompat.PRIORITY_HIGH;
                break;
            case 3:
                channelId = WARNING_CHANNEL_HIGH;
                title = "CẢNH BÁO SỬ DỤNG QUÁ MỨC";
                icon = android.R.drawable.stat_notify_error;
                color = android.R.color.holo_red_dark;
                priority = NotificationCompat.PRIORITY_MAX;
                break;
            default:
                channelId = WARNING_CHANNEL_LOW;
                title = "Thông báo sử dụng";
                icon = android.R.drawable.ic_dialog_info;
                color = android.R.color.holo_blue_light;
                priority = NotificationCompat.PRIORITY_DEFAULT;
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(String.format("%s đã dùng %s", appName, timeText))
                .setSmallIcon(icon)
                .setColor(getResources().getColor(color))
                .setPriority(priority)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(packageName.hashCode() + warningLevel, notification);
    }

    private String getAppName(String packageName) {
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kênh chính cho foreground service
            NotificationChannel mainChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Giám sát ứng dụng",
                    NotificationManager.IMPORTANCE_LOW
            );

            // Kênh cho cảnh báo nhẹ (30-60 phút)
            NotificationChannel lowChannel = new NotificationChannel(
                    WARNING_CHANNEL_LOW,
                    "Cảnh báo nhẹ",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            // Kênh cho cảnh báo trung bình (60-180 phút)
            NotificationChannel mediumChannel = new NotificationChannel(
                    WARNING_CHANNEL_MEDIUM,
                    "Cảnh báo trung bình",
                    NotificationManager.IMPORTANCE_LOW
            );

            // Kênh cho cảnh báo cao (trên 180 phút)
            NotificationChannel highChannel = new NotificationChannel(
                    WARNING_CHANNEL_HIGH,
                    "Cảnh báo nghiêm trọng",
                    NotificationManager.IMPORTANCE_HIGH
            );
            highChannel.enableVibration(true);
            highChannel.setVibrationPattern(new long[]{0, 500, 200, 500});

            notificationManager.createNotificationChannels(
                    Arrays.asList(mainChannel, lowChannel, mediumChannel, highChannel)
            );
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Giám sát ứng dụng")
                .setContentText("Đang theo dõi thời gian sử dụng ứng dụng...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(usageChecker);
        }
        Log.d("UsageMonitorService", "Service bị dừng!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent restartServiceIntent = new Intent(getApplicationContext(), UsageMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartServiceIntent);
        } else {
            startService(restartServiceIntent);
        }
    }
}