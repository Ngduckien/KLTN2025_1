package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unetikhoaluan2025.timabk.R;

import java.util.ArrayList;
import java.util.List;

public class SelectAppAdapter extends RecyclerView.Adapter<SelectAppAdapter.ViewHolder> {
    private final List<AppInfo> appList;
    private final List<SelectedAppInfo> selectedApps = new ArrayList<>();

    public SelectAppAdapter(List<AppInfo> appList) {
        this.appList = appList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gioihan_item_gioihan_selcapp, parent, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.txtAppName.setText(app.getAppName());
        holder.imgAppIcon.setImageDrawable(app.getIcon());

        // Đối tượng SelectedAppInfo
        SelectedAppInfo selectedAppInfo = new SelectedAppInfo(app.getAppName(),app.getPackageName(), false);  // Mặc định chưa chọn

        // Lắng nghe sự kiện thay đổi checkbox
        holder.checkBoxApp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedAppInfo.setSelected(isChecked);  // Cập nhật trạng thái của checkbox
            if (isChecked) {
                selectedApps.add(selectedAppInfo);  // Thêm vào danh sách khi checkbox được chọn
            } else {
                selectedApps.remove(selectedAppInfo);  // Xóa khỏi danh sách khi checkbox không được chọn
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public List<SelectedAppInfo> getSelectedApps() {
        return selectedApps;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAppIcon;
        TextView txtAppName;
        CheckBox checkBoxApp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAppIcon = itemView.findViewById(R.id.imgAppIcon);
            txtAppName = itemView.findViewById(R.id.txtAppName);
            checkBoxApp = itemView.findViewById(R.id.checkBoxApp);
        }
    }
}
