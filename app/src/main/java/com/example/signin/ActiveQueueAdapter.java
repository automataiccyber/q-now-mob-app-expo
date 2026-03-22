package com.example.signin;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for displaying active queue items in a RecyclerView.
 * Automatically updates elapsed times every second for waiting/in-service items.
 */
public class ActiveQueueAdapter extends RecyclerView.Adapter<ActiveQueueAdapter.ViewHolder> {

    private final List<ActiveQueueItem> items = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;

    // Runnable to refresh elapsed times every second
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!items.isEmpty()) notifyDataSetChanged();
            handler.postDelayed(this, 1000);
        }
    };

    public ActiveQueueAdapter() { }

    /**
     * Adds a new queue item at the top.
     */
    public void addQueue(ActiveQueueItem item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    /**
     * Clears all queue items.
     */
    public void clear() {
        int size = items.size();
        if (size == 0) return;
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * Starts the live timer updates.
     */
    public void startTimer() {
        if (!running) {
            running = true;
            handler.post(ticker);
        }
    }

    /**
     * Stops the live timer updates.
     */
    public void stopTimer() {
        if (running) {
            handler.removeCallbacks(ticker);
            running = false;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveQueueItem item = items.get(position);

        long now = System.currentTimeMillis();
        String label;
        long baseTime = 0;

        // Determine label and base time for elapsed timer based on status
        switch (item.getStatus()) {
            case "in_service":
                label = "Serving";
                baseTime = item.getServiceStartTime() > 0 ? item.getServiceStartTime() : item.getJoinTime();
                break;
            case "waiting":
                label = "Waiting";
                baseTime = item.getJoinTime();
                break;
            case "awaiting_arrival":
                label = "Awaiting Arrival";
                baseTime = 0; // No timer
                break;
            default:
                label = safe(item.getStatus());
                baseTime = 0; // No timer
        }

        // Calculate elapsed time
        long elapsed = baseTime > 0 ? now - baseTime : 0;

        // Bind data to unified item layout
        holder.tvType.setText(String.format(Locale.getDefault(), "%s%s",
                label, item.getChainOrder() > 0 ? " • Chain #" + item.getChainOrder() : ""));
        holder.tvEstName.setText(String.format(Locale.getDefault(),
                "%s - %s", safe(item.getEstName()), safe(item.getCounterName())));
        holder.tvReason.setText(String.format(Locale.getDefault(),
                "Reason: %s", safe(item.getReason())));
        holder.tvTime.setText(baseTime > 0
                ? String.format(Locale.getDefault(), "⏱ %s", formatElapsed(elapsed))
                : "⏱ --:--");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class for queue items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvEstName, tvReason, tvTime;

        ViewHolder(@NonNull View view) {
            super(view);
            tvType = view.findViewById(R.id.tvType);
            tvEstName = view.findViewById(R.id.tvEstName);
            tvReason = view.findViewById(R.id.tvReason);
            tvTime = view.findViewById(R.id.tvTime);
        }
    }

    /**
     * Null-safe string.
     */
    private static String safe(String s) {
        return s == null ? "-" : s;
    }

    /**
     * Converts milliseconds to formatted HH:mm:ss or mm:ss.
     */
    private static String formatElapsed(long millis) {
        if (millis < 0) millis = 0;

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(millis));

        if (hours > 0)
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
