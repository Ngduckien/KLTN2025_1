package com.unetikhoaluan2025.timabk.ui.lapkehoach.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.unetikhoaluan2025.timabk.ui.login.User;

@Entity(
        tableName = "kehoach",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user",
                childColumns = "userid",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index("userid"), @Index("ngay_bat_dau"), @Index("ngay_ket_thuc")}
)
public class LapKeHoach {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "userid")
    @NonNull
    private String userId;

    @ColumnInfo(name = "ten_ke_hoach")
    @NonNull
    private String tenKeHoach;

    @ColumnInfo(name = "ngay_bat_dau")
    private long ngayBatDau;

    @ColumnInfo(name = "ngay_ket_thuc")
    private long ngayKetThuc;

    @ColumnInfo(name = "mo_ta")
    private String moTa;

    @ColumnInfo(name = "thoi_gian_nhac_nho")
    private String thoiGianNhacNho;

    @ColumnInfo(name = "ngay_nhac_nho")
    private Long ngayNhacNho;

    @ColumnInfo(name = "lap_lai")
    private String lapLai;

    @ColumnInfo(name = "ngay_ket_thuc_lap")
    private Long ngayKetThucLap;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private boolean isCompleted;
    // Constructor
    public LapKeHoach(String userId, String tenKeHoach, long ngayBatDau, long ngayKetThuc,
                   String moTa, String thoiGianNhacNho, Long ngayNhacNho,
                   String lapLai, Long ngayKetThucLap) {
        this.userId = userId;
        this.tenKeHoach = tenKeHoach;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.moTa = moTa;
        this.thoiGianNhacNho = thoiGianNhacNho;
        this.ngayNhacNho = ngayNhacNho;
        this.lapLai = lapLai;
        this.ngayKetThucLap = ngayKetThucLap;
        this.isCompleted = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenKeHoach() {
        return tenKeHoach;
    }

    public void setTenKeHoach(String tenKeHoach) {
        this.tenKeHoach = tenKeHoach;
    }

    public long getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(long ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public long getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(long ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getThoiGianNhacNho() {
        return thoiGianNhacNho;
    }

    public void setThoiGianNhacNho(String thoiGianNhacNho) {
        this.thoiGianNhacNho = thoiGianNhacNho;
    }

    public Long getNgayNhacNho() {
        return ngayNhacNho;
    }

    public void setNgayNhacNho(Long ngayNhacNho) {
        this.ngayNhacNho = ngayNhacNho;
    }

    public String getLapLai() {
        return lapLai;
    }

    public void setLapLai(String lapLai) {
        this.lapLai = lapLai;
    }

    public Long getNgayKetThucLap() {
        return ngayKetThucLap;
    }

    public void setNgayKetThucLap(Long ngayKetThucLap) {
        this.ngayKetThucLap = ngayKetThucLap;
    }

    public  boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}