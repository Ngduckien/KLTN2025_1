package com.unetikhoaluan2025.timabk.ui.gioihantimkiem;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unetikhoaluan2025.timabk.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GHTKViewModel extends AndroidViewModel {
    private final KeyDAO keyDAO;
    private final LiveData<List<Key>> allKeywords;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GHTKViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        keyDAO = db.keyDAO();
        allKeywords = keyDAO.getAll(); // ✅ Lấy dữ liệu từ DAO
    }

    public LiveData<List<Key>> getAllKeywords() {
        return allKeywords; // ✅ Trả về danh sách từ khóa
    }
    public void insertKeyword(String keyword) {
        executor.execute(() -> keyDAO.insert(new Key(keyword)));
    }

    public void updateKeyword(Key key) {
        executor.execute(() -> keyDAO.update(key));
    }

    public void deleteAllKeywords() {
        executor.execute(keyDAO::deleteAll);
    }
    public LiveData<List<String>> getAllKeywordsAsStrings() {
        MutableLiveData<List<String>> keywordsAsStrings = new MutableLiveData<>();
        executor.execute(() -> {
            List<Key> keys = keyDAO.getAllKey();  // Lấy tất cả các từ khóa từ DAO
            List<String> keywordStrings = new ArrayList<>();
            for (Key key : keys) {
                keywordStrings.add(key.getKeyword()); // Giả sử Key có phương thức getKeyword() trả về từ khóa
            }
            keywordsAsStrings.postValue(keywordStrings); // Cập nhật LiveData
        });
        return keywordsAsStrings;
    }


}