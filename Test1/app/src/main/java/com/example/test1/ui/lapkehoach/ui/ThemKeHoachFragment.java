package com.unetikhoaluan2025.timabk.ui.lapkehoach.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.viewmodel.LapKeHoachViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ThemKeHoachFragment extends Fragment {
    private LapKeHoachViewModel viewModel;
    private EditText editTenKH, editMoTa, editStarKH, editEndKH, editTGNN, editNgayNN, editNgayKT;
    private Button btnSave, btnHuy;
    private Spinner spinnerLapLai;
    public static ThemKeHoachFragment newInstance() {
        return new ThemKeHoachFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lapkehoach_fragment_themkehoach, container, false);
        ThamChieu(view);
        viewModel = new ViewModelProvider(this).get(LapKeHoachViewModel.class);

        btnSave.setOnClickListener(v -> clickSave(view));

        setupDatePicker(editStarKH);
        setupDatePicker(editEndKH);
        setupDatePicker(editNgayNN);
        setupDatePicker(editNgayKT);
        setupTimePicker(editTGNN);

        return view;
    }

    private void clickSave(View view) {
        SharedPreferences preferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
         String  currentUserId = preferences.getString("user", "");
        Log.d("test user ",currentUserId);

            // Lấy dữ liệu từ các EditText
            Date ngayBatDau = null;
            Date ngayKetThuc = null;
            Date ngayNhacNho = null;
            Date ngayKetThucLap = null;
            String tenKH = editTenKH.getText().toString().trim();
            String moTa = editMoTa.getText().toString().trim();
            String ngayBatDauStr = editStarKH.getText().toString().trim();
            String ngayKetThucStr = editEndKH.getText().toString().trim();
            String thoiGianNhacNhoStr = editTGNN.getText().toString().trim();
            String ngayNhacNhoStr = editNgayNN.getText().toString().trim();
            String lapLai = spinnerLapLai.getSelectedItem().toString();
            String ngayKetThucLapStr = editNgayKT.getText().toString().trim();
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                ngayBatDau = sdfDate.parse(ngayBatDauStr);
                ngayKetThuc = sdfDate.parse(ngayKetThucStr);
                ngayNhacNho = sdfDate.parse(ngayNhacNhoStr);
                ngayKetThucLap = sdfDate.parse(ngayKetThucLapStr);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Log.d("giá trị Kehoach",currentUserId+", "+tenKH+", "+ngayBatDau.getTime()+", "+ngayKetThuc.getTime()+","+moTa+","
                    +thoiGianNhacNhoStr+", "+ngayNhacNho.getTime()+", "+lapLai+", "+ngayKetThucLap.getTime());

            // Kiểm tra và xử lý nếu có dữ liệu thiếu
            if (tenKH.isEmpty() || moTa.isEmpty() ) {
                // Hiển thị thông báo nếu thiếu dữ liệu cần thiết
                Toast.makeText(getActivity(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                // Tạo một đối tượng LapKeHoach mới và gọi ViewModel để lưu
                LapKeHoach lapKeHoach = new LapKeHoach(currentUserId,tenKH,ngayBatDau.getTime(),ngayKetThuc.getTime(),moTa,thoiGianNhacNhoStr,ngayNhacNho.getTime(),lapLai,ngayKetThucLap.getTime());

                viewModel.insertKeHoach(lapKeHoach);
                Toast.makeText(getActivity(), "Lưu thành công", Toast.LENGTH_SHORT).show();
                clickCancel(view);
            }

    }
    private void setupDatePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year1, month1, dayOfMonth) -> {
                        String date = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                        editText.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupTimePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute1) -> {
                        String time = String.format("%02d:%02d", hourOfDay, minute1);
                        editText.setText(time);
                    },
                    hour, minute, true
            );
            timePickerDialog.show();
        });
    }


    private void ThamChieu(View view) {
        editTenKH = view.findViewById(R.id.editTenKH);
        editMoTa = view.findViewById(R.id.editMoTa);
        editStarKH = view.findViewById(R.id.editStarKH);
        editEndKH = view.findViewById(R.id.editEndKH);
        editTGNN = view.findViewById(R.id.editTGNN);
        editNgayNN = view.findViewById(R.id.editNgayNN);
        editNgayKT = view.findViewById(R.id.editNgayKT);
        btnSave = view.findViewById(R.id.btnSave);
        spinnerLapLai = view.findViewById(R.id.spinnerLapLai);
    }

    private void clickCancel(View v) {
        // Quay lại màn hình trước đó
        NavController navController = Navigation.findNavController(v);
        navController.popBackStack(); // Quay lại fragment trước

    }
}