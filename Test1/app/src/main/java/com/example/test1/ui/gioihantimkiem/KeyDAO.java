package com.unetikhoaluan2025.timabk.ui.gioihantimkiem;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface KeyDAO {
    // Chèn một từ khóa mới
    @Insert
    void insert(Key key);

    // Cập nhật từ khóa đã có
    @Update
    void update(Key key);

    // Xóa từ khóa
    @Delete
    void delete(Key key);

    // Lấy tất cả các từ khóa
    @Query("SELECT * FROM keyword")
    List<Key> getAllKeywords();

    @Query("SELECT * FROM keyword")
    LiveData<List<Key>> getAll();

    // Xóa tất cả từ khóa trong bảng
    @Query("DELETE FROM keyword")
    void deleteAll();
    @Query("SELECT * FROM keyword WHERE keyword = :keyword")
    Key getKeywordByWord(String keyword);

    @Query("SELECT * FROM keyword")
    List<Key> getAllKey();
}