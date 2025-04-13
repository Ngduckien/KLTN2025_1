package com.unetikhoaluan2025.timabk.ui.lapkehoach.data;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Converter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    // Format tiện ích
    public static String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    public static String formatTime(String timeValue) {
        try {
            Date time = timeFormat.parse(timeValue);
            return timeFormat.format(time);
        } catch (ParseException e) {
            return timeValue;
        }
    }
    public static Date parseDate(String dateStr) throws ParseException {
        return dateFormat.parse(dateStr);
    }
}
