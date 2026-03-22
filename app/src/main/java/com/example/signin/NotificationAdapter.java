package com.example.signin;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter for displaying notifications in a RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    /**
     * Insert newest notification at top
     */
    public void addNotification(Notification n) {
        if (n == null) return;
        notifications.add(0, n);
        sortDescending();
        notifyItemInserted(0);
    }

    /**
     * Clear all notifications
     */
    public void clear() {
        notifications.clear();
        notifyDataSetChanged();
    }

    /**
     * Always keep list sorted newest → oldest
     */
    private void sortDescending() {
        Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Format timestamp
        String formattedTime = formatTimestamp(notification.getTimestamp());
        holder.tvTime.setText(formattedTime);

        String type = safe(notification.getType());
        String status = safe(notification.getStatus());
        String title;
        if ("arrival_update".equalsIgnoreCase(type)) {
            title = "Arrival Update";
            if ("awaiting_arrival".equalsIgnoreCase(status)) {
                holder.tvReason.setText("Proceed to Counter: " + safe(notification.getCounterName()));
            } else if ("in_service".equalsIgnoreCase(status)) {
                holder.tvReason.setText("Now being served at " + safe(notification.getCounterName()));
            } else if (notification.getChainOrder() > 0) {
                holder.tvReason.setText(String.format(Locale.getDefault(),
                        "Chained Queue #%d — wait for your turn", notification.getChainOrder()));
            } else {
                holder.tvReason.setText(notification.getMessage());
            }
        } else if ("appointment".equalsIgnoreCase(type)) {
            title = "Appointment Update";
            holder.tvReason.setText(notification.getMessage());
        } else {
            title = "Notification";
            holder.tvReason.setText(notification.getMessage());
        }

        holder.tvType.setText(title);
        String estPart = safe(notification.getEstName());
        String counterPart = safe(notification.getCounterName());
        holder.tvEstName.setText(counterPart.equals("-") ? estPart : estPart + " • " + counterPart);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Null-safe utility method for strings.
     */
    private String safe(String s) {
        return s == null ? "-" : s;
    }

    /**
     * Format epoch timestamp to readable time.
     */
    private String formatTimestamp(long epochMillis) {
        if (epochMillis <= 0) return "-";

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(epochMillis));
    }

    /**
     * ViewHolder for displaying each notification
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvEstName, tvReason, tvTime;

        ViewHolder(View view) {
            super(view);
            tvType = view.findViewById(R.id.tvType);
            tvEstName = view.findViewById(R.id.tvEstName);
            tvReason = view.findViewById(R.id.tvReason);
            tvTime = view.findViewById(R.id.tvTime);
        }
    }
}
