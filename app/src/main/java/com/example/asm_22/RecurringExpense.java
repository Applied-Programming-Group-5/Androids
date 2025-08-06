package com.example.asm_22;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho bảng 'recurring_expenses' trong database.
 * Lưu trữ định nghĩa về một khoản chi tiêu lặp lại hàng tháng.
 */
@Entity(tableName = "recurring_expenses")
public class RecurringExpense {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String description; // Mô tả (ví dụ: "Tiền thuê nhà")
    public double amount;      // Số tiền
    public String category;    // Danh mục

    // Ngày chi tiêu sẽ được tạo ra hàng tháng, ví dụ vào ngày 15
    public int dayOfMonth;     // Ngày trong tháng (1-31) mà chi phí này được tính

    public long startDate;     // Ngày bắt đầu áp dụng (lưu dưới dạng timestamp)
    public long endDate;       // Ngày kết thúc áp dụng (có thể không có, ví dụ: -1L)

    // Dùng để theo dõi xem tháng gần nhất đã tạo chi tiêu là tháng nào
    // Định dạng: YYYYMM, ví dụ: 202508
    public int lastGeneratedMonth;
    // Thêm dòng này vào trong class RecurringExpense
    public int userId;
}