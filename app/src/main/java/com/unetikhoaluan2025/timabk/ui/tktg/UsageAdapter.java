package com.unetikhoaluan2025.timabk.ui.tktg;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UsageAdapter extends RecyclerView.Adapter<UsageAdapter.ViewHolder> {
    private List<UsageRecord> usageList;
    private final Context context;

    public UsageAdapter(Context context, List<UsageRecord> usageList) {
        this.context = context;
        this.usageList = usageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tktg_item_usage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsageRecord record = usageList.get(position);
        long totalSeconds = record.getDurationInSeconds();
        long hours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;

        holder.appName.setText("Ứng dụng: " + record.getAppName());
        holder.usageTime.setText(String.format("Thời gian sử dụng: %d giờ %d phút", hours, minutes));

        // Thêm sự kiện click
        holder.itemView.setOnClickListener(v -> showSessionDetailsDialog(record));
    }

    private void showSessionDetailsDialog(UsageRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_session_detail, null);
        builder.setView(dialogView);

        // Ánh xạ view
        TextView textAppName = dialogView.findViewById(R.id.textDialogAppName);
        TextView textDialogDate = dialogView.findViewById(R.id.textDialogDate);
        TextView textTotalTime = dialogView.findViewById(R.id.textDialogTotalTime);
        TextView textSessionCount = dialogView.findViewById(R.id.textDialogSessionCount);
        RecyclerView recyclerViewSessions = dialogView.findViewById(R.id.recyclerViewSessions);

        // Thiết lập thông tin cơ bản
        textAppName.setText(record.getAppName());

        // Hiển thị ngày tháng năm
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        textDialogDate.setText("Ngày: " + dateFormat.format(new Date(record.getFirstTimestamp())));

        long totalSeconds = record.getDurationInSeconds();
        long hours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
        textTotalTime.setText(String.format("Tổng thời gian: %d giờ %d phút", hours, minutes));

        int sessionCount = record.getSessions().size();
        textSessionCount.setText(String.format("Số lần sử dụng: %d", sessionCount));

        // Sắp xếp các phiên theo thời gian (mới nhất lên đầu)
        List<Session> sortedSessions = new ArrayList<>(record.getSessions());
        Collections.sort(sortedSessions, (s1, s2) ->
                Long.compare(s2.getStartTime().getTime(), s1.getStartTime().getTime()));

        // Thiết lập RecyclerView cho các phiên
        recyclerViewSessions.setLayoutManager(new LinearLayoutManager(context));
        SessionAdapter sessionAdapter = new SessionAdapter(context, sortedSessions);
        recyclerViewSessions.setAdapter(sessionAdapter);

        builder.setPositiveButton("Đóng", null);
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return usageList != null ? usageList.size() : 0;
    }

    public void updateData(List<UsageRecord> newData) {
        this.usageList = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName, usageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.textAppName);
            usageTime = itemView.findViewById(R.id.textUsageTime);
        }
    }
}

