package com.example.asm_22;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Đây là Entity (Thực thể), đại diện cho một bảng tên là "expenses" trong cơ sở dữ liệu.
 * Room sẽ tự động tạo một bảng dựa trên cấu trúc của class này.
 */
@Entity(tableName = "expenses")
public class Expense {

    /**
     * @PrimaryKey đánh dấu đây là khóa chính của bảng.
     * autoGenerate = true nghĩa là Room sẽ tự động tạo một ID duy nhất
     * cho mỗi bản ghi (chi tiêu) mới được thêm vào.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;
    // Thêm dòng này vào trong class Expense
    public int userId;

    /**
     * Các cột còn lại trong bảng.
     * Tên biến sẽ là tên cột.
     */
    public String description; // Mô tả chi tiêu, ví dụ: "Ăn trưa Bún chả"
    public double amount;      // Số tiền, ví dụ: 35000.0
    public String category;    // Danh mục, ví dụ: "Ăn uống", "Đi lại"
    public long date;          // Ngày chi tiêu. Chúng ta dùng kiểu 'long' để lưu timestamp
    // (số mili giây tính từ 1/1/1970), việc này giúp
    // sắp xếp và truy vấn theo ngày tháng rất hiệu quả.

    // Để đơn giản cho việc học, chúng ta không cần tạo constructor, getters và setters
    // vì các thuộc tính đều là public. Trong dự án thực tế lớn hơn, bạn nên tạo chúng.
    // Thêm trường này vào trong class Expense
    public boolean isCompleted = false; // Mặc định là false khi tạo mới
}
