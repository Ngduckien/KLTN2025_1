package com.unetikhoaluan2025.timabk.ui.lapkehoach.ui;

import static com.unetikhoaluan2025.timabk.ui.lapkehoach.data.Converter.formatDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.Converter;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.viewmodel.LapKeHoachViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Sua_Detail_Fragment extends Fragment {

    private EditText editTenKH, editMoTa, editStarKH, editEndKH,
            editTGNN, editNgayNN, editNgayKT;
    private Spinner spinnerLapLai;
    private Button btnSave, btnCancel;

    private LapKeHoachViewModel viewModel;

    public static Sua_Detail_Fragment newInstance() {
        return new Sua_Detail_Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lapkehoach_fragment_sua_detail_kehoach, container, false);

        // Tham chiếu các view
        editTenKH = view.findViewById(R.id.editTenKH);
        editMoTa = view.findViewById(R.id.editMoTa);
        editStarKH = view.findViewById(R.id.editStarKH);
        editEndKH = view.findViewById(R.id.editEndKH);
        editTGNN = view.findViewById(R.id.editTGNN);
        editNgayNN = view.findViewById(R.id.editNgayNN);
        editNgayKT = view.findViewById(R.id.editNgayKT);
        spinnerLapLai = view.findViewById(R.id.spinnerLapLai);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.lapLaiOptions,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLapLai.setAdapter(adapter);

        // Nhận ID kế hoạch từ arguments
        int keHoachId = getArguments() != null ? getArguments().getInt("keHoachId", -1) : -1;

        if (keHoachId == -1) {
            Toast.makeText(getContext(), "Không tìm thấy kế hoạch", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(LapKeHoachViewModel.class);

        // Lấy kế hoạch từ ViewModel
        viewModel.getKeHoachById(keHoachId).observe(getViewLifecycleOwner(), keHoach -> {
            if (keHoach != null) {
                // Gán dữ liệu lên giao diện
                editTenKH.setText(keHoach.getTenKeHoach());
                editMoTa.setText(keHoach.getMoTa());
                editStarKH.setText(formatDate(keHoach.getNgayBatDau()));
                editEndKH.setText(formatDate(keHoach.getNgayKetThuc()));
                editTGNN.setText(keHoach.getThoiGianNhacNho());
                editNgayNN.setText(formatDate(keHoach.getNgayNhacNho()));
                editNgayKT.setText(formatDate(keHoach.getNgayKetThucLap()));
                // Chọn đúng mục Spinner (giả sử là text)

                String lapLai = keHoach.getLapLai();
                Log.d("text",lapLai);
                for (int i = 0; i < spinnerLapLai.getCount(); i++) {
                    if (spinnerLapLai.getItemAtPosition(i).toString().equalsIgnoreCase(lapLai)) {
                        spinnerLapLai.setSelection(i);
                        break;
                    }
                }
            } else {
                Toast.makeText(getContext(), "Không tìm thấy dữ liệu kế hoạch", Toast.LENGTH_SHORT).show();
            }
        });

        // Bắt sự kiện nút Cancel (đóng fragment hoặc pop backstack)
        btnCancel.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });

        // Bắt sự kiện nút Save (bạn có thể viết thêm phần update ở đây)
        btnSave.setOnClickListener(v -> clickUpdate());

        return view;
    }

    private void clickUpdate() {
        String tenKH = editTenKH.getText().toString().trim();
        String moTa = editMoTa.getText().toString().trim();
        String ngayBD = editStarKH.getText().toString().trim();
        String ngayKT = editEndKH.getText().toString().trim();
        String thoiGianNN = editTGNN.getText().toString().trim();
        String ngayNN = editNgayNN.getText().toString().trim();
        String ngayKetThucLap = editNgayKT.getText().toString().trim();
        String lapLai = spinnerLapLai.getSelectedItem().toString();
        SharedPreferences preferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserId = preferences.getString("user", "");

        if (tenKH.isEmpty() || ngayBD.isEmpty() || ngayKT.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long ngayBatDau = Converter.parseDate(ngayBD).getTime();
            long ngayKetThuc = Converter.parseDate(ngayKT).getTime();
            Long ngayNhacNho = ngayNN.isEmpty() ? null : Converter.parseDate(ngayNN).getTime();
            Long ngayLapKT = ngayKetThucLap.isEmpty() ? null : Converter.parseDate(ngayKetThucLap).getTime();

            int keHoachId = getArguments() != null ? getArguments().getInt("keHoachId", -1) : -1;
            if (keHoachId == -1) {
                Toast.makeText(getContext(), "Không tìm thấy ID kế hoạch", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy userId từ SharedPreferences

            if (currentUserId.isEmpty()) {
                Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            LapKeHoach keHoach = new LapKeHoach(
                    currentUserId,
                    tenKH,
                    ngayBatDau,
                    ngayKetThuc,
                    moTa,
                    thoiGianNN,
                    ngayNhacNho,
                    lapLai,
                    ngayLapKT
            );
            keHoach.setId(keHoachId); // cần thiết để Room biết là update chứ không phải insert

            viewModel.updateKeHoach(keHoach);
            Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();

            Navigation.findNavController(requireView()).popBackStack();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi định dạng ngày", Toast.LENGTH_SHORT).show();
        }
    }
}
