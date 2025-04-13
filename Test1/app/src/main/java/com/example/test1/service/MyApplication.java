package com.unetikhoaluan2025.timabk.service;

import android.app.Application;

import androidx.room.Room;

import com.unetikhoaluan2025.timabk.data.AppDatabase;

public class MyApplication extends Application {
    private AppDatabase appDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app_database").build();
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}