package com.unetikhoaluan2025.timabk.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;

public class QMKActivity extends AppCompatActivity {
    Button btnQMK, btnback;
    EditText editEmail, editPin, editNewPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity_qmk);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        thamchieu();
        btnback.setOnClickListener(v -> ClickBack());
        btnQMK.setOnClickListener(v -> ClickQMK());
    }

    private void ClickQMK() {
        String user = editEmail.getText().toString().trim();
        String pin = editPin.getText().toString().trim();
        String newPass = editNewPass.getText().toString().trim();
        if (user.isEmpty() || pin.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy instance của Room Database
        AppDatabase userDB = AppDatabase.getInstance(this);
        UserDAO userDAO = userDB.userDAO();

        // Kiểm tra Email + PIN
        if (userDAO.checkUser(user, pin) > 0) {
            userDAO.updatePassword(user, newPass);
            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            Intent login = new Intent(QMKActivity.this, MainActivity.class);
            startActivity(login);
            finish();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc Mã PIN không đúng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void ClickBack() {
        Intent login = new Intent(QMKActivity.this, MainActivity.class);
        startActivity(login);
        finish();
    }

    private void thamchieu() {
        btnQMK = findViewById(R.id.btnGuiMKM);
        btnback = findViewById(R.id.btnHuy);
        editEmail = findViewById(R.id.editemailQMK);
        editPin = findViewById(R.id.editPinQMK);
        editNewPass = findViewById(R.id.editnewPass);
    }
}