package com.example.asm_22;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.ViewHolder> {

    private List<RecurringExpense> recurringExpenses = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurring_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringExpense current = recurringExpenses.get(position);
        holder.description.setText(current.description);

        DecimalFormat formatter = new DecimalFormat("#,###,### đ");
        holder.amount.setText(formatter.format(current.amount));

        String details = String.format("Hàng tháng vào ngày %d, thuộc danh mục %s", current.dayOfMonth, current.category);
        holder.details.setText(details);
    }

    @Override
    public int getItemCount() { return recurringExpenses.size(); }

    public void setRecurringExpenses(List<RecurringExpense> recurringExpenses) {
        this.recurringExpenses = recurringExpenses;
        notifyDataSetChanged();
    }

    public RecurringExpense getRecurringExpenseAt(int position) {
        return recurringExpenses.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView description, amount, details;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.text_view_rec_description);
            amount = itemView.findViewById(R.id.text_view_rec_amount);
            details = itemView.findViewById(R.id.text_view_rec_details);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(recurringExpenses.get(pos));
                }
            });
        }
    }

    public interface OnItemClickListener { void onItemClick(RecurringExpense recurringExpense); }
    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }
}
