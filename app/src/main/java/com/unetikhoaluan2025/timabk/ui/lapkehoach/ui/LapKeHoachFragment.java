package com.unetikhoaluan2025.timabk.ui.lapkehoach.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.adapter.LapKeHoachAdapter;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.viewmodel.LapKeHoachViewModel;

import java.util.ArrayList;
import java.util.List;

public class LapKeHoachFragment extends Fragment implements LapKeHoachAdapter.OnKeHoachListener {

    private LapKeHoachViewModel mViewModel;
    private Button btnTimKiem, btnHTKH, btnThem;
    private TextView txtSoKH;
    private EditText editSearch;
    private RecyclerView recyclerViewKeHoach;
    private LapKeHoachAdapter adapter;
    private String currentUserId;
    private boolean isShowingCompleted = false;
    private List<LapKeHoach> fullList = new ArrayList<>();

    public static LapKeHoachFragment newInstance() {
        return new LapKeHoachFragment();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lapkehoach_fragment_lapkehoach, container, false);

        // Khởi tạo các tham chiếu view
        thamchieu(view);

        // Lấy ID người dùng hiện tại từ SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = preferences.getString("user", "");

        // Khởi tạo ViewModel
        mViewModel = new ViewModelProvider(this).get(LapKeHoachViewModel.class);

        // Thiết lập RecyclerView với adapter mới
        setupRecyclerView();

        // Thiết lập các sự kiện click
        setupClickListeners(view);

        // Quan sát dữ liệu từ ViewModel
        observeViewModel();

        hideKeyboardWhenTouchOutside(view);

        mViewModel.getKeHoachCount().observe(getViewLifecycleOwner(), count -> {
            // Hiển thị số lượng kế hoạch
            txtSoKH.setText("Bạn có " + count + " kế hoạch.");
            Log.d("Số kế hoạch", String.valueOf(count));
        });

        return view;
    }


    private void setupClickListeners(View view) {
        // Nút thêm kế hoạch mới
        btnThem.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.nav_themkehoach));

        // Nút tìm kiếm kế hoạch
        btnTimKiem.setOnClickListener(v -> {
            String currentText = btnTimKiem.getText().toString();

            if (currentText.equals("Tìm kiếm")) {
                String keyword = editSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    searchKeHoach(keyword);
                    btnTimKiem.setText("Hủy"); // Đổi nút thành Hủy
                } else {
                    Toast.makeText(requireContext(), "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Nếu đang là nút "Hủy"
                editSearch.setText(""); // Xóa ô tìm kiếm
                btnTimKiem.setText("Tìm kiếm"); // Đổi lại nút
                // Hiển thị lại toàn bộ kế hoạch
                mViewModel.getAllKeHoachByUserId(currentUserId).observe(getViewLifecycleOwner(),
                        lapKeHoachList -> adapter.submitList(lapKeHoachList));
            }
        });

        // Nút hiển thị kế hoạch hoàn thành
        btnHTKH.setOnClickListener(v -> {
            if (!isShowingCompleted) {
                // Đang xem tất cả → Chuyển sang chỉ xem hoàn thành
                mViewModel.getCompletedKeHoachByUserId(currentUserId).observe(getViewLifecycleOwner(),
                        list -> {
                            adapter.submitList(list);
                            btnHTKH.setText("Tất cả");
                            isShowingCompleted = true;
                        });
            } else {
                // Đang xem hoàn thành → Quay lại danh sách tất cả
                mViewModel.getAllKeHoachByUserId(currentUserId).observe(getViewLifecycleOwner(),
                        list -> {
                            adapter.submitList(list);
                            btnHTKH.setText("Hoàn thành");
                            isShowingCompleted = false;
                        });
            }
        });
    }

    private void searchKeHoach(String keyword) {
        mViewModel.searchKeHoachByName(currentUserId, keyword).observe(getViewLifecycleOwner(),
                lapKeHoachList -> adapter.submitList(lapKeHoachList));
    }

    private void setupRecyclerView() {
        // Thiết lập RecyclerView với Layout Manager
        recyclerViewKeHoach.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo adapter mới với interface listener
        adapter = new LapKeHoachAdapter(this);

        // Gán adapter cho RecyclerView
        recyclerViewKeHoach.setAdapter(adapter);
    }

    private void observeViewModel() {
        mViewModel.getAllKeHoachByUserId(currentUserId).observe(getViewLifecycleOwner(),
                lapKeHoachList -> {
                    fullList = lapKeHoachList; // lưu lại danh sách đầy đủ
                    adapter.submitList(fullList);
                });

        mViewModel.getOperationMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onKeHoachClick(LapKeHoach lapKeHoach) {
        // Xử lý khi người dùng click vào một kế hoạch
        // Ví dụ: Mở màn hình chi tiết kế hoạch
        Bundle bundle = new Bundle();
        bundle.putInt("keHoachId", lapKeHoach.getId());
        Navigation.findNavController(getView()).navigate(R.id.nav_suakehoach, bundle);
    }

    @Override
    public void onKeHoachStatusChange(LapKeHoach lapKeHoach, boolean isCompleted) {
        // Cập nhật trạng thái kế hoạch
        if (isCompleted) {
            mViewModel.markKeHoachAsCompleted(lapKeHoach.getId());
        } else {
            mViewModel.markKeHoachAsIncomplete(lapKeHoach.getId());
        }
    }

    @Override
    public void onKeHoachDeleteClick(LapKeHoach lapKeHoach) {
        // Xóa kế hoạch
        mViewModel.deleteKeHoach(lapKeHoach);
    }

    private void thamchieu(View view) {
        btnHTKH = view.findViewById(R.id.btnHTKH);
        btnThem = view.findViewById(R.id.btnThemKH);
        btnTimKiem = view.findViewById(R.id.btnSeachKH);
        editSearch = view.findViewById(R.id.editSeachKH);
        recyclerViewKeHoach = view.findViewById(R.id.recyclerViewKeHoach);
        txtSoKH = view.findViewById(R.id.SoKH);
    }
    private void hideKeyboardWhenTouchOutside(View view) {
        // Nếu không phải EditText thì set listener
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                // Ẩn bàn phím
                View currentFocus = requireActivity().getCurrentFocus();
                if (currentFocus != null) {
                    InputMethodManager imm = (InputMethodManager) requireActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    currentFocus.clearFocus(); // clear focus để ẩn bàn phím
                }
                return false;
            });
        }

        // Nếu là ViewGroup, áp dụng đệ quy cho tất cả các view con
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                hideKeyboardWhenTouchOutside(innerView);
            }
        }
    }
}