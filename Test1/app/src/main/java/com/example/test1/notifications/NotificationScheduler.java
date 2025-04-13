package com.unetikhoaluan2025.timabk.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";
    private static final int MAX_SCHEDULE_ATTEMPTS = 5;

    // Các loại lặp lại
    public static final String LAP_KHONG = "KHONG";
    public static final String LAP_HANG_NGAY = "HANG_NGAY";
    public static final String LAP_THU_2 = "THU_2";
    public static final String LAP_THU_3 = "THU_3";
    public static final String LAP_THU_4 = "THU_4";
    public static final String LAP_THU_5 = "THU_5";
    public static final String LAP_THU_6 = "THU_6";
    public static final String LAP_THU_7 = "THU_7";
    public static final String LAP_CHU_NHAT = "CHU_NHAT";

    public static void scheduleKeHoachNotification(Context context, LapKeHoach keHoach) {
        if (!validateInput(context, keHoach)) return;

        Calendar calendar = createValidNotificationCalendar(keHoach);
        if (calendar == null) return;

        setupAlarm(context, keHoach, calendar);
    }

    private static boolean validateInput(Context context, LapKeHoach keHoach) {
        if (context == null || keHoach == null) {
            Log.e(TAG, "Context hoặc kế hoạch null");
            return false;
        }

        if (TextUtils.isEmpty(keHoach.getThoiGianNhacNho()) || keHoach.getNgayNhacNho() <= 0) {
            Log.d(TAG, "Thông tin nhắc nhở không hợp lệ cho ID: " + keHoach.getId());
            return false;
        }

        return true;
    }

    private static Calendar createValidNotificationCalendar(LapKeHoach keHoach) {
        try {
            String[] parts = keHoach.getThoiGianNhacNho().split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new IllegalArgumentException("Giờ/phút không hợp lệ");
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(keHoach.getNgayNhacNho());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return adjustCalendarIfNeeded(calendar, keHoach);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tạo lịch nhắc nhở: " + e.getMessage());
            return null;
        }
    }

    private static Calendar adjustCalendarIfNeeded(Calendar calendar, LapKeHoach keHoach) {
        long now = System.currentTimeMillis();
        int attempts = 0;

        while (calendar.getTimeInMillis() <= now && attempts++ < MAX_SCHEDULE_ATTEMPTS) {
            if (LAP_KHONG.equals(keHoach.getLapLai())) {
                Log.d(TAG, "Không lặp lại thông báo đã qua");
                return null;
            }
            adjustTimeBasedOnRepeatType(calendar, keHoach.getLapLai());
        }

        if (keHoach.getNgayKetThucLap() != null &&
                calendar.getTimeInMillis() > keHoach.getNgayKetThucLap()) {
            Log.d(TAG, "Đã qua ngày kết thúc");
            return null;
        }

        return calendar;
    }

    private static void adjustTimeBasedOnRepeatType(Calendar calendar, String lapLai) {
        int targetDayOfWeek = getTargetDayOfWeek(lapLai);
        if (targetDayOfWeek == -1) return;

        Calendar now = Calendar.getInstance();

        if (targetDayOfWeek == 0) { // Lặp hàng ngày
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return;
        }

        // Tính số ngày cần thêm để đến ngày target
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;
        daysToAdd = daysToAdd == 0 ? 7 : daysToAdd; // Nếu cùng ngày thì chuyển sang tuần sau

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
    }

    private static int getTargetDayOfWeek(String lapLai) {
        switch (lapLai) {
            case LAP_HANG_NGAY: return 0;
            case LAP_THU_2: return Calendar.MONDAY;
            case LAP_THU_3: return Calendar.TUESDAY;
            case LAP_THU_4: return Calendar.WEDNESDAY;
            case LAP_THU_5: return Calendar.THURSDAY;
            case LAP_THU_6: return Calendar.FRIDAY;
            case LAP_THU_7: return Calendar.SATURDAY;
            case LAP_CHU_NHAT: return Calendar.SUNDAY;
            default: return -1;
        }
    }

    private static void setupAlarm(Context context, LapKeHoach keHoach, Calendar calendar) {
        String action = "NOTIFY_" + keHoach.getId() + "_" + calendar.getTimeInMillis();
        Intent intent = new Intent(context, NotificationReceiver.class)
                .setAction(action)
                .putExtras(createNotificationExtras(keHoach, calendar));

        int requestCode = generateRequestCode(keHoach.getId(), calendar.getTimeInMillis());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        setExactAlarm(context, calendar.getTimeInMillis(), pendingIntent);
        logScheduledNotification(keHoach, calendar);
    }

    private static Bundle createNotificationExtras(LapKeHoach keHoach, Calendar calendar) {
        Bundle extras = new Bundle();
        extras.putLong("keHoachId", keHoach.getId());
        extras.putString("title", keHoach.getTenKeHoach());
        extras.putString("message", !TextUtils.isEmpty(keHoach.getMoTa())
                ? keHoach.getMoTa()
                : "Nhắc nhở kế hoạch");
        extras.putString("lapLai", keHoach.getLapLai());
        extras.putString("thoiGianNhacNho", keHoach.getThoiGianNhacNho());
        extras.putLong("nextNotifyTime", calendar.getTimeInMillis());
        if (keHoach.getNgayKetThucLap() != null) {
            extras.putLong("ngayKetThucLap", keHoach.getNgayKetThucLap());
        }
        return extras;
    }

    private static int generateRequestCode(long keHoachId, long timeMillis) {
        return (int) ((keHoachId + timeMillis) % Integer.MAX_VALUE);
    }

    private static void setExactAlarm(Context context, long triggerAtMillis, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager không khả dụng");
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Thiếu quyền SCHEDULE_EXACT_ALARM");
                context.startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Lỗi quyền khi đặt báo thức", e);
        }
    }

    public static void rescheduleKeHoachNotification(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null) return;

        Bundle extras = intent.getExtras();
        int keHoachId = (int) extras.getLong("keHoachId", -1);
        String lapLai = extras.getString("lapLai", LAP_KHONG);
        long nextTime = extras.getLong("nextNotifyTime", 0);

        if (keHoachId == -1 || LAP_KHONG.equals(lapLai) || nextTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Không đủ điều kiện lập lịch lại");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nextTime);

        // Lấy dữ liệu từ database
        LapKeHoach keHoach = AppDatabase.getInstance(context).lapKeHoachDAO().getKeHoachById(keHoachId).getValue();
        if (keHoach == null) {
            Log.e(TAG, "Không tìm thấy kế hoạch ID: " + keHoachId);
            return;
        }

        setupAlarm(context, keHoach, calendar);
    }

    public static void cancelKeHoachNotification(Context context, int keHoachId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Hủy tất cả PendingIntent liên quan đến keHoachId
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                keHoachId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Đã hủy thông báo cho kế hoạch ID: " + keHoachId);
        }
    }

    public static void setupAllNotifications(Context context, List<LapKeHoach> keHoachList) {
        if (context == null || keHoachList == null) return;

        for (LapKeHoach keHoach : keHoachList) {
            if (!keHoach.isCompleted()) {
                scheduleKeHoachNotification(context, keHoach);
            }
        }
        Log.d(TAG, "Đã đặt lại " + keHoachList.size() + " thông báo");
    }

    private static void logScheduledNotification(LapKeHoach keHoach, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Log.i(TAG, String.format(Locale.US,
                "Đã lập lịch [ID:%d, Tên:%s, Thời gian:%s, Lặp lại:%s]",
                keHoach.getId(),
                keHoach.getTenKeHoach(),
                sdf.format(calendar.getTime()),
                keHoach.getLapLai()));
    }
}