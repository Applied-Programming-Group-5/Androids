package com.example.asm_22;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportActivity extends AppCompatActivity {

    private PieChart pieChart;
    private Spinner spinnerMonth, spinnerYear;
    private TextView tvNoData;
    private AppDatabase db;
    private SessionManager sessionManager; // THÊM MỚI
    private int currentUserId; // THÊM MỚI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = AppDatabase.getDatabase(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext()); // KHỞI TẠO
        currentUserId = sessionManager.getUserId(); // LẤY USER ID

        Toolbar toolbar = findViewById(R.id.toolbar_report);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        pieChart = findViewById(R.id.pie_chart);
        spinnerMonth = findViewById(R.id.spinner_report_month);
        spinnerYear = findViewById(R.id.spinner_report_year);
        tvNoData = findViewById(R.id.text_view_no_data);

        setupSpinners();
        setupPieChart();
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.getLegend().setEnabled(false); // Ẩn chú thích, vì sẽ hiển thị trên biểu đồ
    }

    private void setupSpinners() {
        // Tương tự như màn hình BudgetManagement
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        spinnerYear.setSelection(years.indexOf(String.valueOf(currentYear)), false);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH), false);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadReportData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerMonth.setOnItemSelectedListener(listener);
        spinnerYear.setOnItemSelectedListener(listener);

        loadReportData(); // Tải dữ liệu lần đầu
    }

    private void loadReportData() {
        int year = Integer.parseInt((String) spinnerYear.getSelectedItem());
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        String yearStr = String.valueOf(year);
        String monthStr = String.format(Locale.US, "%02d", month);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<CategorySpending> spendingData = db.expenseDao().getSpendingByCategoryForMonth(currentUserId, yearStr, monthStr);
            runOnUiThread(() -> {
                if (spendingData == null || spendingData.isEmpty()) {
                    pieChart.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                } else {
                    pieChart.setVisibility(View.VISIBLE);
                    tvNoData.setVisibility(View.GONE);
                    populatePieChart(spendingData);
                }
            });
        });
    }

    private void populatePieChart(List<CategorySpending> data) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (CategorySpending item : data) {
            entries.add(new PieEntry((float) item.totalAmount, item.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu theo Danh mục");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.invalidate(); // Vẽ lại biểu đồ
        pieChart.animateY(1400); // Thêm hiệu ứng animation
    }
}