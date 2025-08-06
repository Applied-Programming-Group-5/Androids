package com.example.asm_22;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddRecurringExpenseActivity extends AppCompatActivity {

    private TextInputEditText etDescription, etAmount;
    private AutoCompleteTextView acCategory;
    private NumberPicker npDay;
    private Button btnStartDate, btnEndDate, btnSave;
    private CheckBox cbNoEndDate;
    private TextView tvTitle;

    private AppDatabase db;
    private SessionManager sessionManager; // THÊM MỚI
    private RecurringExpense currentRecurringExpense;

    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private boolean hasEndDate = true;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recurring_expense);

        db = AppDatabase.getDatabase(getApplicationContext());

        sessionManager = new SessionManager(getApplicationContext());

        // Ánh xạ views
        tvTitle = findViewById(R.id.text_view_rec_title);
        etDescription = findViewById(R.id.edit_text_rec_description);
        etAmount = findViewById(R.id.edit_text_rec_amount);
        acCategory = findViewById(R.id.auto_complete_rec_category);
        npDay = findViewById(R.id.number_picker_day);
        btnStartDate = findViewById(R.id.button_start_date);
        btnEndDate = findViewById(R.id.button_end_date);
        cbNoEndDate = findViewById(R.id.checkbox_no_end_date);
        btnSave = findViewById(R.id.button_rec_save);

        setupPickersAndInputs();
        setupListeners();

        // Kiểm tra xem có phải chế độ sửa không
        int recurringExpenseId = getIntent().getIntExtra("RECURRING_EXPENSE_ID", -1);
        if (recurringExpenseId != -1) {
            tvTitle.setText("Sửa Chi phí định kỳ");
            loadExistingData(recurringExpenseId);
        } else {
            tvTitle.setText("Thêm Chi phí định kỳ");
            currentRecurringExpense = new RecurringExpense();
            currentRecurringExpense.userId = sessionManager.getUserId();
        }
    }

    private void setupPickersAndInputs() {
        // NumberPicker cho ngày
        npDay.setMinValue(1);
        npDay.setMaxValue(31);
        npDay.setValue(1);

        // Dropdown cho danh mục
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        acCategory.setAdapter(adapter);

        // Cập nhật text cho các nút ngày
        updateDateButtons();
    }

    private void setupListeners() {
        btnStartDate.setOnClickListener(v -> showDatePickerDialog(startDate, true));
        btnEndDate.setOnClickListener(v -> showDatePickerDialog(endDate, false));

        cbNoEndDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasEndDate = !isChecked;
            btnEndDate.setEnabled(!isChecked);
            updateDateButtons();
        });

        btnSave.setOnClickListener(v -> saveRecurringExpense());
    }

    private void showDatePickerDialog(Calendar calendar, boolean isStartDate) {
        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            if (isStartDate && calendar.after(endDate)) {
                endDate.setTime(calendar.getTime()); // Đảm bảo ngày kết thúc không trước ngày bắt đầu
            }
            updateDateButtons();
        };

        new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateButtons() {
        btnStartDate.setText(sdf.format(startDate.getTime()));
        if (hasEndDate) {
            btnEndDate.setText(sdf.format(endDate.getTime()));
        } else {
            btnEndDate.setText("Không có");
        }
    }

    private void loadExistingData(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            currentRecurringExpense = db.recurringExpenseDao().getById(id, sessionManager.getUserId()); // Cần thêm getById vào DAO
            runOnUiThread(() -> {
                if (currentRecurringExpense != null) {
                    etDescription.setText(currentRecurringExpense.description);
                    etAmount.setText(String.valueOf(currentRecurringExpense.amount));
                    acCategory.setText(currentRecurringExpense.category, false);
                    npDay.setValue(currentRecurringExpense.dayOfMonth);

                    startDate.setTimeInMillis(currentRecurringExpense.startDate);
                    if (currentRecurringExpense.endDate == -1L) {
                        cbNoEndDate.setChecked(true);
                    } else {
                        endDate.setTimeInMillis(currentRecurringExpense.endDate);
                    }
                    updateDateButtons();
                }
            });
        });
    }

    private void saveRecurringExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String category = acCategory.getText().toString().trim();

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        currentRecurringExpense.description = description;
        currentRecurringExpense.amount = Double.parseDouble(amountStr);
        currentRecurringExpense.category = category;
        currentRecurringExpense.dayOfMonth = npDay.getValue();
        currentRecurringExpense.startDate = startDate.getTimeInMillis();
        currentRecurringExpense.endDate = hasEndDate ? endDate.getTimeInMillis() : -1L;

        // lastGeneratedMonth sẽ được xử lý sau

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (currentRecurringExpense.id == 0) {
                db.recurringExpenseDao().insert(currentRecurringExpense);
            } else {
                db.recurringExpenseDao().update(currentRecurringExpense);
            }
            finish(); // Quay lại màn hình danh sách
        });
    }
}