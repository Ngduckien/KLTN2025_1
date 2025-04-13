package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.unetikhoaluan2025.timabk.R;

public class BlockScreenActivity extends Activity {
    private Button btnDong, btnGiahan;
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gioihan_activity_block_screen);

        // **Lấy packageName từ Intent**
        packageName = getIntent().getStringExtra("packageName");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView message = findViewById(R.id.block_message);
        message.setText("Thời gian sử dụng ứng dụng đã hết!");

        // **Khởi tạo Button**
        btnDong = findViewById(R.id.btnDong);
        btnGiahan = findViewById(R.id.btnGiahan);

        // **Đóng ứng dụng**
        btnDong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeApp();
            }
        });

        // **Gia hạn thêm 10 phút**
//        btnGiahan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                extendTime();
//            }
//        });
    }

    // **Hàm đóng ứng dụng**
    private void closeApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }

    // **Hàm gia hạn thêm 10 phút**
    private void extendTime() {
        // gán giá trị ở đây bằng 10 và truyền sang activyti khác
        int timeValue = 0;
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("timeKey", timeValue + 10);  // Cộng thêm 10 phút
        editor.apply(); // Đóng màn hình BlockScreen
    }
}
