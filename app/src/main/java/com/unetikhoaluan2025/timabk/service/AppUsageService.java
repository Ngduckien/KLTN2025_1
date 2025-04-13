package com.unetikhoaluan2025.timabk.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.core.app.NotificationCompat;
import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.notifications.ReminderScheduler;
import com.unetikhoaluan2025.timabk.ui.gioihan.BlockScreenActivity;
import com.unetikhoaluan2025.timabk.ui.gioihantimkiem.BlockGKTHActivity;
import com.unetikhoaluan2025.timabk.ui.gioihantimkiem.Key;
import com.unetikhoaluan2025.timabk.ui.gioihantimkiem.KeyDAO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppUsageService extends AccessibilityService {
    private static final String TAG = "AppUsageService";
    private static final String CHANNEL_ID = "app_usage_service";
    private static final int NOTIFICATION_ID = 1;
    private static final long MIN_EVENT_INTERVAL_MS = 1000;
    private static final int SUGGESTION_THRESHOLD = 2;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final ConcurrentHashMap<String, Long> lastEventTimes = new ConcurrentHashMap<>();

    private NotificationManager notificationManager;
    private SharedPreferences blockedAppsPrefs;
    private KeyDAO keyDao;
    private String lastInputText = "";
    private static final String UNLOCK_PREFS = "UnlockPrefs";
    private static final String KEY_IS_UNLOCKED = "is_unlocked";
    private static final String KEY_LAST_BLOCKED = "last_blocked_keyword";
    private static final long UNLOCK_TIMEOUT = 10 * 60 * 1000;

    private SharedPreferences unlockPrefs;
    private BroadcastReceiver unlockReceiver;

    private static final Set<String> SEARCH_APPS = new HashSet<String>() {{
        add("com.google.android.googlequicksearchbox");
        add("com.google.android.youtube");
        add("com.android.browser");
        add("com.android.chrome");
        //add("com.google.android.inputmethod.latin");
    }};

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service initializing...");

        notificationManager = getSystemService(NotificationManager.class);
        blockedAppsPrefs = getSharedPreferences("BlockedApps", MODE_PRIVATE);
        keyDao = AppDatabase.getInstance(this).keyDAO();

        unlockPrefs = getSharedPreferences(UNLOCK_PREFS, MODE_PRIVATE);
        setupUnlockReceiver();
        clearExpiredUnlock();

        createNotificationChannel();
        startForegroundService();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;

        final String packageName = event.getPackageName().toString();
        if (!shouldProcessEvent(packageName)) return;
        Log.d("test package chạy",packageName);
        try {
            executor.execute(() -> {
                if (SEARCH_APPS.contains(packageName)) {
                    processSearchEvent(event);
                }
                processUsageLimit(packageName);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error processing accessibility event", e);
        }
    }

    private boolean shouldProcessEvent(String packageName) {
        long now = System.currentTimeMillis();
        return lastEventTimes.compute(packageName, (k, v) ->
                (v == null || (now - v) >= MIN_EVENT_INTERVAL_MS) ? now : null
        ) != null;
    }

    private void processSearchEvent(AccessibilityEvent event) {
        String currentText = getInputTextFromEvent(event);
        if (currentText == null || currentText.isEmpty()) return;

        boolean isSuggestion = isSuggestionSelected(lastInputText, currentText);
        lastInputText = currentText;

        checkForBlockedKeywords(currentText.trim().toLowerCase(), isSuggestion);

    }

    private String getInputTextFromEvent(AccessibilityEvent event) {
        if (event == null) {
            Log.w(TAG, "Null event received");
            return null;
        }

        if (event.getSource() == null) {
            Log.d(TAG, "Event source is null for event type: " + event.getEventType());
            return null;
        }
        if (event.getSource() == null) return null;

        AccessibilityNodeInfo source = event.getSource();
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
                    event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
                if (source.getText() != null) {
                    return source.getText().toString();
                }
            }

            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                return findTextFromNodeTree(source);
            }
        } finally {
            if (source != null) {  // Thêm kiểm tra null
                source.recycle();
            }
        }
        return null;
    }

    private String findTextFromNodeTree(AccessibilityNodeInfo root) {
        if (root == null) return null;

        if (root.getClassName() != null && root.getClassName().toString().contains("EditText")) {
            if (root.getText() != null) {
                return root.getText().toString();
            }
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                try {
                    String result = findTextFromNodeTree(child);
                    if (result != null) {
                        return result;
                    }
                } finally {
                    child.recycle();
                }
            }
        }
        return null;
    }

    private boolean isSuggestionSelected(String previousText, String currentText) {
        if (previousText == null || previousText.isEmpty()) return false;

        boolean containsAndLonger = currentText.contains(previousText) &&
                currentText.length() - previousText.length() >= SUGGESTION_THRESHOLD;

        boolean isNewText = !currentText.startsWith(previousText) &&
                currentText.length() >= previousText.length();

        return containsAndLonger || isNewText;
    }

    private void checkForBlockedKeywords(String keyword, boolean isSuggestion) {
        if (keyword.length() < 3) return;

        try {
            Key blockedWord = keyDao.getKeywordByWord(keyword);
            if (blockedWord != null) {
                mainHandler.post(() -> handleBlockedContent(blockedWord.getKeyword(), isSuggestion));
            }
        } catch (Exception e) {
            Log.e(TAG, "Database error", e);
        }
    }

    private void setupUnlockReceiver() {
        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_UNLOCK_SUCCESS".equals(intent.getAction())) {
                    handleSuccessfulUnlock();
                }
            }
        };
        registerReceiver(unlockReceiver, new IntentFilter("ACTION_UNLOCK_SUCCESS"));
    }

    private void handleSuccessfulUnlock() {
        unlockPrefs.edit()
                .putBoolean(KEY_IS_UNLOCKED, true)
                .putLong("unlock_time", System.currentTimeMillis())
                .remove(KEY_LAST_BLOCKED)
                .apply();

        // Tự động reset unlock sau 30 phút
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            unlockPrefs.edit().putBoolean(KEY_IS_UNLOCKED, false).apply();
        }, UNLOCK_TIMEOUT);
    }

    private void clearExpiredUnlock() {
        long unlockTime = unlockPrefs.getLong("unlock_time", 0);
        if (System.currentTimeMillis() - unlockTime > UNLOCK_TIMEOUT) {
            unlockPrefs.edit().putBoolean(KEY_IS_UNLOCKED, false).apply();
        }
    }

    private void handleBlockedContent(String keyword, boolean isSuggestion) {
        if (unlockPrefs.getBoolean(KEY_IS_UNLOCKED, false)) {
            Log.d(TAG, "App is unlocked, skipping block");
            return;
        }

        unlockPrefs.edit().putString(KEY_LAST_BLOCKED, keyword).apply();

        try {
            Intent intent = new Intent(this, BlockGKTHActivity.class);
            intent.putExtra("blocked_keyword", keyword);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch block screen", e);
        }
    }

    private void showBlockNotification(String keyword) {
        if (notificationManager == null) return;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_block)
                .setContentTitle("Nội dung bị chặn")
                .setContentText("Từ khóa '" + keyword + "' không được phép")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(keyword.hashCode(), notification);
    }

    private void launchBlockScreen(String keyword) {
        try {
            Intent intent = new Intent(this, BlockGKTHActivity.class);
            intent.putExtra("blocked_keyword", keyword);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch block screen", e);
            performGlobalAction(GLOBAL_ACTION_HOME);
        }
    }

    private void processUsageLimit(String packageName) {

        Log.d(TAG, "Processing usage limit for: " + packageName);
        ReminderScheduler.isAppLimited(this, packageName, new ReminderScheduler.OnCheckLimitListener() {
            @Override
            public void onChecked(int limitStatus) {
                Log.d(TAG, "Limit status for " + packageName + ": " + limitStatus);
                switch (limitStatus) {
                    case 1:
                        sendWarningNotification(packageName);
                        break;
                    case 2:
                        blockApplication(packageName);
                        break;
                }
            }
        });
    }
    private boolean isAppRunning(String packageName) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (process.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void blockApplication(String packageName) {
        Log.d(TAG, "Checking if app is already blocked: " + packageName);
        if (isAppAlreadyBlocked(packageName)) {
            Log.d(TAG, "App is already blocked: " + packageName);
            // Ở đây có vấn đề - nó kết thúc mà không thực hiện hành động nào
            // Xem xét việc tái khởi chạy màn hình chặn ngay cả khi đã bị chặn
            mainHandler.post(() -> launchAppBlockScreen(packageName));
            return;
        }
        setAppBlocked(packageName);
        mainHandler.post(() -> launchAppBlockScreen(packageName));
    }

    private void launchAppBlockScreen(String packageName) {
        Log.d("Test package",packageName);
        try {
            Intent intent = new Intent(this, BlockScreenActivity.class);
            intent.putExtra("packageName", packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch app block screen", e);
            performGlobalAction(GLOBAL_ACTION_HOME);
        }
    }

    private boolean isAppAlreadyBlocked(String packageName) {
        return blockedAppsPrefs.getBoolean(packageName, false);
    }

    private void setAppBlocked(String packageName) {
        blockedAppsPrefs.edit().putBoolean(packageName, true).apply();
    }

    private void sendWarningNotification(String packageName) {
        if (notificationManager == null) return;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Cảnh báo thời gian")
                .setContentText(packageName + " sắp hết thời gian sử dụng")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(packageName.hashCode(), notification);
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Dịch vụ giám sát ứng dụng")
                .setContentText("Đang hoạt động")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Giám sát ứng dụng",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo từ dịch vụ giám sát");
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Service interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Executors.newSingleThreadExecutor();
        if (unlockReceiver != null) {
            unregisterReceiver(unlockReceiver);
        }
        Log.d(TAG, "Service destroyed");
    }
}