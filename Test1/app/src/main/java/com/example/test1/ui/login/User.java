package com.unetikhoaluan2025.timabk.ui.login;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Entity(tableName = "user")

public class User {


    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user")
    private String user;

    private String pass;
    private String hoten;
    private String pass2;
    private String pin;
    private String email;
    private String sdt;
    private String gt;

    @TypeConverters(Converters.class)
    private Date ngaysinh;

    public User(String user, String pass, String hoten, String pass2, String pin) {
        this.user = user;
        this.pass = pass;
        this.hoten = hoten;
        this.pass2 = pass2;
        this.pin = pin;
        this.ngaysinh = null;
    }
    @Ignore
    public User( String user, String hoten, String gt, String sdt, String email,Date ngaysinh) {
        this.ngaysinh = ngaysinh != null ? ngaysinh : new Date();
        this.gt = gt;
        this.sdt = sdt;
        this.email = email;
        this.hoten = hoten;
        this.user = user;

    }



    public String getHoten() {
        return hoten;
    }

    public void setHoten(String hoten) {
        this.hoten = hoten;
    }

    public String getPass2() {
        return pass2;
    }

    public void setPass2(String pass2) {
        this.pass2 = pass2;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getGt() {
        return gt;
    }

    public void setGt(String gt) {
        this.gt = gt;
    }



    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public Date getNgaysinh() {
        return ngaysinh;
    }

    public void setNgaysinh(Date ngaysinh) { this.ngaysinh = ngaysinh; }
    public String getNgaysinhFormat() {
        if (ngaysinh == null) {
            return "Chưa cập nhật";
        }
        try {
            // Log giá trị ngày sinh ban đầu
            Log.d("DEBUG", "Ngày sinh từ DB: " + ngaysinh.toString());

            // Sử dụng định dạng dd-MM-yyyy thay vì dd/MM/yyyy
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            // Set múi giờ chính xác của Việt Nam (Asia/Ho_Chi_Minh)
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

            // Định dạng và trả về ngày sinh
            String formattedDate = sdf.format(ngaysinh);

            // Log giá trị ngày sinh đã được định dạng
            Log.d("DEBUG", "Ngày sinh đã định dạng: " + formattedDate);

            return formattedDate;
        } catch (Exception e) {
            Log.e("DEBUG", "Lỗi format ngày sinh: " + e.getMessage());
            return "Ngày không hợp lệ";
        }
    }
}
