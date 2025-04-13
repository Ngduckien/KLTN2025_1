package com.unetikhoaluan2025.timabk.ui.gioihan;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppLimit> appList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AppLimit app);
    }

    public AppAdapter(List<AppLimit> appList, OnItemClickListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gioihan_item_gioihan_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppLimit app = appList.get(position);
        holder.appName.setText(app.appName);
        holder.time.setText(holder.itemView.getContext().getString(R.string.limit_time_text, app.limitTime));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(app);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<AppLimit> newList) {
        appList.clear(); // Đảm bảo danh sách cũ được xóa sạch
        appList.addAll(newList); // Thêm tất cả ứng dụng mới
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName;
        TextView time;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.txtAppName);
            time = itemView.findViewById(R.id.txtTime);
        }
    }
}
