package com.unetikhoaluan2025.timabk.ui.tktg;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UsageStatsHelper {

    private static final String TAG = "UsageStatsHelper";

    public static List<UsageRecord> getUsageStatsByDateRange(Context context, long startTime, long endTime) {
        List<UsageRecord> records = new ArrayList<>();
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null || !hasUsageStatsPermission(context)) return records;

        // Sử dụng queryEvents để lấy từng sự kiện sử dụng
        UsageEvents events = usm.queryEvents(startTime, endTime);
        PackageManager pm = context.getPackageManager();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Map để lưu trữ các phiên theo package name
        Map<String, List<Session>> sessionsMap = new HashMap<>();
        Map<String, String> appNames = new HashMap<>();

        // Duyệt qua các sự kiện
        while (events.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            events.getNextEvent(event);

            String packageName = event.getPackageName();
            if (packageName == null) continue;

            // Lấy tên ứng dụng nếu chưa có
            if (!appNames.containsKey(packageName)) {
                appNames.put(packageName, getAppName(pm, packageName));
            }

            // Tạo session mới cho mỗi lần vào foreground
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                Session session = new Session(
                        new Date(event.getTimeStamp()),
                        null, // Chưa biết thời điểm kết thúc
                        0
                );

                if (!sessionsMap.containsKey(packageName)) {
                    sessionsMap.put(packageName, new ArrayList<>());
                }
                sessionsMap.get(packageName).add(session);
            }
            // Cập nhật thời gian kết thúc khi ứng dụng chuyển sang background
            else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                List<Session> sessions = sessionsMap.get(packageName);
                if (sessions != null && !sessions.isEmpty()) {
                    Session lastSession = sessions.get(sessions.size() - 1);
                    if (lastSession.getEndTime() == null) {
                        lastSession.setEndTime(new Date(event.getTimeStamp()));
                        lastSession.setDuration((event.getTimeStamp() - lastSession.getStartTime().getTime()) / 1000);
                    }
                }
            }
        }

        // Tạo các UsageRecord từ các session
        for (Map.Entry<String, List<Session>> entry : sessionsMap.entrySet()) {
            String packageName = entry.getKey();
            List<Session> sessions = entry.getValue();

            // Lọc bỏ các session không có thời gian kết thúc
            List<Session> validSessions = new ArrayList<>();
            long totalDuration = 0;
            for (Session session : sessions) {
                if (session.getEndTime() != null && session.getDuration() > 0) {
                    validSessions.add(session);
                    totalDuration += session.getDuration();
                }
            }

            if (!validSessions.isEmpty()) {
                UsageRecord record = new UsageRecord(
                        appNames.get(packageName),
                        validSessions.get(0).getStartTime().getTime(),
                        validSessions.get(validSessions.size()-1).getEndTime().getTime(),
                        totalDuration,
                        sdf.format(new Date(startTime))
                );
                record.setSessions(validSessions);
                records.add(record);
            }
        }

        return records;
    }
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
