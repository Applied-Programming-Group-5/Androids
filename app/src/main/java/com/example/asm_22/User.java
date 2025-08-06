package com.example.asm_22;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho bảng 'users'.
 * Đảm bảo tên bảng là "users".
 * Thêm một 'index' để đảm bảo rằng cột 'username' là duy nhất (unique).
 */
@Entity(tableName = "users",
        indices = {@Index(value = {"username"}, unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Đảm bảo có cột username và password
    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password")
    public String password;
}
