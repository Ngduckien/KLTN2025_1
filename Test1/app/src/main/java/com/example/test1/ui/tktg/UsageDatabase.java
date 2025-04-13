package com.unetikhoaluan2025.timabk.ui.tktg;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UsageRecord.class}, version = 1,exportSchema = false)
public abstract class UsageDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "usage.db";
    private static UsageDatabase instance;

    public static synchronized UsageDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), UsageDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract UsageDao usageDao();
}
