package com.example.signin;

import java.util.List;

/**
 * Represents an appointment between a patient and an establishment.
 */
public class Appointment {
    public String appointmentId;
    public String estId;
    public String counterName;
    public String counterId;
    public String estName; // for display in RecyclerView
    public String patientId;
    public String patientName;
    public String service;
    public String reason;
    public String status; // e.g., "pending", "accepted", "completed"
    public long timestamp;
    public List<Slot> slots; // Optional scheduled slots

    public Appointment() { }

    /**
     * Represents a scheduled date and time slot for the appointment
     */
    public static class Slot {
        public String date; // Format: yyyy-MM-dd or user-friendly
        public String time; // Format: HH:mm or user-friendly

        public Slot() { }
    }
}
