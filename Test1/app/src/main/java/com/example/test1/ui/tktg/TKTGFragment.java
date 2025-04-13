package com.unetikhoaluan2025.timabk.ui.tktg;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TKTGFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsageAdapter adapter;
    private TKTGViewModel viewModel;
    private EditText editTextDateFirst, editTextDateLast;
    private Button btnLoc, btnTang, btnGiam;
    private Calendar calendar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TKTGFragment() {
        super(R.layout.tktg_fragment_tktg);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Thamchieu(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsageAdapter(getContext(), null);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TKTGViewModel.class);
        viewModel.getUsageRecords().observe(getViewLifecycleOwner(), adapter::updateData);

        calendar = Calendar.getInstance();
        String todayDate = dateFormat.format(calendar.getTime());

        editTextDateFirst.setText(todayDate);
        editTextDateLast.setText(todayDate);

        editTextDateFirst.setOnClickListener(v -> showDatePickerDialog(editTextDateFirst));
        editTextDateLast.setOnClickListener(v -> showDatePickerDialog(editTextDateLast));

        btnLoc.setOnClickListener(v -> clickLoc());
        btnTang.setOnClickListener(v -> viewModel.sortUsageRecords(true));
        btnGiam.setOnClickListener(v -> viewModel.sortUsageRecords(false));

        // ✅ Tự động hiển thị dữ liệu hôm nay khi mở giao diện
        loadTodayUsage();
    }

    private void loadTodayUsage() {
        long startTime, endTime;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        startTime = today.getTimeInMillis();

        endTime = System.currentTimeMillis(); // Lấy thời gian hiện tại

        Log.d("FILTER", "Tự động tải dữ liệu hôm nay từ: " + new Date(startTime) + " đến " + new Date(endTime));
        viewModel.loadUsageRecordsByDate(startTime, endTime);
    }

    private void clickLoc() {
        String startDateStr = editTextDateFirst.getText().toString().trim();
        String endDateStr = editTextDateLast.getText().toString().trim();

        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày trước khi lọc!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            if (startDate == null || endDate == null) {
                Toast.makeText(getContext(), "Ngày không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (startDate.after(endDate)) {
                Toast.makeText(getContext(), "Ngày bắt đầu phải trước ngày kết thúc!", Toast.LENGTH_SHORT).show();
                return;
            }

            long startTime = startDate.getTime();
            long endTime;

            Calendar today = Calendar.getInstance();
            String todayStr = dateFormat.format(today.getTime());

            if (endDateStr.equals(todayStr)) {
                endTime = System.currentTimeMillis(); // Nếu chọn hôm nay, lấy thời gian hiện tại
            } else {
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(endDate);
                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                endCalendar.set(Calendar.MINUTE, 59);
                endCalendar.set(Calendar.SECOND, 59);
                endTime = endCalendar.getTimeInMillis();
            }

            Log.d("FILTER", "Lọc dữ liệu từ: " + new Date(startTime) + " đến " + new Date(endTime));
            viewModel.loadUsageRecordsByDate(startTime, endTime);

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Định dạng ngày không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    private void Thamchieu(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        editTextDateFirst = view.findViewById(R.id.editTextDateFirst);
        editTextDateLast = view.findViewById(R.id.editTextDateLast);
        btnLoc = view.findViewById(R.id.btnLoc);
        btnTang = view.findViewById(R.id.btnTang);
        btnGiam = view.findViewById(R.id.btnGiam);
    }

    private void showDatePickerDialog(EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = dateFormat.format(calendar.getTime());
                    editText.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}

