package com.unetikhoaluan2025.timabk.ui.login;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")

public class User {


    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user")
    private String user;

    private String pass;
    private String hoten;
    private String pin;
    private String email;
    private String sdt;
    private String gt;

    private long ngaysinh;

    public User(String user, String pass, String hoten, String pin) {
        this.user = user;
        this.pass = pass;
        this.hoten = hoten;
        this.pin = pin;
        this.ngaysinh = 0L;
    }
    @Ignore
    public User( String user, String hoten, String gt, String sdt, String email,long ngaysinh) {
        this.ngaysinh = ngaysinh > 0 ? ngaysinh : System.currentTimeMillis();
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

    public long getNgaysinh() {
        return ngaysinh;
    }

    public void setNgaysinh(long ngaysinh) {
        this.ngaysinh = ngaysinh;
    }
}
