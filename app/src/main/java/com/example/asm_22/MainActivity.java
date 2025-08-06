package com.example.asm_22;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private AppDatabase db;
    private ActivityResultLauncher<Intent> expenseActivityResultLauncher;

    private TextView textViewMonthOverview, textViewTotalSpent, textViewTotalBudget, textViewRemaining, textViewWarnings;
    private ProgressBar progressBarBudget;
    private TextView budgetBadge; // Biến để giữ tham chiếu đến dấu chấm đỏ trên menu
    private SessionManager sessionManager;
    private int currentUserId;

    private void generateRecurringExpensesIfNeeded() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Calendar now = Calendar.getInstance();
            int currentMonthCode = now.get(Calendar.YEAR) * 100 + (now.get(Calendar.MONTH) + 1); // Định dạng YYYYMM

            List<RecurringExpense> allRecurring = db.recurringExpenseDao().getAll(currentUserId);
            for (RecurringExpense re : allRecurring) {
                // Kiểm tra xem chi phí này có còn hoạt động không
                boolean isActive = re.startDate <= now.getTimeInMillis() && (re.endDate == -1L || re.endDate >= now.getTimeInMillis());

                // Nếu nó đang hoạt động VÀ tháng này chưa được tạo chi tiêu
                if (isActive && re.lastGeneratedMonth < currentMonthCode) {
                    // Tạo một bản ghi chi tiêu mới
                    Expense newExpense = new Expense();
                    newExpense.description = re.description + " (Định kỳ)";
                    newExpense.amount = re.amount;
                    newExpense.category = re.category;

                    // Đặt ngày chi tiêu là ngày đã định nghĩa trong tháng hiện tại
                    Calendar expenseDate = Calendar.getInstance();
                    expenseDate.set(Calendar.DAY_OF_MONTH, re.dayOfMonth);
                    newExpense.date = expenseDate.getTimeInMillis();

                    newExpense.isCompleted = false; // Mặc định chưa hoàn thành

                    db.expenseDao().insert(newExpense);

                    // Cập nhật lại chi phí định kỳ để đánh dấu đã tạo cho tháng này
                    re.lastGeneratedMonth = currentMonthCode;
                    db.recurringExpenseDao().update(re);
                }
            }
            // Sau khi có thể đã tạo chi tiêu mới, tải lại toàn bộ
            loadExpensesAndOverview();
        });
    }

    // ... onCreate() và các phương thức khác không thay đổi ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(getApplicationContext());
        currentUserId = sessionManager.getUserId();

        db = AppDatabase.getDatabase(getApplicationContext());

        generateRecurringExpensesIfNeeded();

        textViewMonthOverview = findViewById(R.id.text_view_month_overview);
        textViewTotalSpent = findViewById(R.id.text_view_total_spent);
        textViewTotalBudget = findViewById(R.id.text_view_total_budget);
        textViewRemaining = findViewById(R.id.text_view_remaining);
        progressBarBudget = findViewById(R.id.progress_bar_budget);
        textViewWarnings = findViewById(R.id.text_view_warnings);

        setupRecyclerView();
        setupActivityResultLauncher();
        setupItemTouchHelper();

        FloatingActionButton fabAddExpense = findViewById(R.id.fab_add_expense);
        fabAddExpense.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            intent.putExtra(AddExpenseActivity.EXTRA_USER_ID, currentUserId); // Gửi userId khi thêm mới
            expenseActivityResultLauncher.launch(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpensesAndOverview();
    }

    private void updateOverview() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH) + 1;

            // 1. Lấy các giá trị thiết lập ngân sách
            Budget totalBudgetObj = db.budgetDao().getBudgetForCategory(currentUserId, "TOTAL_BUDGET", currentYear, currentMonth);
            Budget spendingLimitObj = db.budgetDao().getBudgetForCategory(currentUserId, "SPENDING_LIMIT", currentYear, currentMonth);
            double totalBudget = totalBudgetObj != null ? totalBudgetObj.amount : 0;
            double spendingLimit = spendingLimitObj != null ? spendingLimitObj.amount : 0;

            // 2. Tính tổng chi tiêu dự kiến (kết hợp thực tế và định kỳ)
            double totalProjectedSpent = 0;
            Double actualSpent = db.expenseDao().getTotalAmountForMonth(currentUserId, String.valueOf(currentYear), String.format(Locale.US, "%02d", currentMonth));
            Double recurringSpent = db.recurringExpenseDao().getTotalActiveRecurringAmount(currentUserId, now.getTimeInMillis());
            if (actualSpent != null) totalProjectedSpent += actualSpent;
            if (recurringSpent != null) totalProjectedSpent += recurringSpent;

            // 3. Tính toán các giá trị hiển thị
            double remainingInLimit = spendingLimit - totalProjectedSpent;
            double remainingInBudget = totalBudget - totalProjectedSpent;

            // 4. Xác định trạng thái cảnh báo
            String warningMessage = "";
            int warningColor = 0; // 0: không có, 1: vàng, 2: đỏ

            if (totalProjectedSpent > totalBudget && totalBudget > 0) {
                warningMessage = "(!) Đã VƯỢT Ngân sách Tổng!";
                warningColor = 2; // Đỏ
            } else if (totalProjectedSpent > spendingLimit && spendingLimit > 0) {
                warningMessage = "(!) Vượt Hạn mức Chi tiêu. Khoản tiết kiệm có thể bị ảnh hưởng.";
                warningColor = 1; // Vàng
            }

            // 5. Gán vào biến final để dùng trong UI thread
            final double finalTotalBudget = totalBudget;
            final double finalTotalProjectedSpent = totalProjectedSpent;
            final String finalWarningMessage = warningMessage;
            final int finalWarningColor = warningColor;

            runOnUiThread(() -> {
                DecimalFormat formatter = new DecimalFormat("#,###,### đ");
                textViewMonthOverview.setText(String.format("Tổng quan Tháng %d/%d", currentMonth, currentYear));
                textViewTotalSpent.setText("Dự kiến chi: " + formatter.format(finalTotalProjectedSpent));
                textViewTotalBudget.setText("Ngân sách Tổng: " + formatter.format(finalTotalBudget));

                // Hiển thị số tiền còn lại so với Hạn mức
                textViewRemaining.setText("Còn lại (so với Hạn mức): " + formatter.format(remainingInLimit));
                textViewRemaining.setTextColor(remainingInLimit < 0 ? Color.RED : ContextCompat.getColor(this, android.R.color.tab_indicator_text));

                if (finalTotalBudget > 0) {
                    int progress = (int) ((finalTotalProjectedSpent / finalTotalBudget) * 100);
                    progressBarBudget.setProgress(Math.min(progress, 100));
                } else {
                    progressBarBudget.setProgress(0);
                }

                if (finalWarningColor > 0) {
                    textViewWarnings.setText(finalWarningMessage);
                    // Set màu Vàng hoặc Đỏ
                    textViewWarnings.setTextColor(finalWarningColor == 1 ? Color.rgb(255, 165, 0) : Color.RED);
                    textViewWarnings.setVisibility(View.VISIBLE);
                } else {
                    textViewWarnings.setVisibility(View.GONE);
                }
                if (budgetBadge != null) {
                    budgetBadge.setVisibility(finalWarningColor > 0 ? View.VISIBLE : View.GONE);
                }
            });
        });
    }

    private void setupItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expenseToActOn = expenseAdapter.getExpenseAt(position);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Hành động cho chi tiêu")
                        .setMessage("Bạn muốn làm gì với chi tiêu này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            deleteExpense(expenseToActOn);
                            Toast.makeText(MainActivity.this, "Đã xóa chi tiêu", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            expenseAdapter.notifyItemChanged(position);
                        })
                        .setNeutralButton("Hoàn thành", (dialog, which) -> {
                            // ---- LOGIC MỚI: GỌI HÀM completeExpense ----
                            completeExpense(expenseToActOn);
                            Toast.makeText(MainActivity.this, "Đã hoàn thành chi tiêu", Toast.LENGTH_SHORT).show();
                        })
                        .setCancelable(false)
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    // ---- LOGIC MỚI: PHƯƠNG THỨC completeExpense ----
    private void completeExpense(Expense expense) {
        expense.isCompleted = true; // Đánh dấu là đã hoàn thành
        updateExpense(expense); // Cập nhật lại trong DB. Nó sẽ tự biến mất khỏi list.
    }

    // ---- LOGIC MỚI: CẬP NHẬT onCreateOptionsMenu ----
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        final MenuItem budgetMenuItem = menu.findItem(R.id.action_manage_budget);
        View actionView = budgetMenuItem.getActionView();
        if (actionView != null) {
            budgetBadge = actionView.findViewById(R.id.budget_badge);
            // Thiết lập sự kiện click cho toàn bộ layout
            actionView.setOnClickListener(v -> onOptionsItemSelected(budgetMenuItem));
        }

        // Cập nhật trạng thái badge ngay khi menu được tạo
        updateOverview();

        return true;
    }

    // Bên trong phương thức onOptionsItemSelected của MainActivity
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Dùng getIntExtra để tránh lỗi tiềm ẩn khi dùng item.getItemId() trong câu lệnh switch
        int itemId = item.getItemId();
        if (itemId == R.id.action_manage_budget) {
            startActivity(new Intent(this, BudgetManagementActivity.class));
            return true;
        } else if (itemId == R.id.action_recurring_expenses) {
            startActivity(new Intent(this, RecurringExpenseActivity.class));
            return true;
        } else if (itemId == R.id.action_report) {
            startActivity(new Intent(this, ReportActivity.class));
            return true;
        } else if (itemId == R.id.action_feedback) {
            startActivity(new Intent(this, FeedbackActivity.class));
            return true;
        } else if (itemId == R.id.action_logout) { // Thêm nút đăng xuất
            sessionManager.logoutUser();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }



    // --- Các phương thức còn lại không đổi ---
    private void loadExpensesAndOverview() { updateOverview(); executeDbOperation(() -> {}); }
    private void executeDbOperation(Runnable operation) { ExecutorService executor = Executors.newSingleThreadExecutor(); executor.execute(() -> { operation.run(); List<Expense> expenses = db.expenseDao().getAllExpenses(currentUserId); runOnUiThread(() -> { expenseAdapter.setExpenses(expenses); updateOverview(); }); }); }
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(); recyclerView.setAdapter(expenseAdapter);
        expenseAdapter.setOnItemClickListener(expense -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            intent.putExtra(AddExpenseActivity.EXTRA_ID, expense.id);
            intent.putExtra(AddExpenseActivity.EXTRA_USER_ID, currentUserId); // Gửi userId khi sửa
            intent.putExtra(AddExpenseActivity.EXTRA_DESCRIPTION, expense.description);
            intent.putExtra(AddExpenseActivity.EXTRA_AMOUNT, expense.amount);
            intent.putExtra(AddExpenseActivity.EXTRA_CATEGORY, expense.category);
            intent.putExtra(AddExpenseActivity.EXTRA_DATE, expense.date);
            expenseActivityResultLauncher.launch(intent); }); }
    private void setupActivityResultLauncher() {
        expenseActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        int id = data.getIntExtra(AddExpenseActivity.EXTRA_ID, -1);
                        Expense expense = new Expense();
                        expense.userId = data.getIntExtra(AddExpenseActivity.EXTRA_USER_ID, -1); // ĐỌC LẠI userId
                        expense.description = data.getStringExtra(AddExpenseActivity.EXTRA_DESCRIPTION);
                        expense.amount = data.getDoubleExtra(AddExpenseActivity.EXTRA_AMOUNT, 0);
                        expense.category = data.getStringExtra(AddExpenseActivity.EXTRA_CATEGORY);
                        expense.date = data.getLongExtra(AddExpenseActivity.EXTRA_DATE, 0);
                        if (id == -1) {
                            insertExpense(expense);
                            Toast.makeText(this, "Đã lưu chi tiêu", Toast.LENGTH_SHORT).show();
                        } else {
                            expense.id = id;
                            updateExpense(expense);
                            Toast.makeText(this, "Đã cập nhật chi tiêu", Toast.LENGTH_SHORT).show();
                        } } }); }
    private void insertExpense(Expense expense) { executeDbOperation(() -> db.expenseDao().insert(expense)); }
    private void updateExpense(Expense expense) { executeDbOperation(() -> db.expenseDao().update(expense)); }
    private void deleteExpense(Expense expense) { executeDbOperation(() -> db.expenseDao().delete(expense)); }

}