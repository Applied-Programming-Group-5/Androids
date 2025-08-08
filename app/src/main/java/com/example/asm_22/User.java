package com.example.asm_22;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Cập nhật Entity:
 * - Thêm cột 'email'.
 * - Thêm index để đảm bảo cả 'username' và 'email' đều là duy nhất.
 */
@Entity(tableName = "users",
        indices = {
                @Index(value = {"username"}, unique = true),
                @Index(value = {"email"}, unique = true) // THÊM DÒNG NÀY
        })
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "email") // THÊM CỘT MỚI
    public String email;
}
