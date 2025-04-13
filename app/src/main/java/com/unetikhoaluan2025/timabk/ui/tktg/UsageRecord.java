package com.unetikhoaluan2025.timabk.ui.tktg;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "usage_records")
public class UsageRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String appName;
    private long firstTimestamp;
    private long lastTimestamp;
    private long durationInSeconds;
    private String date;

    @Ignore
    private List<Session> sessions = new ArrayList<>();

    public UsageRecord(String appName, long firstTimestamp, long lastTimestamp, long durationInSeconds, String date) {
        this.appName = appName;
        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = lastTimestamp;
        this.durationInSeconds = durationInSeconds;
        this.date = date;
    }

    // Getters
    public int getId() {
        return id;
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

    public String getDate() {
        return date;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setFirstTimestamp(long firstTimestamp) {
        this.firstTimestamp = firstTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
        // Cập nhật tổng thời gian khi set sessions
        this.durationInSeconds = calculateTotalDuration();
        // Cập nhật first và last timestamp
        updateTimestampsFromSessions();
    }

    // Thêm session mới
    public void addSession(Session session) {
        this.sessions.add(session);
        this.durationInSeconds += session.getDuration();
        updateTimestampsFromSession(session);
    }

    // Tính tổng thời gian từ các session
    private long calculateTotalDuration() {
        long total = 0;
        for (Session session : sessions) {
            total += session.getDuration();
        }
        return total;
    }

    // Cập nhật first và last timestamp từ session mới
    private void updateTimestampsFromSession(Session session) {
        if (session.getStartTime().getTime() < this.firstTimestamp || this.firstTimestamp == 0) {
            this.firstTimestamp = session.getStartTime().getTime();
        }
        if (session.getEndTime().getTime() > this.lastTimestamp) {
            this.lastTimestamp = session.getEndTime().getTime();
        }
    }

    // Cập nhật first và last timestamp từ tất cả sessions
    private void updateTimestampsFromSessions() {
        if (sessions.isEmpty()) {
            this.firstTimestamp = 0;
            this.lastTimestamp = 0;
            return;
        }

        this.firstTimestamp = sessions.get(0).getStartTime().getTime();
        this.lastTimestamp = sessions.get(0).getEndTime().getTime();

        for (Session session : sessions) {
            if (session.getStartTime().getTime() < this.firstTimestamp) {
                this.firstTimestamp = session.getStartTime().getTime();
            }
            if (session.getEndTime().getTime() > this.lastTimestamp) {
                this.lastTimestamp = session.getEndTime().getTime();
            }
        }
    }
}
