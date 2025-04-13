package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class CloseAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra("packageName");
        Log.d("CloseAppReceiver", "Đóng ứng dụng: " + packageName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Log.e("CloseAppReceiver", "Ứng dụng chưa có quyền SYSTEM_ALERT_WINDOW!");
            return;
        }

        // Hiển thị màn hình che phủ để chặn ứng dụng
        Intent overlayIntent = new Intent(context, BlockScreenActivity.class);
        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(overlayIntent);
    }
}