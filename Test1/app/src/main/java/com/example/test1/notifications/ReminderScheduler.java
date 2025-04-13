package com.unetikhoaluan2025.timabk.notifications;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.ui.gioihan.AppLimit;
import com.unetikhoaluan2025.timabk.ui.gioihan.AppLimitDao;
import com.unetikhoaluan2025.timabk.ui.gioihan.CloseAppReceiver;
import com.unetikhoaluan2025.timabk.ui.tktg.UsageStatsHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderScheduler {
    public static void scheduleReminders(Context context) {
        Log.d("ReminderScheduler", "scheduleReminders() đã được gọi!");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            AppLimitDao dao = db.appLimitDao();
            List<AppLimit> limitedApps = dao.getLimitedAppsDirect();

            if (limitedApps == null || limitedApps.isEmpty()) {
                Log.d("ReminderScheduler", "Không có ứng dụng nào có giới hạn.");
                return;
            }
            if (!UsageStatsHelper.hasUsageStatsPermission(context)) {
                Log.e("ReminderScheduler", "Ứng dụng chưa được cấp quyền Usage Access!");
                return;
            }
            for (AppLimit app : limitedApps) {
                Log.d("ReminderScheduler", "Ứng dụng: " + app.appName + " | Package: " + app.getPackageName());
                long usedTime = UsageStatsHelper.getAppUsageTime(context, app.getPackageName());
                long remainingTime = app.limitTime - usedTime;

                Log.d("ReminderScheduler", "Ứng dụng " + app.getPackageName() + " còn lại: " + remainingTime + " phút "+ usedTime);

            }
        });
    }

    private static void scheduleNotification(Context context, String packageName, long remainingTime) {
        Log.d("ReminderScheduler", "Thông báo: " + packageName + " còn ít " + remainingTime + " phút.");

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("packageName", packageName);
        intent.putExtra("remainingTime", remainingTime);
        intent.putExtra("limitReached", false);  // Chưa hết thời gian

        context.sendBroadcast(intent);
    }

    private static void sendLimitReachedNotification(Context context, String packageName) {
        Log.d("ReminderScheduler", "Ứng dụng " + packageName + " đã hết thời gian!");

        Intent intent = new Intent(context, CloseAppReceiver.class);
        intent.putExtra("packageName", packageName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, packageName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationHelper.showLimitReachedNotification(context, packageName, pendingIntent);
    }
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    public static void isAppLimited(Context context, String packageName, OnCheckLimitListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // lấy giá trị 10p
            SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
            int timeValue = sharedPreferences.getInt("timeKey", 0);

            AppLimitDao appLimitDao = AppDatabase.getInstance(context).appLimitDao();
            AppLimit appLimit = appLimitDao.getAppBypackageName(packageName);

            int limitStatus = 0; // 0: Chưa đến giới hạn, 1: Còn dưới 5 phút, 2: Đã hết giờ
            long remainingTime = 0;
            if (appLimit != null) {
                long usedTime = UsageStatsHelper.getAppUsageTime(context, packageName);
                remainingTime = appLimit.limitTime - usedTime ;
                Log.d("Check thời gian",appLimit.limitTime+" và "+usedTime+" Và "+ remainingTime);

                if (remainingTime > 5)
                {
                    limitStatus = 0;
                }
                else if (remainingTime > 0)
                {
                    limitStatus = 1; // Còn dưới 5 phút
                } else {
                    limitStatus = 2; // Đã hết thời gian
                }


            }
            Log.d("Check LimitStatus", String.valueOf(limitStatus));
            int finalStatus = limitStatus;
            new Handler(Looper.getMainLooper()).post(() -> listener.onChecked(finalStatus));
        });
    }



    public interface OnCheckLimitListener {
        void onChecked(int limitStatus); // Sửa boolean -> int
    }



}