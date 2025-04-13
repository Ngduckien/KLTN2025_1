package com.unetikhoaluan2025.timabk.ui.tktg;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    private final Context context;
    private final List<Session> sessions;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    public SessionAdapter(Context context, List<Session> sessions) {
        this.context = context;
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);

        // Hiển thị số thứ tự phiên
        holder.sessionNumber.setText("Lần " + (position + 1));

        // Hiển thị ngày giờ bắt đầu và kết thúc (đầy đủ)
        holder.startTime.setText("Bắt đầu: " + dateTimeFormat.format(session.getStartTime()));
        holder.endTime.setText("Kết thúc: " + dateTimeFormat.format(session.getEndTime()));

        // Hiển thị thời lượng
        long duration = session.getDuration();
        long hours = TimeUnit.SECONDS.toHours(duration);
        long minutes = TimeUnit.SECONDS.toMinutes(duration) % 60;
        long seconds = duration % 60;
        holder.duration.setText(String.format("Thời lượng: %02d:%02d:%02d", hours, minutes, seconds));
        if (session.isExceededLimit()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF5F5"));
            holder.warningText.setVisibility(View.VISIBLE);
            holder.warningText.setText("⚠ Đã vượt quá thời gian cho phép");
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.warningText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sessionNumber, startTime, endTime, duration, warningText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sessionNumber = itemView.findViewById(R.id.textSessionNumber);
            startTime = itemView.findViewById(R.id.textStartTime);
            endTime = itemView.findViewById(R.id.textEndTime);
            duration = itemView.findViewById(R.id.textDuration);
            warningText = itemView.findViewById(R.id.textWarning);
        }
    }
}
