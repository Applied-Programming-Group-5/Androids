package com.example.asm_22;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etConfirmPassword, etEmail; // Thêm etEmail
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getDatabase(getApplicationContext());

        etUsername = findViewById(R.id.edit_text_register_username);
        etEmail = findViewById(R.id.edit_text_register_email); // Ánh xạ ô email
        etPassword = findViewById(R.id.edit_text_register_password);
        etConfirmPassword = findViewById(R.id.edit_text_register_confirm_password);
        Button btnRegister = findViewById(R.id.button_register);
        TextView tvGoToLogin = findViewById(R.id.text_view_go_to_login);

        btnRegister.setOnClickListener(v -> registerUser());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim(); // Lấy email
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            User existingUser = db.userDao().findByUsername(username);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show());
                return;
            }

            User existingEmail = db.userDao().findByEmail(email);
            if (existingEmail != null) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show());
                return;
            }

            User newUser = new User();
            newUser.username = username;
            newUser.email = email;
            newUser.password = password;
            db.userDao().insert(newUser);
            runOnUiThread(() -> {
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}