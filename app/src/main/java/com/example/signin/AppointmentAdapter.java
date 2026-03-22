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
 * Adapter for displaying a list of Appointment objects in a RecyclerView.
 */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();

    /** Listener for appointment item clicks */
    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private OnAppointmentClickListener listener;

    /** Set click listener */
    public void setOnAppointmentClickListener(OnAppointmentClickListener listener) {
        this.listener = listener;
    }

    /** Replace the current list of appointments */
    public void setAppointments(List<Appointment> list) {
        this.appointments = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_request, parent, false);
        return new AppointmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appt = appointments.get(position);

        // Type
        String type = "Appointment";
        if (appt.status != null && !appt.status.isEmpty()) type += " (" + appt.status + ")";

        String estName = appt.estName != null ? appt.estName : "Loading...";
        String counter = appt.counterName != null ? appt.counterName : "-";

        // Time/slots
        StringBuilder slotInfo = new StringBuilder();
        if (appt.slots != null && !appt.slots.isEmpty()) {
            for (Appointment.Slot s : appt.slots) {
                if (slotInfo.length() > 0) slotInfo.append(" | ");
                slotInfo.append(s.date).append(" ").append(s.time);
            }
        } else {
            slotInfo.append("Pending scheduling");
        }
        holder.tvType.setText(type);
        holder.tvEstName.setText(estName + " - " + counter);
        holder.tvReason.setText("Service: " + (appt.service != null ? appt.service : "-"));
        holder.tvTime.setText(slotInfo.toString());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAppointmentClick(appt);
        });
    }
    
    @Override
    public int getItemCount() {
        return appointments.size();
    }

    /** Remove appointment by establishment ID */
    public void removeAppointmentByEstId(String estId) {
        if (estId == null) return;
        for (int i = 0; i < appointments.size(); i++) {
            if (estId.equals(appointments.get(i).estId)) {
                appointments.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvEstName, tvReason, tvTime;
        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvEstName = itemView.findViewById(R.id.tvEstName);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
