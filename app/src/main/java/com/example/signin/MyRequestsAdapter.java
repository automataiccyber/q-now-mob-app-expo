package com.example.signin;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyRequestsAdapter extends RecyclerView.Adapter<MyRequestsAdapter.VH> {

    private final List<RequestItem> requests = new ArrayList<>();

    public void setRequests(List<RequestItem> newList) {
        requests.clear();
        requests.addAll(newList);
        notifyDataSetChanged();
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
        RequestItem item = requests.get(position);

        // 🔹 Title: Establishment and counter (if applicable)
        if ("Queue Request".equals(item.type)) {
            String est = item.estName != null ? item.estName : "-";
            String counter = item.counterName != null ? item.counterName : "-";
            holder.tvEstName.setText(est + " - " + counter);
        } else {
            holder.tvEstName.setText(item.estName != null ? item.estName : "-");
        }

        holder.tvType.setText(item.type);
        holder.tvReason.setText("Reason: " + (item.reason != null ? item.reason : "-"));
        holder.tvTime.setText("Time: " + MyRequestsActivity.formatTime(item.timestamp));

        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Remove Request?")
                    .setMessage("Are you sure you want to cancel this " + item.type + "?")
                    .setPositiveButton("Yes", (d, w) -> removeRequest(item, v))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    /**
     * Remove a request from both establishment and patient branches
     */
    private void removeRequest(RequestItem item, View contextView) {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;
            if (userId == null) {
                Toast.makeText(contextView.getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();

            if ("Queue Request".equals(item.type)
                    && item.estId != null && item.counterId != null && item.requestKey != null) {

                // Delete from establishment requests
                updates.put("/establishments/" + item.estId +
                        "/counters/" + item.counterId +
                        "/requests/" + item.requestKey, null);

                // Delete from patient’s queueRequests
                updates.put("/patients/" + userId +
                        "/queueRequests/" + item.requestKey, null);

            } else if ("Appointment Request".equals(item.type)
                    && item.estId != null && item.appointmentKey != null) {

                // Delete from establishment appointments
                updates.put("/establishments/" + item.estId +
                        "/appointments/" + item.appointmentKey, null);

                // Delete from patient appointments
                updates.put("/patients/" + userId +
                        "/appointments/" + item.appointmentKey, null);
            }

            if (!updates.isEmpty()) {
                FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            removeItemFromList(item);
                            Toast.makeText(contextView.getContext(),
                                    "Request removed successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(contextView.getContext(),
                                "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(contextView.getContext(), "Error removing request", Toast.LENGTH_SHORT).show();
        }
    }

    /** Remove item from list immediately after successful cancellation. */
    private void removeItemFromList(RequestItem item) {
        int idx = requests.indexOf(item);
        if (idx >= 0) {
            requests.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvEstName, tvReason, tvTime;

        VH(@NonNull View v) {
            super(v);
            tvType = v.findViewById(R.id.tvType);
            tvEstName = v.findViewById(R.id.tvEstName);
            tvReason = v.findViewById(R.id.tvReason);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
}
