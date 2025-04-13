package com.unetikhoaluan2025.timabk.ui.tktg;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TKTGViewModel extends AndroidViewModel {
    private final MutableLiveData<List<UsageRecord>> usageRecords = new MutableLiveData<>();
    private long totalDuration = 0;

    public TKTGViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<UsageRecord>> getUsageRecords() {
        return usageRecords;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void loadUsageRecordsByDate(long startTime, long endTime) {
        Log.d("VIEWMODEL", "Lọc từ " + new Date(startTime) + " đến " + new Date(endTime));

        List<UsageRecord> filteredRecords = UsageStatsHelper.getUsageStatsByDateRange(getApplication(), startTime, endTime);

        Log.d("VIEWMODEL", "Số bản ghi sau khi lọc: " + filteredRecords.size());

        updateUsageRecords(filteredRecords);
    }

    private void updateUsageRecords(List<UsageRecord> records) {
        totalDuration = 0;
        for (UsageRecord record : records) {
            totalDuration += record.getDurationInSeconds();
        }
        usageRecords.setValue(records);
    }

    public void sortUsageRecords(boolean ascending) {
        List<UsageRecord> currentList = usageRecords.getValue();
        if (currentList != null) {
            Collections.sort(currentList, (o1, o2) -> ascending
                    ? Long.compare(o1.getDurationInSeconds(), o2.getDurationInSeconds())
                    : Long.compare(o2.getDurationInSeconds(), o1.getDurationInSeconds()));
            usageRecords.setValue(currentList);
        }
    }
}
