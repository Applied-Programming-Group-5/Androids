package com.example.asm_22;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecurringExpenseDao {

    @Insert
    void insert(RecurringExpense recurringExpense);

    @Update
    void update(RecurringExpense recurringExpense);

    @Delete
    void delete(RecurringExpense recurringExpense);

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId ORDER BY dayOfMonth ASC")
    List<RecurringExpense> getAll(int userId);

    @Query("SELECT * FROM recurring_expenses WHERE id = :id AND userId = :userId")
    RecurringExpense getById(int id, int userId);

    @Query("SELECT SUM(amount) FROM recurring_expenses WHERE userId = :userId AND startDate <= :specificDateInMillis AND (endDate = -1 OR endDate >= :specificDateInMillis)")
    Double getTotalActiveRecurringAmount(int userId, long specificDateInMillis);

    @Query("SELECT * FROM recurring_expenses WHERE " +
            "userId = :userId AND " +
            "startDate <= :specificDateInMillis AND " +
            "(endDate = -1 OR endDate >= :specificDateInMillis)")
    List<RecurringExpense> getAllActiveForMonth(int userId, long specificDateInMillis);

    @Query("SELECT * FROM recurring_expenses ORDER BY dayOfMonth ASC")
    List<RecurringExpense> getAll();

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    RecurringExpense getById(int id);

    @Query("SELECT SUM(amount) FROM recurring_expenses WHERE " +
            "startDate <= :specificDateInMillis AND " +
            "(endDate = -1 OR endDate >= :specificDateInMillis)")
    Double getTotalActiveRecurringAmount(long specificDateInMillis);
}
