package com.unetikhoaluan2025.timabk.ui.qltt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.ui.login.User;
import com.unetikhoaluan2025.timabk.ui.login.UserDAO;


public class DoiMKFragment extends Fragment {

    private EditText edtOldPass, edtNewPass, edtConfirmPass;
    private Button btnLuu, btnHuy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qltt_fragment_qltt_doimk, container, false);

        // Ánh xạ view
        edtOldPass = view.findViewById(R.id.editOldPass);
        edtNewPass = view.findViewById(R.id.editNewPass);
        edtConfirmPass = view.findViewById(R.id.editConfirmPass);
        btnLuu = view.findViewById(R.id.btnLuu);
        btnHuy = view.findViewById(R.id.btnHuyMK);

        // Sự kiện nút Lưu
        btnLuu.setOnClickListener(v -> doiMatKhau());

        // Sự kiện nút Hủy
        btnHuy.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_qltt));

        return view;
    }

    private void doiMatKhau() {
        // Lấy dữ liệu nhập vào
        String oldPass = edtOldPass.getText().toString().trim();
        String newPass = edtNewPass.getText().toString().trim();
        String confirmPass = edtConfirmPass.getText().toString().trim();

        // Kiểm tra xem có nhập đầy đủ không
        if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp nhau không
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(getContext(), "Mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy username từ SharedPreferences
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUser = preferences.getString("user", "");

        if (currentUser.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện đổi mật khẩu trong Room Database
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            UserDAO userDao = db.userDAO();

            // Lấy thông tin user
            User user = userDao.getUserByUsername(currentUser);

            // Kiểm tra mật khẩu cũ có đúng không
            if (user == null || !user.getPass().equals(oldPass)) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Mật khẩu cũ không chính xác!", Toast.LENGTH_SHORT).show());
                return;
            }

            // Cập nhật mật khẩu mới
            userDao.updatePassword(currentUser, newPass);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(getView()).navigate(R.id.nav_qltt); // Quay lại màn trước
            });

        }).start();
    }


}