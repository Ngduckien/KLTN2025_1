package com.unetikhoaluan2025.timabk.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.unetikhoaluan2025.timabk.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "app_limit_channel";

    public static void showLimitReachedNotification(Context context, String packageName, PendingIntent closeIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Giới hạn ứng dụng", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Hết thời gian sử dụng!")
                .setContentText("Ứng dụng " + packageName + " đã hết thời gian sử dụng.")
                .setAutoCancel(true);

        notificationManager.notify(packageName.hashCode(), builder.build());

    }

}
