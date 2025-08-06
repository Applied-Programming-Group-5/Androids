package com.example.asm_22;

import androidx.lifecycle.LiveData; // Sẽ dùng sau, cứ import trước
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import androidx.room.Delete;
import androidx.room.Update;
import java.util.List;

/**
 * Đây là DAO (Data Access Object).
 * @Dao báo cho Room biết đây là một interface DAO.
 * Interface này định nghĩa các cách chúng ta có thể truy cập vào dữ liệu.
 */
@Dao
public interface ExpenseDao {

    /**
     * @Insert: Một hành động chèn dữ liệu. Room sẽ tạo ra tất cả code cần thiết
     * để chèn một đối tượng Expense vào bảng.
     */
    @Insert
    void insert(Expense expense);

    /**
     * @Query: Dùng để định nghĩa một truy vấn tùy chỉnh bằng ngôn ngữ SQL.
     * Câu lệnh này có nghĩa là: "Lấy TẤT CẢ (*) các cột TỪ bảng expenses,
     * và SẮP XẾP chúng THEO cột 'date' với thứ tự GIẢM DẦN (DESC)",
     * tức là chi tiêu mới nhất sẽ hiện ở trên cùng.
     *
     * Room sẽ xác thực câu lệnh SQL này ngay tại thời điểm biên dịch.
     * Nó trả về một danh sách (List) các đối tượng Expense.
     */

    @Query("SELECT * FROM expenses WHERE isCompleted = 0 ORDER BY date DESC") // isCompleted = 0 nghĩa là false
    List<Expense> getAllExpenses();

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    // Thêm phương thức này vào bên trong interface ExpenseDao

    /**
     * Tính tổng số tiền đã chi trong một tháng/năm cụ thể.
     * 'strftime' là một hàm của SQLite để định dạng thời gian.
     *  - '%Y' lấy ra năm (ví dụ: '2025')
     *  - '%m' lấy ra tháng (ví dụ: '08')
     *  - 'date / 1000' chuyển từ mili giây sang giây.
     *  - 'unixepoch' báo cho hàm biết đầu vào là timestamp.
     * Nó trả về một kiểu Double, có thể là null nếu không có chi tiêu nào.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND isCompleted = 0 ORDER BY date DESC")
    List<Expense> getAllExpenses(int userId);
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND strftime('%Y', date / 1000, 'unixepoch') = :year AND strftime('%m', date / 1000, 'unixepoch') = :month")
    Double getTotalAmountForMonth(int userId, String year, String month);

    @Query("SELECT category, SUM(amount) as totalAmount FROM expenses WHERE userId = :userId AND strftime('%Y', date / 1000, 'unixepoch') = :year AND strftime('%m', date / 1000, 'unixepoch') = :month GROUP BY category")
    List<CategorySpending> getSpendingByCategoryForMonth(int userId, String year, String month);


    @Query("SELECT SUM(amount) FROM expenses WHERE " +
            "strftime('%Y', date / 1000, 'unixepoch') = :year AND " +
            "strftime('%m', date / 1000, 'unixepoch') = :month")
    Double getTotalAmountForMonth(String year, String month);

    /**
     * Lấy tổng chi tiêu của TỪNG DANH MỤC trong một tháng/năm cụ thể.
     * GROUP BY category: Nhóm tất cả các bản ghi có cùng danh mục lại và tính tổng của chúng.
     * Nó trả về một danh sách các đối tượng CategorySpending.
     */
    @Query("SELECT category, SUM(amount) as totalAmount FROM expenses WHERE " +
            "strftime('%Y', date / 1000, 'unixepoch') = :year AND " +
            "strftime('%m', date / 1000, 'unixepoch') = :month " +
            "GROUP BY category")
    List<CategorySpending> getSpendingByCategoryForMonth(String year, String month);

    // Chúng ta sẽ thêm các phương thức như update (cập nhật) và delete (xóa) ở các giai đoạn sau.
}