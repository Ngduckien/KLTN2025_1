package com.unetikhoaluan2025.timabk.ui.tktg;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usage_records")
public class UsageRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String appName;
    private long firstTimestamp;
    private long lastTimestamp;
    private long durationInSeconds;
    private String date;  // ✅ Thêm trường date

    public UsageRecord(String appName, long firstTimestamp, long lastTimestamp, long durationInSeconds, String date) {
        this.appName = appName;
        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = lastTimestamp;
        this.durationInSeconds = durationInSeconds;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public long getDurationInSeconds() {
        return durationInSeconds;
    }

    public String getDate() {  // ✅ Thêm getter cho date
        return date;
    }

    public void setDate(String date) {  // ✅ Thêm setter cho date
        this.date = date;
    }
}
