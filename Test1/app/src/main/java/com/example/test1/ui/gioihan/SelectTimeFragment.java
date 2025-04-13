package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;

import java.util.List;

public class SelectTimeFragment extends Fragment {

    private List<SelectedAppInfo> selectedApps;  // Danh sách ứng dụng đã chọn
    private LinearLayout layoutLimitTime;
    private NumberPicker numberPickerTime;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gioihan_fragment_gioihan_selecttime, container, false);
        CheckBox checkLimitUsage = view.findViewById(R.id.checkLimitUsage);
        layoutLimitTime = view.findViewById(R.id.layoutLimitTime);
        numberPickerTime = view.findViewById(R.id.numberPickerTime);
        // Nhận danh sách ứng dụng đã chọn từ Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            selectedApps = bundle.getParcelableArrayList("selectedApps");

        }

        checkLimitUsage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutLimitTime.setVisibility(View.VISIBLE);  // Hiển thị phần giới hạn thời gian
            } else {
                layoutLimitTime.setVisibility(View.GONE);  // Ẩn phần giới hạn thời gian
            }
        });
        numberPickerTime.setMinValue(1);
        numberPickerTime.setMaxValue(180);

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            // Lấy giá trị từ NumberPicker
            int selectedTime = numberPickerTime.getValue();
            // Lưu vào CSDL (tạo AppLimit entity và lưu nó)
            saveAppsWithTimeToDatabase(selectedApps, selectedTime);
        });

        return view;
    }

    private void saveAppsWithTimeToDatabase(List<SelectedAppInfo> selectedApps, int selectedTime) {
        // Giả sử bạn đã tạo AppDatabase và AppLimitDao
        AppDatabase appDatabase = AppDatabase.getInstance(getContext());

        // Thực hiện lưu dữ liệu trong một thread khác, tránh gây block UI thread
        new Thread(() -> {
            for (SelectedAppInfo selectedApp : selectedApps) {
                if (selectedApp.isSelected()) {
                    // Kiểm tra xem ứng dụng đã tồn tại chưa
                    AppLimit existingApp = appDatabase.appLimitDao().getAppByName(selectedApp.getAppName());

                    if (existingApp != null) {
                        // Nếu ứng dụng đã tồn tại, cập nhật thời gian giới hạn
                        existingApp.setLimitTime(selectedTime);
                        appDatabase.appLimitDao().update(existingApp);
                    } else {
                        // Nếu chưa có, thêm mới
                        AppLimit appLimit = new AppLimit(selectedApp.getAppName(),selectedApp.getPackageName(), selectedTime, true);
                        appDatabase.appLimitDao().insert(appLimit);
                    }
                }
            }

            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Dữ liệu đã được cập nhật", Toast.LENGTH_SHORT).show();
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.nav_gioihan);
            });

        }).start();
    }
}
