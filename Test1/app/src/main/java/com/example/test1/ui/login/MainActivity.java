package com.unetikhoaluan2025.timabk.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.ui.TrangChuActivity;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.viewmodel.LapKeHoachViewModel;

public class MainActivity extends AppCompatActivity {
    Button btnlogin;
    ImageButton btnImg;
    TextView textQMK, textDK;
    EditText editUser, editPass;
    private boolean isPasswordVisible = false;
    private LapKeHoachViewModel lapKeHoachViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        thamchieu();

        btnlogin.setOnClickListener(view -> ClickLogin());
        textDK.setOnClickListener(view -> ClickDk());
        textQMK.setOnClickListener(view -> ClickQMK());
        btnImg.setOnClickListener(v -> ClickAnHien());

    }


    private void ClickAnHien() {
        if (isPasswordVisible) {
            editPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnImg.setImageResource(R.drawable.ic_key_24);
        } else {
            editPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnImg.setImageResource(R.drawable.ic_key_off_24);
        }
        isPasswordVisible = !isPasswordVisible;
    }

    private void ClickQMK() {
        Intent qmk = new Intent(MainActivity.this, QMKActivity.class);
        startActivity(qmk);
        finish();
    }

    private void ClickDk() {
        Intent signin = new Intent(MainActivity.this, DangKyActivity.class);
        startActivity(signin);
        finish();
    }

    private void ClickLogin() {
        String user = editUser.getText().toString().trim();
        String pass = editPass.getText().toString().trim();
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra thông tin đăng nhập trong database
        UserDAO userDAO = AppDatabase.getInstance(this).userDAO();
        User existingUser = userDAO.loginUser(user, pass);

        if (existingUser == null) {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu username
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user", user);
        editor.apply();
        // chuyển trang
        Intent s = new Intent(MainActivity.this, TrangChuActivity.class);
        startActivity(s);
        finish();

        lapKeHoachViewModel = new ViewModelProvider(this).get(LapKeHoachViewModel.class);
        lapKeHoachViewModel.restoreAllNotifications();
    }

    private void thamchieu() {
        btnlogin = findViewById(R.id.btnLogin);
        editUser = findViewById(R.id.editUser);
        editPass = findViewById(R.id.editPass);
        textDK = findViewById(R.id.textDK);
        textQMK = findViewById(R.id.textQMK);
        btnImg = findViewById(R.id.imageButton);
    }

}