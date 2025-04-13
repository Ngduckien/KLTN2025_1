package com.unetikhoaluan2025.timabk.ui;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.databinding.ActivityTrangChuBinding;
import com.unetikhoaluan2025.timabk.service.MyAppUsageService;
import com.unetikhoaluan2025.timabk.ui.login.MainActivity;
import com.unetikhoaluan2025.timabk.ui.login.User;
import com.unetikhoaluan2025.timabk.ui.login.UserDAO;
import com.unetikhoaluan2025.timabk.notifications.ReminderScheduler;
import com.unetikhoaluan2025.timabk.service.AppUsageService;
import com.unetikhoaluan2025.timabk.service.UsageMonitorService;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.viewmodel.LapKeHoachViewModel;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.concurrent.Executors;

public class TrangChuActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityTrangChuBinding binding;
    private LapKeHoachViewModel lapKeHoachViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo binding
        binding = ActivityTrangChuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarTrangChu.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Cấu hình các fragment chính
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_tktg, R.id.nav_gioihan,
                R.id.nav_gioihantk, R.id.nav_qltt, R.id.nav_lapkehoach)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_trang_chu);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        // Lấy header của NavigationView
        View headerView = navigationView.getHeaderView(0);
        TextView txtHoTen = headerView.findViewById(R.id.txtHoTen);
        TextView txtEmail = headerView.findViewById(R.id.txtEmail);

        //  Lấy username từ SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String currentUser = preferences.getString("user", ""); // Lấy username đã đăng nhập

        if (!currentUser.isEmpty()) {
            UserDAO userDAO = AppDatabase.getInstance(this).userDAO();

            // Lấy dữ liệu từ CSDL trên luồng phụ
            Executors.newSingleThreadExecutor().execute(() -> {
                User user = userDAO.getUserByUsername(currentUser);
                if (user != null) {
                    runOnUiThread(() -> {
                        txtHoTen.setText(user.getHoten()); // Hiển thị họ tên
                        txtEmail.setText(user.getEmail()); // Hiển thị email
                    });
                }
            });
        }
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Danh sách các mục yêu cầu nhập mã PIN
            if (itemId == R.id.nav_tktg || itemId == R.id.nav_gioihan || itemId == R.id.nav_lapkehoach || itemId == R.id.nav_gioihantk || itemId == R.id.nav_qltt) {
                requestPinAndNavigate(itemId, navController);
            } else {
                navController.navigate(itemId);
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START); // Đóng menu sau khi chọn
            return true;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                OnBackPressedDispatcher();
            }
        });
        checkAndRequestExactAlarmPermission();
        checkUsageAccessPermission();
        checkAndRequestNotificationPermission();
        checkAndRequestUsageStatsPermission();
        checkAndRequestAccessibilityPermission(this, MyAppUsageService.class);
        /////////////////////////////////////////////////////
        Intent serviceIntent = new Intent(this, UsageMonitorService.class);
        startService(serviceIntent);
        Intent si = new Intent(this, AppUsageService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(si);  // API 26+
        } else {
            startService(si);  // API < 26
        }
        lapKeHoachViewModel = new ViewModelProvider(this).get(LapKeHoachViewModel.class);
        lapKeHoachViewModel.restoreAllNotifications();


    }
    private void checkAndRequestUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            // Nếu quyền chưa được cấp, yêu cầu người dùng cấp quyền
            new AlertDialog.Builder(this)
                    .setTitle("Yêu cầu quyền truy cập thống kê sử dụng")
                    .setMessage("Ứng dụng cần quyền để truy cập thống kê sử dụng ứng dụng.")
                    .setPositiveButton("Cấp quyền", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivityForResult(intent, REQUEST_USAGE_ACCESS);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Log.d("UsageAccess", "Quyền truy cập thống kê sử dụng đã được cấp.");
        }
    }

    private void checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Yêu cầu quyền đặt lịch báo thức")
                        .setMessage("Ứng dụng cần quyền đặt lịch chính xác để gửi thông báo. Hãy cấp quyền trong Cài đặt.")
                        .setPositiveButton("Mở Cài đặt", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        }
    }

    private void requestPinAndNavigate(int fragmentId, NavController navController) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập mã PIN để tiếp tục");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String currentUser = preferences.getString("user", "");

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String enteredPin = input.getText().toString().trim();
            if (enteredPin.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã PIN", Toast.LENGTH_SHORT).show();

            }
            else
            {
                if (currentUser.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserDAO userDAO = AppDatabase.getInstance(this).userDAO();
                Executors.newSingleThreadExecutor().execute(() -> {
                    User user = userDAO.getUserByUsername(currentUser);
                    if (user != null) {
                        String correctPin = user.getPin();
                        runOnUiThread(() -> {
                            if (enteredPin.equals(correctPin)) {
                                navController.navigate(fragmentId);
                            } else {
                                Toast.makeText(this, "Mã PIN không đúng!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Lỗi khi lấy dữ liệu người dùng!", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
        

    }
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1002;

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Nên giải thích lý do cần quyền trước khi yêu cầu
                new AlertDialog.Builder(this)
                        .setTitle("Quyền thông báo")
                        .setMessage("Ứng dụng cần gửi thông báo để nhắc nhở bạn về thời gian sử dụng ứng dụng và các giới hạn đã đặt.")
                        .setPositiveButton("Tiếp tục", (dialog, which) -> {
                            // Yêu cầu quyền trực tiếp
                            requestPermissions(
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                    REQUEST_NOTIFICATION_PERMISSION
                            );
                        })
                        .setNegativeButton("Để sau", null)
                        .show();
            }
        }
    }

    private static final int REQUEST_USAGE_ACCESS = 1001;

    private void checkUsageAccessPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            // Hiển thị thông báo yêu cầu cấp quyền
            new AlertDialog.Builder(this)
                    .setTitle("Yêu cầu cấp quyền")
                    .setMessage("Ứng dụng cần quyền truy cập thống kê sử dụng để hiển thị dữ liệu thời gian sử dụng ứng dụng.")
                    .setPositiveButton("Cấp quyền", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivityForResult(intent, REQUEST_USAGE_ACCESS);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Log.d("UsageAccess", "Quyền đã được cấp!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USAGE_ACCESS) {
            checkUsageAccessPermission(); // Kiểm tra lại quyền sau khi trở về ứng dụng
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trang_chu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_trang_chu);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }


    public void OnBackPressedDispatcher() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_trang_chu);
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.nav_home) {
            navController.popBackStack(R.id.nav_home, false);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thoát")
                    .setMessage("Bạn có chắc chắn thoát không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        handleLogout();
                        finish(); // Thay thế super.onBackPressed()
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("user_email");
                    editor.remove("user_password");
                    editor.apply();

                    Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        ReminderScheduler.scheduleReminders(this);
    }

    public void checkAndRequestAccessibilityPermission(Context context, Class<? extends AccessibilityService> serviceClass) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return;

        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        boolean isEnabled = false;

        for (AccessibilityServiceInfo service : enabledServices) {
            ServiceInfo si = service.getResolveInfo().serviceInfo;
            if (si.packageName.equals(context.getPackageName()) && si.name.equals(serviceClass.getName())) {
                isEnabled = true;
                break;
            }
        }

        if (!isEnabled) {
            new AlertDialog.Builder(context instanceof Activity ? (Activity) context : new ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault_Light_Dialog))
                    .setTitle("Yêu cầu quyền Trợ năng")
                    .setMessage("Ứng dụng cần quyền Trợ năng để hoạt động chính xác. Bạn có muốn cấp quyền không?")
                    .setCancelable(false)
                    .setPositiveButton("Cấp quyền", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        if (!(context instanceof Activity)) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        context.startActivity(intent);
                    })
                    .setNegativeButton("Thoát", (dialog, which) -> {
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    })
                    .show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền thông báo bị từ chối", Toast.LENGTH_SHORT).show();
                // Có thể hướng dẫn người dùng bật thủ công trong cài đặt
            }
        }
    }
        // Phương thức từ phần d.3
    private boolean canShowNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // Phương thức từ phần 5
    private void showNotificationPermissionSettings() {
        new AlertDialog.Builder(this)
                .setTitle("Hướng dẫn cấp quyền")
                .setMessage("Vui lòng bật quyền thông báo trong cài đặt ứng dụng")
                .setPositiveButton("Mở cài đặt", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Để sau", null)
                .show();
    }



}
