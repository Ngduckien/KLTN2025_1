package com.unetikhoaluan2025.timabk.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.unetikhoaluan2025.timabk.ui.login.User;
import com.unetikhoaluan2025.timabk.ui.login.UserDAO;
import com.unetikhoaluan2025.timabk.ui.gioihantimkiem.Key;
import com.unetikhoaluan2025.timabk.ui.gioihantimkiem.KeyDAO;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoach;
import com.unetikhoaluan2025.timabk.ui.gioihan.AppLimit;
import com.unetikhoaluan2025.timabk.ui.gioihan.AppLimitDao;
import com.unetikhoaluan2025.timabk.ui.lapkehoach.data.LapKeHoachDAO;
import com.unetikhoaluan2025.timabk.ui.tktg.UsageDao;
import com.unetikhoaluan2025.timabk.ui.tktg.UsageRecord;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, LapKeHoach.class, AppLimit.class, UsageRecord.class, Key.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);


    // DAOs for each entity
    public abstract UserDAO userDAO();
    public abstract UsageDao usageDao();
    public abstract LapKeHoachDAO lapKeHoachDAO();
    public abstract AppLimitDao appLimitDao();
    public abstract KeyDAO keyDAO();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_time.db") // New unique name
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
