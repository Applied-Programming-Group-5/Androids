package com.example.asm_22;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Đảm bảo version = 5 và User.class có trong entities
@Database(entities = {Expense.class, Budget.class, RecurringExpense.class, User.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();
    public abstract BudgetDao budgetDao();
    public abstract RecurringExpenseDao recurringExpenseDao();
    public abstract UserDao userDao(); // Đảm bảo có phương thức này

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "expense_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
