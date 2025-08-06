package com.example.asm_22;


import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<String> categories = new ArrayList<>();
    // Dùng Map để lưu trữ ngân sách đã thiết lập, key là tên danh mục
    private Map<String, Budget> budgetMap = new HashMap<>();

    // Interface để báo cho Activity biết khi ngân sách thay đổi
    public interface OnBudgetChangedListener {
        void onBudgetChanged(String category, double newAmount);
    }
    private OnBudgetChangedListener listener;

    public void setOnBudgetChangedListener(OnBudgetChangedListener listener) {
        this.listener = listener;
    }

    // Cập nhật dữ liệu cho Adapter
    public void setData(List<String> categories, List<Budget> budgets) {
        this.categories = categories;
        this.budgetMap.clear();
        for (Budget budget : budgets) {
            this.budgetMap.put(budget.category, budget);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewCategory;
        private final EditText editTextAmount;
        private TextWatcher textWatcher;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.text_view_budget_category);
            editTextAmount = itemView.findViewById(R.id.edit_text_budget_amount);
        }

        public void bind(String category) {
            // Gỡ bỏ TextWatcher cũ để tránh gọi lại không cần thiết
            if (textWatcher != null) {
                editTextAmount.removeTextChangedListener(textWatcher);
            }

            textViewCategory.setText(category);

            // Hiển thị số tiền ngân sách nếu đã có
            if (budgetMap.containsKey(category)) {
                editTextAmount.setText(String.valueOf(budgetMap.get(category).amount));
            } else {
                editTextAmount.setText("");
            }

            // Tạo TextWatcher mới
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (listener != null) {
                        double newAmount = 0;
                        try {
                            if (!s.toString().isEmpty()) {
                                newAmount = Double.parseDouble(s.toString());
                            }
                        } catch (NumberFormatException e) {
                            // Bỏ qua nếu nhập không phải là số
                        }
                        // Gửi sự kiện về Activity
                        listener.onBudgetChanged(category, newAmount);
                    }
                }
            };

            // Gắn TextWatcher vào EditText
            editTextAmount.addTextChangedListener(textWatcher);
        }
    }
}
