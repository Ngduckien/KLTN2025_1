package com.unetikhoaluan2025.timabk.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;

public class DangKyActivity extends AppCompatActivity {
    Button btnBack, btnDK;
    EditText editHoten, editUser, editPass,editPass2, editPin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity_dang_ky);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        thamchieu();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(DangKyActivity.this, MainActivity.class);
                startActivity(login);
                finish();
            }
        });
        btnDK.setOnClickListener(v -> AddClick());
    }

    private void AddClick() {
        String user = editUser.getText().toString().trim();
        String pass = editPass.getText().toString().trim();
        String pass2 = editPass2.getText().toString().trim();
        String hoten = editHoten.getText().toString().trim();
        String pin = editPin.getText().toString().trim();

        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass) ||
                TextUtils.isEmpty(pass2) || TextUtils.isEmpty(hoten) || TextUtils.isEmpty(pin)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!isValidEmail(user) && !isValidPhone(user)) {
            Toast.makeText(this, "Tài khoản phải là email hợp lệ hoặc số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (pass.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(pass2)) {
            Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp!", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!pin.matches("^[0-9]{4}$")) {
            Toast.makeText(this, "Mã PIN phải là số và có đúng 4 chữ số!", Toast.LENGTH_SHORT).show();
            return;
        }
        UserDAO userDAO = AppDatabase.getInstance(this).userDAO();
        User existingUser = userDAO.getUserByUsername(user);

        if (existingUser != null) {
            Toast.makeText(this, "Tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }


        User user1 = new User(user,pass,hoten,pass2,pin);
        AppDatabase.getInstance(this).userDAO().InsetUser(user1);
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        editHoten.setText("");
        editUser.setText("");
        editPass.setText("");
        editPass2.setText("");
        editPin.setText("");
        Intent login = new Intent(DangKyActivity.this, MainActivity.class);
        startActivity(login);
        finish();
    }

    private void thamchieu() {
        btnBack = findViewById(R.id.btnback);
        btnDK = findViewById(R.id.btnDangKy);
        editHoten = findViewById(R.id.editHoTen);
        editUser = findViewById(R.id.editUser);
        editPass = findViewById(R.id.editPass);
        editPass2 = findViewById(R.id.editXacNhanPass);
        editPin = findViewById(R.id.editMaPin);
    }
    // Kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Kiểm tra số điện thoại (10 số, bắt đầu bằng 03, 05, 07, 08, 09)
    private boolean isValidPhone(@NonNull String phone) {
        return phone.matches("^(03|05|07|08|09)[0-9]{8}$");
    }
}