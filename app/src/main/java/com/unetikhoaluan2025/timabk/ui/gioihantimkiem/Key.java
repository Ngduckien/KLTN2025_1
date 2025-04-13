package com.unetikhoaluan2025.timabk.ui.gioihantimkiem;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "keyword")
public class Key {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "keyword")
    private String keyword;

    // Constructor
    public Key(String keyword) {
        this.keyword = keyword;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}