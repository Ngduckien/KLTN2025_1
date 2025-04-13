package com.unetikhoaluan2025.timabk.ui.gioihan;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_limits")
public class AppLimit {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String appName;

    @NonNull
    public String packageName;

    public int limitTime; // Giới hạn thời gian (phút)

    public boolean isSelected; // Ứng dụng đã chọn hay chưa

    public AppLimit(@NonNull String appName, @NonNull String packageName, int limitTime, boolean isSelected) {
        this.appName = appName;
        this.packageName = packageName;
        this.limitTime = limitTime;
        this.isSelected = isSelected;
    }

    @NonNull
    public String getAppName() {
        return appName;
    }

    public void setAppName(@NonNull String appName) {
        this.appName = appName;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }

    public int getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }
}
