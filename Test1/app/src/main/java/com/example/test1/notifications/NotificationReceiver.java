package com.unetikhoaluan2025.timabk.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "KeHoachChannel";
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Lấy thông tin từ intent
        int keHoachId = intent.getIntExtra("keHoachId", -1);
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (keHoachId == -1 || title == null) {
            Log.e(TAG, "Thiếu thông tin keHoachId hoặc tiêu đề");
            return;
        }

        // Tạo thông báo
        createNotification(context, keHoachId, title, message);

        // Đặt lại thông báo nếu có lặp lại
        String lapLai = intent.getStringExtra("lapLai");
        if (lapLai != null && !lapLai.isEmpty()) {
            NotificationScheduler.rescheduleKeHoachNotification(context, intent);
        }
    }

    private void createNotification(Context context, int keHoachId, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Kế Hoạch",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo về kế hoạch");
            notificationManager.createNotificationChannel(channel);
        }

        // Intent khi nhấn vào thông báo
        Intent mainIntent = new Intent(context, LapKeHoach.class);
        mainIntent.putExtra("keHoachId", keHoachId);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                keHoachId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Thay thế bằng icon của bạn
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Hiển thị thông báo
        notificationManager.notify(keHoachId, builder.build());

        Log.d(TAG, "Đã hiển thị thông báo cho kế hoạch ID: " + keHoachId);
    }
}