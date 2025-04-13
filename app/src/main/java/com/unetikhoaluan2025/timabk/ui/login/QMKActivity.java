package com.unetikhoaluan2025.timabk.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    EditText editEmail, editPin, editNewPass, editConfirn;
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
        String Confirn = editConfirn.getText().toString().trim();
        if (user.isEmpty() || pin.isEmpty() || newPass.isEmpty() || Confirn.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Confirn.equals(newPass))
        {
            Toast.makeText(this, "Nhập lại mật khẩu vì không khớp!", Toast.LENGTH_SHORT).show();
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
        editConfirn = findViewById(R.id.editConfirnnewPass);
        editEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });

        editEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validateEmail();
            }
        });
    }
    private void validateEmail() {
        String username = editEmail.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            editEmail.setError("Tên đăng nhập không được để trống!");
        } else if (!isValidEmail(username) && !isValidPhone(username)) {
            editEmail.setError("Vui lòng nhập email hoặc số điện thoại hợp lệ!");
        } else {
            editEmail.setError(null);
        }
    }
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }
}