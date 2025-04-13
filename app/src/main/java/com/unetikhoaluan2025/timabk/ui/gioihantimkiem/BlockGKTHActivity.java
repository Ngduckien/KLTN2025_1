package com.unetikhoaluan2025.timabk.ui.gioihantimkiem;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.ui.login.UserDAO;

import java.util.concurrent.Executors;

public class BlockGKTHActivity extends AppCompatActivity {
    private static final String TAG = "BlockGKTHActivity";
    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 30000; // 30 giây
    private static final String UNLOCK_PREFS = "UnlockPrefs";
    private static final String KEY_IS_UNLOCKED = "is_unlocked";
    private static final long UNLOCK_TIMEOUT = 10 * 60 * 1000;

    private int failedAttempts = 0;
    private Handler handler = new Handler();
    private SharedPreferences unlockPrefs;
    private EditText editPin;
    private Button btnUnlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ghtk_activity_ghtk_blockgkthactivity);

        unlockPrefs = getSharedPreferences(UNLOCK_PREFS, MODE_PRIVATE);

        // Kiểm tra trạng thái unlock trước khi hiển thị
        if (checkUnlockStatus()) {
            return;
        }

        configureSystemUI();
        initViews();
        setupListeners();
        applySecurityPolicy();
    }

    private boolean checkUnlockStatus() {
        // 1. Kiểm tra nếu đã unlock và chưa hết hạn
        if (unlockPrefs.getBoolean(KEY_IS_UNLOCKED, false) &&
                !isUnlockExpired()) {
            finish();
            return true;
        }

        // 2. Kiểm tra intent auto-unlock
        if (getIntent() != null && getIntent().getBooleanExtra("auto_unlock", false)) {
            unlockSuccessfully();
            return true;
        }

        return false;
    }

    private boolean isUnlockExpired() {
        long unlockTime = unlockPrefs.getLong("unlock_time", 0);
        Log.d("test giá trị hàm isUnlock",System.currentTimeMillis()+", "+unlockTime+", "+UNLOCK_TIMEOUT);
        return System.currentTimeMillis() - unlockTime > UNLOCK_TIMEOUT;

    }

    private void configureSystemUI() {
        try {
            // Fullscreen config
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );

            // Ẩn action bar
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            // Ngăn chặn chụp màn hình
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
            );
        } catch (Exception e) {
            Log.e(TAG, "Error configuring system UI", e);
        }
    }

    private void initViews() {
        editPin = findViewById(R.id.edit_pin);
        btnUnlock = findViewById(R.id.btn_unlock);

        // Hiển thị từ khóa bị chặn (nếu có)
        if (getIntent() != null && getIntent().hasExtra("blocked_keyword")) {
            String keyword = getIntent().getStringExtra("blocked_keyword");
            TextView tvBlocked = findViewById(R.id.tv_blocked_keyword);
            if (tvBlocked != null) {
                tvBlocked.setText("Bạn đang tìm nội dung không phù hợp" );
                Log.d("Test Key",keyword);
            }
        }
    }

    private void setupListeners() {
        btnUnlock.setOnClickListener(v -> handleUnlockAttempt());

        findViewById(R.id.btnDongUngDung).setOnClickListener(v -> {
            // Thêm xác nhận trước khi đóng ứng dụng
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn thoát ứng dụng?")
                    .setPositiveButton("Đóng", (dialog, which) -> closeApp())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void applySecurityPolicy() {
        // Reset số lần thử nếu đã qua thời gian lockout
        if (failedAttempts >= MAX_PIN_ATTEMPTS) {
            long lastAttemptTime = unlockPrefs.getLong("last_attempt_time", 0);
            if (System.currentTimeMillis() - lastAttemptTime > LOCKOUT_DURATION) {
                failedAttempts = 0;
            }
        }
    }

    private void handleUnlockAttempt() {
        String enteredPin = editPin.getText().toString().trim();

        if (enteredPin.isEmpty()) {
            showToast("Vui lòng nhập mã PIN");
            return;
        }

        if (isLockedOut()) {
            showLockoutMessage();
            return;
        }

        verifyPin(enteredPin);
    }

    private void verifyPin(String enteredPin) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String currentUser = getCurrentUser();
            if (currentUser == null) return;

            UserDAO userDAO = AppDatabase.getInstance(this).userDAO();
            boolean isValid = userDAO.checkUser(currentUser, enteredPin) > 0;

            runOnUiThread(() -> {
                if (isValid) {
                    unlockSuccessfully();
                } else {
                    handleFailedAttempt();
                }
            });
        });
    }

    private void unlockSuccessfully() {
        // Cập nhật trạng thái unlock
        unlockPrefs.edit()
                .putBoolean(KEY_IS_UNLOCKED, true)
                .putLong("unlock_time", System.currentTimeMillis())
                .apply();

        // Gửi broadcast thông báo unlock thành công
        sendBroadcast(new Intent("ACTION_UNLOCK_SUCCESS"));

        // Kết thúc activity với animation
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void handleFailedAttempt() {
        failedAttempts++;
        unlockPrefs.edit()
                .putLong("last_attempt_time", System.currentTimeMillis())
                .apply();

        if (isLockedOut()) {
            lockTemporarily();
            showLockoutMessage();
        } else {
            showToast("Sai mã PIN. Lần thử " + failedAttempts + "/" + MAX_PIN_ATTEMPTS);
            editPin.setText("");
            editPin.requestFocus();
        }
    }

    private void showLockoutMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Vượt quá số lần thử")
                .setMessage("Bạn đã nhập sai quá 5 lần. Vui lòng đợi 30 giây.")
                .setPositiveButton("OK", (dialog, which) -> closeApp())
                .setCancelable(false)
                .show();
    }

    private void lockTemporarily() {
        btnUnlock.setEnabled(false);
        editPin.setEnabled(false);

        handler.postDelayed(() -> {
            btnUnlock.setEnabled(true);
            editPin.setEnabled(true);
            failedAttempts = 0;
            editPin.setText("");
        }, LOCKOUT_DURATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void closeApp() {
        unlockPrefs.edit()
                .putBoolean(KEY_IS_UNLOCKED, false)
                .remove("unlock_time")
                .apply();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private String getCurrentUser() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return preferences.getString("user", "");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isLockedOut() {
        return failedAttempts >= MAX_PIN_ATTEMPTS;
    }

    @Override
    public void onBackPressed() {
        // Chặn nút back vật lý

    }
}