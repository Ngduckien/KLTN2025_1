package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.unetikhoaluan2025.timabk.databinding.FragmentGioihanApplimitDetailBinding;


public class AppLimitDetailFragment extends Fragment {
    private FragmentGioihanApplimitDetailBinding binding;
    private GioiHanViewModel gioiHanViewModel;
    private String appName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGioihanApplimitDetailBinding.inflate(inflater, container, false);
        gioiHanViewModel = new ViewModelProvider(this).get(GioiHanViewModel.class);
        // Nhận dữ liệu từ Bundle
        if (getArguments() != null) {
           appName = getArguments().getString("appName", "Không xác định");
            int limitTime = getArguments().getInt("limitTime", 0);

            // Hiển thị dữ liệu lên TextView trong layout
            binding.nameApp.setText(appName);
                binding.numberPickerTime.setMinValue(0);
                binding.numberPickerTime.setMaxValue(180);
                binding.numberPickerTime.setValue(limitTime);
        }
        binding.btnDelApp.setOnClickListener(v ->  {
            gioiHanViewModel.removeAppLimit(appName);
            Navigation.findNavController(v).navigateUp();
        });
        binding.btnSaveApp.setOnClickListener(v -> {
                int newLimit = binding.numberPickerTime.getValue();
                gioiHanViewModel.updateAppLimit(appName, newLimit);
                Navigation.findNavController(v).navigateUp();
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}