package com.example.signin;

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
 * RecyclerView Adapter to show the list of recently visited queues or appointments.
 */
public class RecentlyVisitedAdapter extends RecyclerView.Adapter<RecentlyVisitedAdapter.VH> {

    private final List<VisitedItem> items = new ArrayList<>();

    public RecentlyVisitedAdapter() {}

    /** Add a new visited item to the top of the list */
    public void addVisited(VisitedItem v) {
        if (v == null) return;
        items.add(0, v);
        notifyItemInserted(0);
    }

    /** Clear all visited items */
    public void clear() {
        int s = items.size();
        if (s == 0) return;
        items.clear();
        notifyItemRangeRemoved(0, s);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        VisitedItem item = items.get(position);

        // Type
        holder.tvType.setText("Visited");

        // Establishment + Counter
        holder.tvEstName.setText(String.format(Locale.getDefault(),
                "%s - %s", safe(item.getEstName()), safe(item.getCounterName())));

        // Waited duration
        String waited = formatMillis(item.getWaitMillis());

        // Date of visit in Manila timezone
        java.text.DateFormat manilaFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        manilaFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
        String visitedAt = manilaFormat.format(new java.util.Date(item.getVisitedAt()));

        // Reason and time
        holder.tvReason.setText(String.format(Locale.getDefault(), "Waited: %s", waited));
        holder.tvTime.setText(String.format(Locale.getDefault(), "Visited: %s", visitedAt));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvEstName, tvReason, tvTime;
        VH(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvEstName = itemView.findViewById(R.id.tvEstName);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private static String safe(String s) {
        return s != null ? s : "-";
    }

    private static String formatMillis(long ms) {
        if (ms <= 0) return "0s";
        long hours = TimeUnit.MILLISECONDS.toHours(ms);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));
        if (hours > 0) return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        if (minutes > 0) return String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds);
        return String.format(Locale.getDefault(), "%ds", seconds);
    }
}
