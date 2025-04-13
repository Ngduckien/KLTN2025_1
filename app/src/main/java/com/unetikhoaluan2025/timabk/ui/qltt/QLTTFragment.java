package com.unetikhoaluan2025.timabk.ui.qltt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.ui.login.MainActivity;
import com.unetikhoaluan2025.timabk.ui.login.User;
import com.unetikhoaluan2025.timabk.ui.login.UserDAO;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.Converter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class QLTTFragment extends Fragment {
    Button btnSua, btnDMK, btnXoaTaiKhoan;
    private TextView txtTen, txtGT, txtNgaySinh, txtPhone, txtEmail, txtUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qltt_fragment_qltt, container, false);


        // Gọi phương thức thamchieu và truyền view vào
        thamchieu(view);
        btnSua.setOnClickListener(this::clickSua);
        btnDMK.setOnClickListener(v ->Navigation.findNavController(v).navigate(R.id.nav_qltt_dmk));
        btnXoaTaiKhoan.setOnClickListener(v -> XoaTaiKhoan());
        loadUserData();

        return view;
    }
    public void clickSua(View v)
    {
        // Lấy dữ liệu từ TextView
        Bundle bundle = new Bundle();
        bundle.putString("hoten", txtTen.getText().toString().replace("Họ và tên: ", ""));
        bundle.putString("gt", txtGT.getText().toString().replace("Giới tính: ", ""));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngaySinhText = txtNgaySinh.getText().toString().replace("Ngày sinh: ", "").trim();

        if (ngaySinhText.isEmpty() || ngaySinhText.equals("Chưa có")) {
            ngaySinhText = sdf.format(Calendar.getInstance().getTime()); // Lấy ngày hiện tại
        }

        bundle.putString("ngaysinh", ngaySinhText);
        bundle.putString("phone", txtPhone.getText().toString().replace("Số điện thoại: ", ""));
        bundle.putString("email", txtEmail.getText().toString().replace("Email: ", ""));
        bundle.putString("user", txtUser.getText().toString().replace("Tên đăng nhập: ", ""));

        Log.d("QLTTFragment", "Chuyển đến SuaFragment với dữ liệu: " + bundle);

        // Chuyển đến SuaFragment
        Navigation.findNavController(v).navigate(R.id.nav_qltt_sua, bundle);
    }
    private void loadUserData() {
        try {
            SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String currentUser = preferences.getString("user", "");

            if (!currentUser.isEmpty()) {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                UserDAO userDAO = db.userDAO();
                User user = userDAO.getUserByUsername(currentUser);

                if (user != null) {
                    Log.d("DEBUG", "Ngày sinh từ DB: " + user.getNgaysinh());

                    txtTen.setText("Họ và tên: " + user.getHoten());
                    txtGT.setText("Giới tính: " + user.getGt());
                    txtNgaySinh.setText("Ngày sinh: "+Converter.formatDate(user.getNgaysinh()));
                    txtPhone.setText("Số điện thoại: " + user.getSdt());
                    txtEmail.setText("Email: " + user.getEmail());
                    txtUser.setText("Tên đăng nhập: " + user.getUser());
                } else {
                    Log.e("loadUserData", "Không tìm thấy người dùng trong DB");
                    Toast.makeText(requireContext(), "Không tìm thấy dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("loadUserData", "Lỗi khi load dữ liệu: " + e.getMessage(), e);
        }
    }

    private void XoaTaiKhoan() {
        try {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận xóa tài khoản")
                    .setMessage("Bạn có chắc chắn muốn xóa tài khoản này không?")
                    .setPositiveButton("Có", (dialog, which) ->
                    {
                        new Thread(() -> {
                            try {
                                AppDatabase db = AppDatabase.getInstance(requireContext());
                                UserDAO userDao = db.userDAO();

                                SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                String currentUser = preferences.getString("user", "");

                                if (!currentUser.isEmpty()) {
                                    userDao.deleteByUsername(currentUser);
                                    preferences.edit().clear().apply();
                                }

                                requireActivity().runOnUiThread(() -> {
                                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                });
                            } catch (Exception e) {
                                Log.e("XoaTaiKhoan", "Lỗi khi xóa tài khoản: " + e.getMessage(), e);
                            }
                        }).start();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            Log.e("XoaTaiKhoan", "Lỗi khi hiển thị hộp thoại xóa tài khoản: " + e.getMessage(), e);
        }
    }

    private void thamchieu(View view) {
        btnDMK = view.findViewById(R.id.btnDoiMK);
        btnSua = view.findViewById(R.id.btnSua);
        btnXoaTaiKhoan = view.findViewById(R.id.btnXoaTK);
        txtTen = view.findViewById(R.id.txtten);
        txtGT = view.findViewById(R.id.txtGT);
        txtNgaySinh = view.findViewById(R.id.txtNgaySinh);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtUser = view.findViewById(R.id.txtUser);
    }



}