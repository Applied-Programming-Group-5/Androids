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

public class FeedbackActivity extends AppCompatActivity {

    private TextInputEditText etSubject, etBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = findViewById(R.id.toolbar_feedback);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSubject = findViewById(R.id.edit_text_feedback_subject);
        etBody = findViewById(R.id.edit_text_feedback_body);
        Button btnSend = findViewById(R.id.button_send_feedback);

        btnSend.setOnClickListener(v -> sendFeedbackEmail());
    }

    private void sendFeedbackEmail() {
        String subject = etSubject.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(body)) {
            Toast.makeText(this, "Vui lòng nhập cả tiêu đề và nội dung.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một Intent để mở ứng dụng email
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

        // Địa chỉ email của đội ngũ hỗ trợ (BudgetWise Solutions)
        String supportEmail = "khanh911738154a1@gmail.com";
        String mailto = "mailto:" + supportEmail +
                "?subject=" + Uri.encode("[Phản hồi] " + subject) +
                "&body=" + Uri.encode(body);

        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            // Xử lý trường hợp người dùng không cài đặt ứng dụng email nào
            Toast.makeText(this, "Không tìm thấy ứng dụng email nào.", Toast.LENGTH_LONG).show();
        }
    }
}