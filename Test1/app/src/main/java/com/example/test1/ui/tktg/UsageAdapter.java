package com.unetikhoaluan2025.timabk.ui.tktg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;

import java.util.List;
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
