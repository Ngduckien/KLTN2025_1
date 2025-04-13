package com.unetikhoaluan2025.timabk.ui.gioihan;

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.drawable.Drawable;

public class AppInfo implements Parcelable {
    private String appName;
    private String packageName;
    private Drawable icon;

    public AppInfo(String appName, String packageName, Drawable icon) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
    }

    // Getter và Setter

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    // Phương thức để đọc từ Parcel
    protected AppInfo(Parcel in) {
        appName = in.readString();
        packageName = in.readString();
        icon = in.readParcelable(Drawable.class.getClassLoader());
    }

    // Phương thức để ghi vào Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeParcelable((Parcelable) icon, flags); // Ghi Drawable
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}
