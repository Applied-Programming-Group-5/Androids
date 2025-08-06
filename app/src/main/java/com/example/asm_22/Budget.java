package com.example.asm_22;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho bảng 'budgets' trong database.
 * Mỗi bản ghi là một ngân sách cho một danh mục trong một tháng/năm cụ thể.
 */
@Entity(tableName = "budgets")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String category; // Danh mục áp dụng ngân sách (ví dụ: "Ăn uống")

    public double amount;   // Số tiền ngân sách (ví dụ: 2000000.0)

    public int year;        // Năm áp dụng (ví dụ: 2025)

    public int month;       // Tháng áp dụng (1-12)
    // Thêm dòng này vào trong class Budget
    public int userId;
}