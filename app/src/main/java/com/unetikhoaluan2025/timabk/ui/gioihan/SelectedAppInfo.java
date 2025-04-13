package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SelectedAppInfo implements Parcelable {
    private String appName;
    String packageName;
    private boolean isSelected;  // Trạng thái checkbox

    // Constructor
    public SelectedAppInfo(String appName,String packageName, boolean isSelected) {
        this.appName = appName;
        this.packageName = packageName;
        this.isSelected = isSelected;
    }

    protected SelectedAppInfo(Parcel in) {
        appName = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<SelectedAppInfo> CREATOR = new Creator<SelectedAppInfo>() {
        @Override
        public SelectedAppInfo createFromParcel(Parcel in) {
            return new SelectedAppInfo(in);
        }

        @Override
        public SelectedAppInfo[] newArray(int size) {
            return new SelectedAppInfo[size];
        }
    };

    // Getter và Setter
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
