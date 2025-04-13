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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
    private TextView textView26;
    private Button btnSaveKH, btnHuy;
    private Spinner spinnerLapLai;
    public static ThemKeHoachFragment newInstance() {
        return new ThemKeHoachFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lapkehoach_fragment_themkehoach, container, false);

        btnSaveKH = view.findViewById(R.id.btnSaveKH);
        editTenKH = view.findViewById(R.id.editTenKH);
        editMoTa = view.findViewById(R.id.editMoTa);
        editStarKH = view.findViewById(R.id.editStarKH);
        editEndKH = view.findViewById(R.id.editEndKH);
        editTGNN = view.findViewById(R.id.editTGNN);
        editNgayNN = view.findViewById(R.id.editNgayNN);
        editNgayKT = view.findViewById(R.id.editNgayKT);
        spinnerLapLai = view.findViewById(R.id.spinnerLapLai);
        textView26 = view.findViewById(R.id.textView26);
        btnHuy = view.findViewById(R.id.btnCancelKH);

        viewModel = new ViewModelProvider(this).get(LapKeHoachViewModel.class);
       // ThamChieu(view);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.lapLaiOptions, // bạn cần định nghĩa mảng này trong strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLapLai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerLapLai.getSelectedItem().toString();
                editNgayKT.setVisibility(selected.equals("Không") ? View.GONE : View.VISIBLE);
                textView26.setVisibility(selected.equals("Không") ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Log.d("CHECK", "btnSave is null? " + (btnSaveKH == null));
        btnSaveKH.setOnClickListener(v -> clickSave(view));
        btnHuy.setOnClickListener(v -> clickCancel(view));
        setupDatePicker(editStarKH);
        setupDatePicker(editEndKH);
        setupDatePicker(editNgayNN);
        setupDatePicker(editNgayKT);
        setupTimePicker(editTGNN);


        return view;
    }

    private void clickSave(View view) {
        SharedPreferences preferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserId = preferences.getString("user", "");
        Log.d("test user", currentUserId);

        String tenKH = editTenKH.getText().toString().trim();
        String moTa = editMoTa.getText().toString().trim();
        String ngayBatDauStr = editStarKH.getText().toString().trim();
        String ngayKetThucStr = editEndKH.getText().toString().trim();
        String thoiGianNhacNhoStr = editTGNN.getText().toString().trim();
        String ngayNhacNhoStr = editNgayNN.getText().toString().trim();
        String ngayKetThucLapStr = editNgayKT.getText().toString().trim();
        String lapLai = spinnerLapLai.getSelectedItem().toString();

        if (tenKH.isEmpty() || moTa.isEmpty() || ngayBatDauStr.isEmpty() || ngayKetThucStr.isEmpty()
                || thoiGianNhacNhoStr.isEmpty() || ngayNhacNhoStr.isEmpty() || ( !lapLai.equals("Không") && ngayKetThucLapStr.isEmpty())) {
            Toast.makeText(getActivity(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date ngayBatDau, ngayKetThuc, ngayNhacNho;
        Date ngayKetThucLap = null;

        try {
            ngayBatDau = sdfDate.parse(ngayBatDauStr);
            ngayKetThuc = sdfDate.parse(ngayKetThucStr);
            ngayNhacNho = sdfDate.parse(ngayNhacNhoStr);
            if (!lapLai.equals("Không")) {
                ngayKetThucLap = sdfDate.parse(ngayKetThucLapStr);
            }
        } catch (ParseException e) {
            Toast.makeText(getActivity(), "Ngày không hợp lệ, vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        long ngayKetThucLapMillis = ngayKetThucLap != null ? ngayKetThucLap.getTime() : 0;

        Log.d("giá trị Kehoach", currentUserId + ", " + tenKH + ", " + ngayBatDau.getTime() + ", " + ngayKetThuc.getTime() + "," + moTa + ","
                + thoiGianNhacNhoStr + ", " + ngayNhacNho.getTime() + ", " + lapLai + ", " + ngayKetThucLapMillis);

        LapKeHoach lapKeHoach = new LapKeHoach(currentUserId, tenKH, ngayBatDau.getTime(), ngayKetThuc.getTime(),
                moTa, thoiGianNhacNhoStr, ngayNhacNho.getTime(), lapLai, ngayKetThucLapMillis);

        viewModel.insertKeHoach(lapKeHoach);
        Toast.makeText(getActivity(), "Lưu thành công", Toast.LENGTH_SHORT).show();
        resetTextFields();
        clickCancel(view);

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


//    private void ThamChieu(View view) {
//        btnSaveKH = view.findViewById(R.id.btnSaveKH);
//        editTenKH = view.findViewById(R.id.editTenKH);
//        editMoTa = view.findViewById(R.id.editMoTa);
//        editStarKH = view.findViewById(R.id.editStarKH);
//        editEndKH = view.findViewById(R.id.editEndKH);
//        editTGNN = view.findViewById(R.id.editTGNN);
//        editNgayNN = view.findViewById(R.id.editNgayNN);
//        editNgayKT = view.findViewById(R.id.editNgayKT);
//        spinnerLapLai = view.findViewById(R.id.spinnerLapLai);
//        textView26 = view.findViewById(R.id.textView26);
//        btnHuy = view.findViewById(R.id.btnHuy);
//    }

    private void clickCancel(View v) {
        // Quay lại màn hình trước đó
        NavController navController = Navigation.findNavController(v);
        navController.popBackStack(); // Quay lại fragment trước

    }
    private void resetTextFields() {
        editTenKH.setText("");
        editMoTa.setText("");
        editStarKH.setText("");
        editEndKH.setText("");
        editTGNN.setText("");
        editNgayNN.setText("");
        editNgayKT.setText("");
        spinnerLapLai.setSelection(0); // Nếu muốn chọn lại dòng đầu tiên
    }

}