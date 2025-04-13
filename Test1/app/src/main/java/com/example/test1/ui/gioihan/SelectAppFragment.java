package com.unetikhoaluan2025.timabk.ui.gioihan;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.unetikhoaluan2025.timabk.R;

import java.util.ArrayList;
import java.util.List;

public class SelectAppFragment extends Fragment  {
    private SelectAppAdapter adapter;

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gioihan_fragment_gioihan_selectapp, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewApps);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy danh sách ứng dụng và gán vào adapter
        List<AppInfo> installedApps = getInstalledApps();
        adapter = new SelectAppAdapter(installedApps);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        btnConfirm.setOnClickListener(v -> {
            List<SelectedAppInfo> selectedApps = adapter.getSelectedApps();

            // Kiểm tra xem có ứng dụng nào được chọn không
            if (selectedApps.isEmpty()) {
                // Nếu không có ứng dụng nào được chọn, hiển thị thông báo hoặc xử lý logic khác
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một ứng dụng.", Toast.LENGTH_SHORT).show();
            } else {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("selectedApps", new ArrayList<>(selectedApps));
                Navigation.findNavController(v).navigate(R.id.nav_gioihan_selecttime, bundle);
            }
        });

        return view;
    }

    private List<AppInfo> getInstalledApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = requireContext().getPackageManager();

        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            String packageName = packageInfo.packageName;
            ApplicationInfo appInfo = packageInfo.applicationInfo;

            // Giữ lại Google Play Services & Google Play Store
            if ( packageName.equals("com.android.vending")) {
                // Không loại bỏ Google Play Services & Play Store
            } else {
                // Loại bỏ ứng dụng hệ thống (trừ khi đã cập nhật từ Google Play)
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 &&
                        (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                    if (!isDefaultBrowser(packageName)) {
                        continue;
                    }

                }

                // Loại bỏ ứng dụng của nhà sản xuất
                if (packageName.startsWith("com.samsung.") ||
                        packageName.startsWith("com.mi.") ||
                        packageName.startsWith("com.oppo.") ||
                        packageName.startsWith("com.vivo.") ||
                        packageName.startsWith("com.huawei.") ||
                        packageName.startsWith("com.oneplus.")) {
                    continue;
                }
            }

            // Lấy thông tin ứng dụng
            String appName = appInfo.loadLabel(pm).toString();
            Drawable icon = appInfo.loadIcon(pm);

            appList.add(new AppInfo(appName, packageName, icon));
        }

        Log.d("AppList", "Tổng số ứng dụng sau khi lọc: " + appList.size());
        return appList;
    }
    private boolean isDefaultBrowser(String packageName) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        ResolveInfo resolveInfo = requireContext().getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && resolveInfo.activityInfo.packageName.equals(packageName);
    }
}