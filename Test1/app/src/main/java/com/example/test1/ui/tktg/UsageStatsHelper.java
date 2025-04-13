package com.unetikhoaluan2025.timabk.ui.tktg;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;

public class UsageStatsHelper {

    public static List<UsageRecord> getUsageStatsForToday(Context context) {
        List<UsageRecord> usageRecords = new ArrayList<>();
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null || !hasUsageStatsPermission(context)) return usageRecords;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        Map<String, UsageStats> statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
        if (statsMap == null || statsMap.isEmpty()) return usageRecords;

        PackageManager packageManager = context.getPackageManager();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(calendar.getTime());

        for (Map.Entry<String, UsageStats> entry : statsMap.entrySet()) {
            UsageStats stats = entry.getValue();
            if (stats.getTotalTimeInForeground() > 0) {
                String appName = getAppName(packageManager, stats.getPackageName());
                long durationInSeconds = stats.getTotalTimeInForeground() / 1000;
                usageRecords.add(new UsageRecord(appName, stats.getFirstTimeStamp(), stats.getLastTimeStamp(), durationInSeconds, todayDate));
            }
        }
        return usageRecords;
    }

    public static List<UsageRecord> getUsageStatsByDateRange(Context context, long startTime, long endTime) {
        List<UsageRecord> usageRecords = new ArrayList<>();
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null || !hasUsageStatsPermission(context)) return usageRecords;

        Map<String, UsageStats> statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
        if (statsMap == null || statsMap.isEmpty()) return usageRecords;

        PackageManager packageManager = context.getPackageManager();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Map.Entry<String, UsageStats> entry : statsMap.entrySet()) {
            UsageStats stats = entry.getValue();
            if (stats.getTotalTimeInForeground() > 0) {
                String appName = getAppName(packageManager, stats.getPackageName());
                long durationInSeconds = stats.getTotalTimeInForeground() / 1000;
                String dateUsed = sdf.format(new Date(stats.getLastTimeStamp()));
                usageRecords.add(new UsageRecord(appName, stats.getFirstTimeStamp(), stats.getLastTimeStamp(), durationInSeconds, dateUsed));
            }
        }
        return usageRecords;
    }

    private static String getAppName(PackageManager packageManager, String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static void requestUsageStatsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static long getAppUsageTime(Context context, String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null || !hasUsageStatsPermission(context)) {
            Log.e("UsageStatsHelper", "UsageStatsManager null hoặc chưa được cấp quyền!");
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        Map<String, UsageStats> statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
        if (statsMap != null) {
            UsageStats stats = statsMap.get(packageName);
            if (stats != null) {
                return stats.getTotalTimeInForeground() / (1000 * 60);
            }
        }

        List<UsageStats> statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        if (statsList == null || statsList.isEmpty()) {
            Log.e("UsageStatsHelper", "Không có dữ liệu UsageStats!");
            return 0;
        }

        long totalTime = 0;
        for (UsageStats stat : statsList) {
            if (stat.getPackageName().equals(packageName)) {
                totalTime = stat.getTotalTimeInForeground() / (1000 * 60);
                break;
            }
        }
        return totalTime;
    }

}

