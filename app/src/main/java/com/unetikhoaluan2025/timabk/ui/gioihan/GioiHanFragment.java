package com.unetikhoaluan2025.timabk.ui.gioihan;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.notifications.ReminderScheduler;

import java.util.ArrayList;

public class GioiHanFragment extends Fragment {
    private GioiHanViewModel gioiHanViewModel;
    private RecyclerView recyclerView;
    private TextView txtThongBao;
    private Button btnThem;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gioihan_fragment_gioihan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gioiHanViewModel = new ViewModelProvider(requireActivity()).get(GioiHanViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerViewApp);
        txtThongBao = view.findViewById(R.id.txtTieuDeGioiHan);
        btnThem = view.findViewById(R.id.btnThem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Khởi tạo adapter với sự kiện click
        AppAdapter appAdapter = new AppAdapter(new ArrayList<>(), app -> {
            Bundle bundle = new Bundle();
            bundle.putString("appName", app.appName);
            bundle.putInt("limitTime", app.limitTime);
            Navigation.findNavController(view).navigate(R.id.nav_gioihan_applimit_detail, bundle);
        });
        recyclerView.setAdapter(appAdapter);
        loadData(appAdapter);



        // Nhấn nút "Thêm giới hạn" để mở màn hình chọn ứng dụng
        btnThem.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_gioihan_selectapp)
        );
        onResume();

    }

    private void loadData(AppAdapter appAdapter) {
        // Đăng ký observer để quan sát dữ liệu từ ViewModel
        gioiHanViewModel.getLimitedApps().observe(getViewLifecycleOwner(), appLimits -> {
            Log.d("GioiHanFragment", "Dữ liệu ứng dụng giới hạn: " + appLimits.size());  // Log kích thước danh sách

            if (appLimits == null || appLimits.isEmpty()) {
                // Nếu không có ứng dụng nào, hiển thị thông báo
                txtThongBao.setVisibility(View.VISIBLE);
                txtThongBao.setText("Chưa có ứng dụng nào được giới hạn");
                recyclerView.setVisibility(View.GONE);
            } else {
                // Nếu có dữ liệu, ẩn thông báo và cập nhật RecyclerView
                txtThongBao.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                // Cập nhật danh sách trong adapter
                appAdapter.updateList(appLimits);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        gioiHanViewModel.fetchLimitedApps();
        ReminderScheduler.scheduleReminders(requireContext());
    }

}
