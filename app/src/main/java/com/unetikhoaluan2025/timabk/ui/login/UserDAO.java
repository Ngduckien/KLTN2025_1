package com.unetikhoaluan2025.timabk.ui.login;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
@Dao
public interface UserDAO {
    @Insert
    void InsetUser(User user);

    @Query("SELECT * FROM user WHERE user = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM user WHERE user = :username AND pass = :password LIMIT 1")
    User loginUser(String username, String password);

    @Query("SELECT COUNT(*) FROM User WHERE user = :user  AND pin = :pin")
    int checkUser(String user, String pin);

    @Query("UPDATE User SET pass = :newPassword WHERE user = :user")
    void updatePassword(String user, String newPassword);

    @Query("SELECT pin FROM user WHERE user = :username LIMIT 1")
    String getPinByUsername(String username);

    @Query("DELETE FROM user WHERE user = :username")
    void deleteByUsername(String username);
    @Query("UPDATE user SET hoten = :hoten, gt = :gt, sdt = :phone, email = :email, ngaysinh = :dateOfBirth WHERE user = :user")
    void updateUserByUsername(String user, String hoten, String gt, String phone, String email, long dateOfBirth);



}
