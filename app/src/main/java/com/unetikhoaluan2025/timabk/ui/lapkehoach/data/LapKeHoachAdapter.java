package com.unetikhoaluan2025.timabk.ui.lapkehoach.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LapKeHoachAdapter extends ListAdapter<LapKeHoach, LapKeHoachAdapter.KeHoachViewHolder> {

    private final OnKeHoachListener onKeHoachListener;

    public interface OnKeHoachListener {
        void onKeHoachClick(LapKeHoach lapKeHoach);
        void onKeHoachStatusChange(LapKeHoach lapKeHoach, boolean isCompleted);
        void onKeHoachDeleteClick(LapKeHoach lapKeHoach);
    }

    // DiffUtil để tối ưu hiệu suất cập nhật danh sách
    private static final DiffUtil.ItemCallback<LapKeHoach> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<LapKeHoach>() {
                @Override
                public boolean areItemsTheSame(@NonNull LapKeHoach oldItem, @NonNull LapKeHoach newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull LapKeHoach oldItem, @NonNull LapKeHoach newItem) {
                    return oldItem.getTenKeHoach().equals(newItem.getTenKeHoach()) &&
                            oldItem.getMoTa().equals(newItem.getMoTa()) &&
                            oldItem.getNgayBatDau() == newItem.getNgayBatDau() &&
                            oldItem.getNgayKetThuc() == newItem.getNgayKetThuc() &&
                            oldItem.isCompleted() == newItem.isCompleted();
                }
            };

    public LapKeHoachAdapter(OnKeHoachListener onKeHoachListener) {
        super(DIFF_CALLBACK);
        this.onKeHoachListener = onKeHoachListener;
    }

    @NonNull
    @Override
    public KeHoachViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lapkehoach_item_kehoach, parent, false);
        return new KeHoachViewHolder(view, onKeHoachListener);
    }

    @Override
    public void onBindViewHolder(@NonNull KeHoachViewHolder holder, int position) {
        LapKeHoach keHoach = getItem(position);
        holder.bind(keHoach);
    }

    public class KeHoachViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTenKeHoach;
        private final TextView tvMoTa;
        private final TextView tvNgayBatDau;
        private final TextView tvNgayKetThuc;
        private final CheckBox cbTrangThai;
        private final ImageButton btnDelete;
        private final OnKeHoachListener listener;
        private LapKeHoach currentKeHoach;

        public KeHoachViewHolder(@NonNull View itemView, OnKeHoachListener listener) {
            super(itemView);
            tvTenKeHoach = itemView.findViewById(R.id.tv_ten_ke_hoach);
            tvMoTa = itemView.findViewById(R.id.tv_mo_ta);
            tvNgayBatDau = itemView.findViewById(R.id.tv_ngay_bat_dau);
            tvNgayKetThuc = itemView.findViewById(R.id.tv_ngay_ket_thuc);
            cbTrangThai = itemView.findViewById(R.id.cb_trang_thai);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            this.listener = listener;

            itemView.setOnClickListener(v -> {
                if (currentKeHoach != null && listener != null) {
                    listener.onKeHoachClick(currentKeHoach);
                }
            });

            cbTrangThai.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (currentKeHoach != null && listener != null && buttonView.isPressed()) {
                    listener.onKeHoachStatusChange(currentKeHoach, isChecked);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (currentKeHoach != null && listener != null) {
                    listener.onKeHoachDeleteClick(currentKeHoach);
                }
            });
        }

        public void bind(LapKeHoach keHoach) {
            this.currentKeHoach = keHoach;
            tvTenKeHoach.setText(keHoach.getTenKeHoach());
            tvMoTa.setText(keHoach.getMoTa());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            tvNgayBatDau.setText(dateFormat.format(new Date(keHoach.getNgayBatDau())));
            tvNgayKetThuc.setText(dateFormat.format(new Date(keHoach.getNgayKetThuc())));

            // Đảm bảo không kích hoạt listener khi cập nhật UI
            cbTrangThai.setOnCheckedChangeListener(null);
            cbTrangThai.setChecked(keHoach.isCompleted());
            cbTrangThai.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && buttonView.isPressed()) {
                    listener.onKeHoachStatusChange(keHoach, isChecked);
                }
            });
        }
    }
}