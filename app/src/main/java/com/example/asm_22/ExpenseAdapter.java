package com.example.asm_22;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private OnItemClickListener listener; // Biến cho listener

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense currentExpense = expenses.get(position);
        holder.textViewDescription.setText(currentExpense.description);
        holder.textViewCategory.setText(currentExpense.category);

        DecimalFormat formatter = new DecimalFormat("#,###,### đ");
        String formattedAmount = formatter.format(currentExpense.amount);
        holder.textViewAmount.setText(formattedAmount);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateString = dateFormat.format(new Date(currentExpense.date));
        holder.textViewDate.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    // Phương thức để lấy expense tại một vị trí cụ thể (dùng cho swipe)
    public Expense getExpenseAt(int position) {
        return expenses.get(position);
    }

    // ViewHolder
    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewDescription;
        private final TextView textViewAmount;
        private final TextView textViewCategory;
        private final TextView textViewDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewAmount = itemView.findViewById(R.id.text_view_amount);
            textViewCategory = itemView.findViewById(R.id.text_view_category);
            textViewDate = itemView.findViewById(R.id.text_view_date);

            // Bắt sự kiện click trên toàn bộ item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(expenses.get(position));
                }
            });
        }
    }

    // Interface để gửi sự kiện click ra bên ngoài (MainActivity)
    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    // Phương thức để MainActivity đăng ký listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
