package com.example.asm_22;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetManagementActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private TextInputEditText etTotalBudget, etSpendingLimit;
    private TextView tvExpectedSavings;
    private Button btnSave;
    private AppDatabase db;
    private SessionManager sessionManager;
    private int currentUserId;

    private int selectedYear;
    private int selectedMonth; // 0-11

    private static final String TOTAL_BUDGET_CATEGORY = "TOTAL_BUDGET";
    private static final String SPENDING_LIMIT_CATEGORY = "SPENDING_LIMIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_management);

        db = AppDatabase.getDatabase(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        currentUserId = sessionManager.getUserId();

        Toolbar toolbar = findViewById(R.id.toolbar_budget);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        etTotalBudget = findViewById(R.id.edit_text_total_budget);
        etSpendingLimit = findViewById(R.id.edit_text_spending_limit);
        tvExpectedSavings = findViewById(R.id.text_view_expected_savings);
        btnSave = findViewById(R.id.button_save_budget_setup);

        // *** BƯỚC QUAN TRỌNG BỊ THIẾU TRƯỚC ĐÂY ***
        setupSpinners();
        setupListeners();
    }

    private void setupSpinners() {
        // --- Thiết lập Spinner Năm ---
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // --- Thiết lập Spinner Tháng ---
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // --- Đặt giá trị mặc định là tháng/năm hiện tại ---
        selectedYear = currentYear;
        selectedMonth = Calendar.getInstance().get(Calendar.MONTH); // Tháng bắt đầu từ 0
        spinnerYear.setSelection(years.indexOf(String.valueOf(selectedYear)), false);
        spinnerMonth.setSelection(selectedMonth, false);

        // --- Lắng nghe sự kiện khi chọn item mới ---
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt((String) spinnerYear.getSelectedItem());
                selectedMonth = spinnerMonth.getSelectedItemPosition();
                loadBudgetSetup(); // Khi thay đổi lựa chọn, tải lại dữ liệu
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerMonth.setOnItemSelectedListener(listener);
        spinnerYear.setOnItemSelectedListener(listener);

        // Tải dữ liệu lần đầu tiên sau khi listener đã được thiết lập
        loadBudgetSetup();
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                calculateExpectedSavings();
            }
        };
        etTotalBudget.addTextChangedListener(textWatcher);
        etSpendingLimit.addTextChangedListener(textWatcher);
        btnSave.setOnClickListener(v -> saveBudgetSetup());
    }

    private void calculateExpectedSavings() {
        double totalBudget = 0;
        double spendingLimit = 0;
        try {
            totalBudget = Double.parseDouble(etTotalBudget.getText().toString());
        } catch (NumberFormatException e) { /* Bỏ qua */ }
        try {
            spendingLimit = Double.parseDouble(etSpendingLimit.getText().toString());
        } catch (NumberFormatException e) { /* Bỏ qua */ }

        double savings = totalBudget - spendingLimit;
        DecimalFormat formatter = new DecimalFormat("#,###,### đ");
        tvExpectedSavings.setText(formatter.format(savings));
    }

    private void loadBudgetSetup() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Budget totalBudgetObj = db.budgetDao().getBudgetForCategory(currentUserId, TOTAL_BUDGET_CATEGORY, selectedYear, selectedMonth + 1);
            Budget spendingLimitObj = db.budgetDao().getBudgetForCategory(currentUserId, SPENDING_LIMIT_CATEGORY, selectedYear, selectedMonth + 1);

            runOnUiThread(() -> {
                etTotalBudget.setText(totalBudgetObj != null ? String.valueOf(totalBudgetObj.amount) : "");
                etSpendingLimit.setText(spendingLimitObj != null ? String.valueOf(spendingLimitObj.amount) : "");
                calculateExpectedSavings();
            });
        });
    }

    private void saveBudgetSetup() {
        double totalBudget = 0;
        double spendingLimit = 0;
        try {
            totalBudget = Double.parseDouble(etTotalBudget.getText().toString());
        } catch (NumberFormatException e) { /* Bỏ qua */ }
        try {
            spendingLimit = Double.parseDouble(etSpendingLimit.getText().toString());
        } catch (NumberFormatException e) { /* Bỏ qua */ }

        final double finalTotalBudget = totalBudget;
        final double finalSpendingLimit = spendingLimit;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            saveOrUpdateBudget(TOTAL_BUDGET_CATEGORY, finalTotalBudget);
            saveOrUpdateBudget(SPENDING_LIMIT_CATEGORY, finalSpendingLimit);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã lưu thiết lập ngân sách", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void saveOrUpdateBudget(String category, double amount) {
        Budget budget = db.budgetDao().getBudgetForCategory(currentUserId, category, selectedYear, selectedMonth + 1);
        if (amount > 0) {
            if (budget == null) {
                budget = new Budget();
                budget.category = category;
                budget.year = selectedYear;
                budget.month = selectedMonth + 1;
                budget.userId = currentUserId;
            }
            budget.amount = amount;
            db.budgetDao().insertOrUpdate(budget);
        } else {
            if (budget != null) {
                db.budgetDao().delete(budget);
            }
        }
    }
}