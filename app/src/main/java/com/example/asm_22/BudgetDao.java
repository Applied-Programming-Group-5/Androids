package com.example.asm_22;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import androidx.room.Delete;

@Dao
public interface BudgetDao {

    /**
     * Chèn một ngân sách mới. Nếu đã có ngân sách cho cùng danh mục/tháng/năm,
     * nó sẽ được thay thế.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(Budget budget);

    /**
     * Lấy tất cả ngân sách cho một tháng và năm cụ thể.
     * @param year Năm cần lấy
     * @param month Tháng cần lấy
     * @return Danh sách các ngân sách
     */
    @Query("SELECT * FROM budgets WHERE userId = :userId AND year = :year AND month = :month")
    List<Budget> getBudgetsForMonth(int userId, int year, int month);
    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category AND year = :year AND month = :month LIMIT 1")
    Budget getBudgetForCategory(int userId, String category, int year, int month);

    @Query("SELECT SUM(amount) FROM budgets WHERE userId = :userId AND year = :year AND month = :month")
    Double getTotalBudgetForMonth(int userId, int year, int month);


    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    List<Budget> getBudgetsForMonth(int year, int month);

    /**
     * Lấy một ngân sách cụ thể cho một danh mục trong một tháng/năm.
     */
    @Query("SELECT * FROM budgets WHERE category = :category AND year = :year AND month = :month LIMIT 1")
    Budget getBudgetForCategory(String category, int year, int month);

    // Thêm phương thức này vào bên trong interface BudgetDao

    /**
     * Tính tổng ngân sách của tất cả các danh mục trong một tháng/năm.
     * Nó trả về một kiểu Double, có thể là null nếu không có ngân sách nào.
     */
    @Query("SELECT SUM(amount) FROM budgets WHERE year = :year AND month = :month")
    Double getTotalBudgetForMonth(int year, int month);

    @Delete
    void delete(Budget budget);
}