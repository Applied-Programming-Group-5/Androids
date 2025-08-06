package com.example.asm_22;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    // Các key để truyền dữ liệu qua Intent, thêm EXTRA_ID
    public static final String EXTRA_ID = "com.example.campusexpensemanager.EXTRA_ID";
    public static final String EXTRA_USER_ID = "com.example.campusexpensemanager.EXTRA_USER_ID";
    public static final String EXTRA_DESCRIPTION = "com.example.campusexpensemanager.EXTRA_DESCRIPTION";
    public static final String EXTRA_AMOUNT = "com.example.campusexpensemanager.EXTRA_AMOUNT";
    public static final String EXTRA_CATEGORY = "com.example.campusexpensemanager.EXTRA_CATEGORY";
    public static final String EXTRA_DATE = "com.example.campusexpensemanager.EXTRA_DATE";

    private TextInputEditText editTextDescription;
    private TextInputEditText editTextAmount;
    private AutoCompleteTextView autoCompleteCategory;
    private Button buttonDatePicker;
    private Button buttonSave;
    private TextView titleTextView;

    private Calendar selectedDate = Calendar.getInstance();
    private int currentExpenseId = -1;
    private int currentUserId = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Ánh xạ views
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextAmount = findViewById(R.id.edit_text_amount);
        autoCompleteCategory = findViewById(R.id.auto_complete_category);
        buttonDatePicker = findViewById(R.id.button_date_picker);
        buttonSave = findViewById(R.id.button_save);
        titleTextView = findViewById(R.id.text_view_title);

        setupCategoryDropdown();

        // Lấy Intent đã mở Activity này
        Intent intent = getIntent();
        currentUserId = intent.getIntExtra(EXTRA_USER_ID, -1);

        // KIỂM TRA XEM CÓ DỮ LIỆU ĐƯỢC GỬI TỚI KHÔNG (CHẾ ĐỘ SỬA)
        if (intent.hasExtra(EXTRA_ID)) {
            // Đây là chế độ sửa
            titleTextView.setText("Sửa Chi Tiêu");
            buttonSave.setText("Cập Nhật");

            currentExpenseId = intent.getIntExtra(EXTRA_ID, -1);
            editTextDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
            editTextAmount.setText(String.valueOf(intent.getDoubleExtra(EXTRA_AMOUNT, 0)));
            autoCompleteCategory.setText(intent.getStringExtra(EXTRA_CATEGORY), false); // false để không filter lại dropdown

            long dateInMillis = intent.getLongExtra(EXTRA_DATE, -1);
            if (dateInMillis != -1) {
                selectedDate.setTimeInMillis(dateInMillis);
            }
        } else {
            // Đây là chế độ thêm mới
            titleTextView.setText("Thêm Chi Tiêu Mới");
            buttonSave.setText("Lưu");
        }

        updateDateButtonText();
        buttonDatePicker.setOnClickListener(v -> showDatePickerDialog());
        buttonSave.setOnClickListener(v -> saveExpense());
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteCategory.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButtonText();
        };

        new DatePickerDialog(this, dateSetListener,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        buttonDatePicker.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveExpense() {
        String description = editTextDescription.getText().toString();
        String amountString = editTextAmount.getText().toString();
        String category = autoCompleteCategory.getText().toString();

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(amountString) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không thể lưu vì không có thông tin người dùng.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent replyIntent = new Intent();
        replyIntent.putExtra(EXTRA_DESCRIPTION, description);
        replyIntent.putExtra(EXTRA_AMOUNT, amount);
        replyIntent.putExtra(EXTRA_CATEGORY, category);
        replyIntent.putExtra(EXTRA_DATE, selectedDate.getTimeInMillis());
        replyIntent.putExtra(EXTRA_USER_ID, currentUserId);

        // Nếu là chế độ sửa, gửi cả ID về
        if (currentExpenseId != -1) {
            replyIntent.putExtra(EXTRA_ID, currentExpenseId);
        }

        setResult(RESULT_OK, replyIntent);
        finish();
    }
}