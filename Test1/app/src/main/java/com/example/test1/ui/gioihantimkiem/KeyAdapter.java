package com.unetikhoaluan2025.timabk.ui.gioihantimkiem;

import android.app.AlertDialog;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;

import java.util.List;
import java.util.concurrent.Executors;
import android.os.Handler;

public class KeyAdapter extends ListAdapter<Key, KeyAdapter.KeyViewHolder> {
    private final KeyDAO keyDAO;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Key key);
    }
    public KeyAdapter(KeyDAO keyDAO, OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.keyDAO = keyDAO;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Key> DIFF_CALLBACK = new DiffUtil.ItemCallback<Key>() {
        @Override
        public boolean areItemsTheSame(@NonNull Key oldItem, @NonNull Key newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Key oldItem, @NonNull Key newItem) {
            return oldItem.getKeyword().equals(newItem.getKeyword());
        }
    };

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ghtk_item_ghtk_key, parent, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        Key key = getItem(position);
        holder.txtKeyword.setText(key.getKeyword());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(key);
            }
        });

        holder.btnDel.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc chắn muốn xoá từ khóa này không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            keyDAO.delete(key);

                            // Lấy danh sách mới từ database
                            List<Key> updatedList = keyDAO.getAllKeywords();

                            // Cập nhật danh sách trên UI
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(v.getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                                submitList(updatedList);
                            });
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    static class KeyViewHolder extends RecyclerView.ViewHolder {
        TextView txtKeyword;
        Button btnDel;

        public KeyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtKeyword = itemView.findViewById(R.id.tvKeyword);
            btnDel = itemView.findViewById(R.id.btnDelete);
        }
    }
    public void updateList(List<Key> newList) {
        submitList(null); // Reset danh sách trước để kích hoạt DiffUtil
        submitList(newList);
    }
}
