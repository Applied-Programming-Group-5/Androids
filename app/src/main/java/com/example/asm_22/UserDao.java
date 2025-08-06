package com.example.asm_22;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    /**
     * Truy vấn từ bảng "users"
     * Điều kiện trên cột "username"
     * Trả về một đối tượng "User"
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    /**
     * Truy vấn từ bảng "users"
     * Điều kiện trên cột "username" VÀ "password"
     * Trả về một đối tượng "User"
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);
}
