package com.unetikhoaluan2025.timabk.ui.qltt;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.ui.login.User;
import com.unetikhoaluan2025.timabk.ui.login.UserDAO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SuaFragment extends Fragment {
    private EditText edtTen, edtGT, edtNgaySinh, edtPhone, edtEmail;
    private TextView txtUser;
    private Button btnLuu, btnHuy;

    public SuaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qltt_fragment_qltt_sua, container, false);

        thamchieu(view);

        if (edtNgaySinh != null) {
            edtNgaySinh.setFocusable(false);
            edtNgaySinh.setOnClickListener(v -> showDatePickerDialog());

        } else {
            Log.e("SuaFragment", "edtNgaySinh is null! Check fragment_qltt_sua.xml.");
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            edtTen.setText(bundle.getString("hoten", ""));
            edtGT.setText(bundle.getString("gt", ""));
            edtNgaySinh.setText(bundle.getString("ngaysinh", ""));
            edtPhone.setText(bundle.getString("phone", ""));
            edtEmail.setText(bundle.getString("email", ""));
            txtUser.setText(bundle.getString("user", ""));
        }

        btnHuy.setOnClickListener(v -> requireActivity().onBackPressed());
        btnLuu.setOnClickListener(v -> {
            User user = getUserInput();
            if (user == null) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            updateUserData(user);
        });
        return view;
    }

    private void thamchieu(View view) {
        edtTen = view.findViewById(R.id.edtFullName);
        edtGT = view.findViewById(R.id.edtGender);
        edtNgaySinh = view.findViewById(R.id.edtBirthDate);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        txtUser = view.findViewById(R.id.edtUsername);
        btnLuu = view.findViewById(R.id.btnSave);
        btnHuy = view.findViewById(R.id.btnCancel);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year);
                    edtNgaySinh.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private User getUserInput() {
        String hoten = edtTen.getText().toString().trim();
        String gt = edtGT.getText().toString().trim();
        String ngaysinh = edtNgaySinh.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String username = txtUser.getText().toString().trim();

        if (hoten.isEmpty() || gt.isEmpty() || ngaysinh.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dateOfBirth = null;
        try {
            dateOfBirth = sdf.parse(ngaysinh);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new User(username, hoten, gt, phone, email, dateOfBirth);
    }

    private void updateUserData(User user) {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                UserDAO userDao = db.userDAO();

                // Chuyển đổi Date thành String
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateOfBirthStr = user.getNgaysinh() != null ? sdf.format(user.getNgaysinh()) : null;

                if (dateOfBirthStr == null) {
                    Log.e("updateUserData", "Ngày sinh null, không thể cập nhật!");
                    return;
                }

                userDao.updateUserByUsername(
                        user.getUser(),
                        user.getHoten(),
                        user.getGt(),
                        user.getSdt(),
                        user.getEmail(),
                        dateOfBirthStr
                );

                requireActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    }
                });
            } catch (Exception e) {
                Log.e("updateUserData", "Lỗi cập nhật dữ liệu: " + e.getMessage(), e);
            }
        }).start();
    }
}