package com.example.asm_22;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (sessionManager.isLoggedIn()) {
            goToMainActivity();
            return; // Dừng thực thi onCreate để không hiển thị layout login
        }

        setContentView(R.layout.activity_login);

        db = AppDatabase.getDatabase(getApplicationContext());

        etUsername = findViewById(R.id.edit_text_login_username);
        etPassword = findViewById(R.id.edit_text_login_password);
        Button btnLogin = findViewById(R.id.button_login);
        TextView tvGoToRegister = findViewById(R.id.text_view_go_to_register);

        btnLogin.setOnClickListener(v -> loginUser());
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            User user = db.userDao().login(username, password);
            runOnUiThread(() -> {
                if (user != null) {
                    // Đăng nhập thành công, lưu phiên đăng nhập
                    sessionManager.createLoginSession(user.id);
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Đóng LoginActivity để người dùng không quay lại được bằng nút Back
    }
}