package com.example.asm_22;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecurringExpenseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecurringExpenseAdapter adapter;
    private AppDatabase db;
    private SessionManager sessionManager; // THÊM MỚI
    private int currentUserId; // THÊM MỚI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        db = AppDatabase.getDatabase(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        currentUserId = sessionManager.getUserId();

        Toolbar toolbar = findViewById(R.id.toolbar_recurring);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view_recurring_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecurringExpenseAdapter();
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_recurring_expense);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(RecurringExpenseActivity.this, AddRecurringExpenseActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện click để SỬA
        adapter.setOnItemClickListener(recurringExpense -> {
            Intent intent = new Intent(RecurringExpenseActivity.this, AddRecurringExpenseActivity.class);
            intent.putExtra("RECURRING_EXPENSE_ID", recurringExpense.id);
            startActivity(intent);
        });

        // Xử lý sự kiện vuốt để XÓA
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView r, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder t) { return false; }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                RecurringExpense toDelete = adapter.getRecurringExpenseAt(position);
                new AlertDialog.Builder(RecurringExpenseActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa chi phí định kỳ này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteItem(toDelete))
                        .setNegativeButton("Hủy", (dialog, which) -> adapter.notifyItemChanged(position))
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecurringExpenses();
    }

    private void loadRecurringExpenses() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<RecurringExpense> data = db.recurringExpenseDao().getAll(currentUserId);
            runOnUiThread(() -> adapter.setRecurringExpenses(data));
        });
    }

    private void deleteItem(RecurringExpense item) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            db.recurringExpenseDao().delete(item);
            loadRecurringExpenses(); // Tải lại danh sách
        });
    }
}