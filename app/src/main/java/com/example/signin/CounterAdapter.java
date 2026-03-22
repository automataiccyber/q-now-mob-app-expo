package com.example.signin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying counters within an establishment.
 */
public class CounterAdapter extends RecyclerView.Adapter<CounterAdapter.ViewHolder> {

    private List<CounterItem> counters = new ArrayList<>();
    private OnCounterClickListener listener;

    public interface OnCounterClickListener {
        void onClick(CounterItem item);
    }

    public CounterAdapter(List<CounterItem> counters, OnCounterClickListener listener) {
        if (counters != null) this.counters = counters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CounterItem item = counters.get(position);
        holder.text1.setText(safe(item.getEstName()));
        // Combine counter name + average queue time
        String queueInfo = safe(item.getCounterName());
        if(item.getAvgQueueTime() != null && !item.getAvgQueueTime().isEmpty()){
            queueInfo += " • Avg: " + item.getAvgQueueTime() + " mins";
        }
        holder.text2.setText(queueInfo);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }


    @Override
    public int getItemCount() {
        return counters != null ? counters.size() : 0;
    }

    /** Replace the list of counters */
    public void updateList(List<CounterItem> newList) {
        counters = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }

    private static String safe(String s) { return s != null ? s : "-"; }
}
