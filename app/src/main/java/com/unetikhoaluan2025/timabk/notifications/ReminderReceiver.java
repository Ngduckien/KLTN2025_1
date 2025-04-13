package com.unetikhoaluan2025.timabk.notifications;


import android.annotation.SuppressLint;
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
import com.unetikhoaluan2025.timabk.ui.gioihan.BlockScreenActivity;
import com.unetikhoaluan2025.timabk.ui.gioihan.CloseAppReceiver;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra("packageName");
        long remainingTime = intent.getLongExtra("remainingTime", -1);
        boolean isLimitReached = intent.getBooleanExtra("limitReached", false);

        if (isLimitReached) {
            Log.d("ReminderReceiver", "Ứng dụng " + packageName + " đã hết thời gian!");
            showNotification(context, packageName);
        } else {
            Log.d("ReminderReceiver", "Ứng dụng " + packageName + " còn " + remainingTime + " phút!");
            showTimeRemainingNotification(context, packageName, remainingTime);
        }
    }
    private void showTimeRemainingNotification(Context context, String packageName, long remainingTime) {
        String channelId = "reminder_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Nhắc nhở giới hạn", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent mở ứng dụng
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cảnh báo!")
                .setContentText("Ứng dụng " + packageName + " còn " + remainingTime + " phút sử dụng.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(packageName.hashCode(), builder.build());
    }

    private void showNotification(Context context, String packageName) {
        String channelId = "reminder_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Nhắc nhở giới hạn", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent mở ứng dụng
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent đóng ứng dụng
        Intent closeIntent = new Intent(context, CloseAppReceiver.class);
        closeIntent.putExtra("packageName", packageName);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(
                context, 1, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
// **Khởi động màn hình che phủ**
        Intent overlayIntent = new Intent(context, BlockScreenActivity.class);
        overlayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(overlayIntent);
        // Tạo thông báo với nút "Đóng ứng dụng"
        @SuppressLint("NotificationTrampoline") NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cảnh báo!")
                .setContentText("Đã hết thời gian sử dụng.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(packageName.hashCode(), builder.build());
    }

}
