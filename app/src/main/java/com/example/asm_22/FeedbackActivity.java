package com.example.asm_22;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedbackActivity extends AppCompatActivity {

    private TextInputEditText etSubject, etBody;
    private AppDatabase db;
    private SessionManager sessionManager;
    private String userEmail = ""; // Biến để lưu email người dùng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        db = AppDatabase.getDatabase(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        // ... thiết lập Toolbar ...

        etSubject = findViewById(R.id.edit_text_feedback_subject);
        etBody = findViewById(R.id.edit_text_feedback_body);
        Button btnSend = findViewById(R.id.button_send_feedback);

        loadUserEmail(); // Tải email người dùng
        btnSend.setOnClickListener(v -> sendFeedbackEmail());
    }

    private void loadUserEmail() {
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                User user = db.userDao().getById(userId); // Cần thêm getById vào UserDao
                if (user != null) {
                    userEmail = user.email;
                }
            });
        }
    }

    private void sendFeedbackEmail() {
        String subject = etSubject.getText().toString().trim();
        String body = etBody.getText().toString().trim();
        // ... kiểm tra input ...

        // Nối thêm thông tin người dùng vào nội dung email
        String fullBody = body + "\n\n----------------\n" + "Phản hồi từ người dùng: " + userEmail;

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String supportEmail = "your_support_email@example.com"; // Thay đổi email của bạn ở đây
        String mailto = "mailto:" + supportEmail +
                "?subject=" + Uri.encode("[Phản hồi] " + subject) +
                "&body=" + Uri.encode(fullBody);

        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng email nào.", Toast.LENGTH_LONG).show();
        }
    }
}