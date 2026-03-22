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
 * Adapter to display available queues in a RecyclerView.
 */
public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private List<QueueItem> queues;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(QueueItem item, int position);
    }

    public QueueAdapter(List<QueueItem> queues, OnItemClickListener listener) {
        this.queues = (queues != null) ? queues : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        QueueItem item = queues.get(position);
        holder.textView.setText(item.getDisplayName() != null ? item.getDisplayName() : "Unnamed Queue");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return (queues != null) ? queues.size() : 0;
    }

    public void updateList(List<QueueItem> newList) {
        this.queues = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public QueueItem getItem(int position) {
        if (queues == null || position < 0 || position >= queues.size()) return null;
        return queues.get(position);
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        QueueViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
