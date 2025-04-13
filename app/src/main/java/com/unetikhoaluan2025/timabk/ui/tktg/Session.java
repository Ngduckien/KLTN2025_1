package com.unetikhoaluan2025.timabk.ui.tktg;

import java.util.Date;

public class Session {
    private Date startTime;
    private Date endTime;
    private long duration; // in seconds
    private boolean exceededLimit; // Có vượt quá giới hạn không

    // Thêm getter và setter
    public boolean isExceededLimit() {
        return exceededLimit;
    }

    public void setExceededLimit(boolean exceededLimit) {
        this.exceededLimit = exceededLimit;
    }
    public Session(Date startTime, Date endTime, long duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    // Thêm setter cho endTime
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        if (startTime != null && endTime != null) {
            this.duration = (endTime.getTime() - startTime.getTime()) / 1000;
        }
    }

    // Getters
    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
    public long getDuration() { return duration; }

    // Setters (nếu cần)
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public void setDuration(long duration) { this.duration = duration; }
}
