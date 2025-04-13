package com.unetikhoaluan2025.timabk.ui.home;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.databinding.FragmentHomeBinding;
import com.unetikhoaluan2025.timabk.ui.login.User;
import com.unetikhoaluan2025.timabk.ui.login.UserDAO;

import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnThongKe.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            requestPinAndNavigate(R.id.nav_tktg, navController);
        });

        binding.btnTLGioiHan.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            requestPinAndNavigate(R.id.nav_gioihan, navController);
        });

        binding.btnLapKeHoach.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            requestPinAndNavigate(R.id.nav_lapkehoach, navController);
        });

        binding.btnGioiHanTK.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            requestPinAndNavigate(R.id.nav_gioihantk, navController);
        });


        return root;
    }

    private void requestPinAndNavigate(int fragmentId, NavController navController) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Sửa lỗi Context
        builder.setTitle("Nhập mã PIN để tiếp tục");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);
        // Lấy username từ SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        String currentUser = preferences.getString("user", ""); // Lấy username đã đăng nhập

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String enteredPin = input.getText().toString().trim();
            if (enteredPin.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mã PIN", Toast.LENGTH_SHORT).show();
                input.requestFocus();
                return;
            }
            else
            {
                if (currentUser.isEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Lấy mã PIN từ CSDL
                UserDAO userDAO = AppDatabase.getInstance(requireContext()).userDAO();
                Executors.newSingleThreadExecutor().execute(() -> {
                    User user = userDAO.getUserByUsername(currentUser);
                    if (user == null || user.getPin() == null) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Không tìm thấy mã PIN!", Toast.LENGTH_SHORT).show());

                        return;
                    }

                    String correctPin = user.getPin(); // Lấy mã PIN từ DB
                    requireActivity().runOnUiThread(() -> {
                        if (enteredPin.equals(correctPin)) {
                            navController.navigate(fragmentId);
                        } else {
                            Toast.makeText(requireContext(), "Mã PIN không đúng!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
