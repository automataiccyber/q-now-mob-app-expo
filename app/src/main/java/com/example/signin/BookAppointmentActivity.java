package com.example.signin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class BookAppointmentActivity extends AppCompatActivity {

    private Spinner spnServices;
    private EditText etReason;
    private Button btnRequest;
    private TableLayout tableSchedule;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    private String estId, estName;
    private final List<String> services = new ArrayList<>();

    // Schedule Logic
    private final Set<String> selectedSlotIds = new HashSet<>(); // "YYYY-MM-DD_HH:MM"
    private final Map<String, Slot> slotMap = new HashMap<>(); // ID -> Slot object
    private final Map<String, String> bookedSlots = new HashMap<>(); // ID -> status

    private static class Slot {
        String date; // YYYY-MM-DD
        String time; // HH:mm
        String displayTime;
        String id; // YYYY-MM-DD_HH:MM

        public Slot(String date, String time, String displayTime) {
            this.date = date;
            this.time = time;
            this.displayTime = displayTime;
            this.id = date + "_" + time;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        spnServices = findViewById(R.id.spnServices);
        etReason = findViewById(R.id.etReason);
        btnRequest = findViewById(R.id.btnRequest);
        tableSchedule = findViewById(R.id.tableSchedule);

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        estId = getIntent().getStringExtra("estId");
        estName = getIntent().getStringExtra("estName");

        setTitle("Book at " + estName);

        loadServices(estId);
        fetchEstablishmentHours(estId);

        btnRequest.setOnClickListener(v -> handleRequest());
    }

    // ---------------------------
    // Load services (Existing Logic)
    // ---------------------------
    private void loadServices(String estId) {
        dbRef.child("establishments").child(estId).child("services")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        services.clear();
                        for (DataSnapshot serviceSnap : snapshot.getChildren()) {
                            String serviceName = serviceSnap.getValue(String.class);
                            if (serviceName != null) services.add(serviceName);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                BookAppointmentActivity.this,
                                android.R.layout.simple_spinner_item,
                                services
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnServices.setAdapter(adapter);

                        spnServices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                dbRef.child("establishments").child(estId).child("appointments")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                resetAllCells();
                                                bookedSlots.clear();
                                                if (snapshot.exists()) {
                                                    String selectedService = getSelectedService();
                                                    for (DataSnapshot apptSnap : snapshot.getChildren()) {
                                                        String status = apptSnap.child("status").getValue(String.class);
                                                        String apptService = apptSnap.child("service").getValue(String.class);
                                                        if (status != null && (status.equals("approved") || status.equals("accepted") || status.equals("confirmed") || status.equals("scheduled") || status.equals("rescheduled"))) {
                                                            if (selectedService != null && apptService != null && apptService.equalsIgnoreCase(selectedService)) {
                                                                DataSnapshot slotsSnap = apptSnap.child("slots");
                                                                if (slotsSnap.exists()) {
                                                                    for (DataSnapshot s : slotsSnap.getChildren()) {
                                                                        String d = s.child("date").getValue(String.class);
                                                                        String t = s.child("time").getValue(String.class);
                                                                        if (d != null && t != null) {
                                                                            String key = d + "_" + t;
                                                                            bookedSlots.put(key, status);
                                                                            updateCellStatus(key, status);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookAppointmentActivity.this,
                                "Failed to load services", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------------------
    // Schedule / Calendar Logic
    // ---------------------------
    private void fetchEstablishmentHours(String estId) {
        dbRef.child("establishments").child(estId).child("hours").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String hours = snapshot.getValue(String.class);
                if (hours == null) hours = "08:00-17:00"; // Default
                parseAndBuildSchedule(hours);
                listenAppointmentsForEst(estId);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void parseAndBuildSchedule(String hours) {
        int openHour = 8;
        int closeHour = 17;
        List<Integer> allowedDays = new ArrayList<>(Arrays.asList(1,2,3,4,5,6)); // Mon-Sat default

        // Regex parsing similar to JS
        try {
            if (hours.contains("-")) {
                String[] parts = hours.split("-");
                String[] start = parts[0].trim().split(":");
                String[] end = parts[1].trim().split(":");
                openHour = Integer.parseInt(start[0]);
                closeHour = Integer.parseInt(end[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        buildScheduleGrid(openHour, closeHour, allowedDays);
    }

    private void buildScheduleGrid(int openHour, int closeHour, List<Integer> allowedDays) {
        tableSchedule.removeAllViews();
        slotMap.clear();
        selectedSlotIds.clear();

        // Generate Dates (Next 14 days)
        List<Calendar> days = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 14; i++) {
            Calendar d = (Calendar) cal.clone();
            d.add(Calendar.DAY_OF_YEAR, i);
            int javaDay = d.get(Calendar.DAY_OF_WEEK); // 1=Sun, 2=Mon...
            int jsDay = javaDay - 1; // 0=Sun...
            if (allowedDays.contains(jsDay)) {
                days.add(d);
            }
        }

        // Header Row
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Time");
        SimpleDateFormat sdfHeader = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        for (Calendar d : days) {
            addHeaderCell(headerRow, sdfHeader.format(d.getTime()));
        }
        tableSchedule.addView(headerRow);

        // Body Rows (Time Slots)
        for (int h = openHour; h < closeHour; h++) {
            for (int half = 0; half < 2; half++) {
                TableRow row = new TableRow(this);

                // Time Label
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, half * 30);
                String displayTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                        (h > 12 ? h - 12 : (h == 0 || h == 12 ? 12 : h)),
                        half * 30,
                        (h >= 12 ? "PM" : "AM"));

                addCell(row, displayTime, true);

                // Date Cells
                for (Calendar d : days) {
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dateKey = sdfDate.format(d.getTime());

                    Slot slot = new Slot(dateKey, timeStr, displayTime);
                    slotMap.put(slot.id, slot);

                    addSlotCell(row, slot);
                }
                tableSchedule.addView(row);
            }
        }
    }

    private void addHeaderCell(TableRow row, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(24, 24, 24, 24);
        tv.setBackgroundColor(Color.parseColor("#0077cc"));
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        
        // Add border margin
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(1, 1, 1, 1);
        tv.setLayoutParams(params);
        
        row.addView(tv);
    }

    private void addCell(TableRow row, String text, boolean isLabel) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(24, 24, 24, 24);
        tv.setBackgroundColor(Color.parseColor("#F0F4F8"));
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(1, 1, 1, 1);
        tv.setLayoutParams(params);
        
        row.addView(tv);
    }

    private void addSlotCell(TableRow row, Slot slot) {
        TextView tv = new TextView(this);
        tv.setText("Available");
        tv.setPadding(24, 24, 24, 24);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.parseColor("#FFFFFF")); // Default white
        tv.setTag(slot.id);
        tv.setTextSize(12);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(1, 1, 1, 1);
        tv.setLayoutParams(params);

        tv.setOnClickListener(v -> toggleSlot(tv, slot));

        row.addView(tv);
    }

    private void toggleSlot(TextView tv, Slot slot) {
        if (bookedSlots.containsKey(slot.id)) return; // Already booked

        if (selectedSlotIds.contains(slot.id)) {
            selectedSlotIds.remove(slot.id);
            tv.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tv.setText("Available");
        } else {
            selectedSlotIds.add(slot.id);
            tv.setBackgroundColor(Color.parseColor("#ADD8E6")); // Selected (Light Blue)
            tv.setText("Selected");
        }
    }

    private void listenAppointmentsForEst(String estId) {
        dbRef.child("establishments").child(estId).child("appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resetAllCells();
                bookedSlots.clear();
                if (snapshot.exists()) {
                    String selectedService = getSelectedService();
                    for (DataSnapshot apptSnap : snapshot.getChildren()) {
                        String status = apptSnap.child("status").getValue(String.class);
                        String apptService = apptSnap.child("service").getValue(String.class);
                        if (status != null && (status.equals("approved") || status.equals("accepted") || status.equals("confirmed") || status.equals("scheduled") || status.equals("rescheduled"))) {
                            if (selectedService != null && apptService != null && apptService.equalsIgnoreCase(selectedService)) {
                                DataSnapshot slotsSnap = apptSnap.child("slots");
                                if (slotsSnap.exists()) {
                                    for (DataSnapshot s : slotsSnap.getChildren()) {
                                        String d = s.child("date").getValue(String.class);
                                        String t = s.child("time").getValue(String.class);
                                        if (d != null && t != null) {
                                            String key = d + "_" + t;
                                            bookedSlots.put(key, status);
                                            updateCellStatus(key, status);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void resetAllCells() {
        for (String id : slotMap.keySet()) {
            View v = tableSchedule.findViewWithTag(id);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setBackgroundColor(Color.parseColor("#FFFFFF"));
                tv.setText("Available");
                tv.setClickable(true);
            }
        }
    }

    private String getSelectedService() {
        Object item = spnServices.getSelectedItem();
        return item != null ? item.toString() : "";
    }

    private void updateCellStatus(String slotId, String status) {
        View v = tableSchedule.findViewWithTag(slotId);
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setBackgroundColor(Color.parseColor("#D4EDDA")); // Booked Green
            tv.setText("Booked");
            tv.setClickable(false);
            if (selectedSlotIds.contains(slotId)) {
                selectedSlotIds.remove(slotId);
            }
        }
    }

    // ---------------------------
    // Handle Request
    // ---------------------------
    private void handleRequest() {
        String service = spnServices.getSelectedItem() != null
                ? spnServices.getSelectedItem().toString() : "";
        String reason = etReason.getText().toString().trim();

        if (service.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSlotIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one time slot.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate consecutives (Optional but good for "same design")
        if (!areSlotsConsecutive()) {
            Toast.makeText(this, "Selected slots must be consecutive.", Toast.LENGTH_SHORT).show();
            return;
        }

        sendAppointmentRequest(estId, service, reason);
    }

    private boolean areSlotsConsecutive() {
        if (selectedSlotIds.size() <= 1) return true;
        List<Slot> sorted = new ArrayList<>();
        for (String id : selectedSlotIds) sorted.add(slotMap.get(id));
        
        // Sort by time
        Collections.sort(sorted, (a, b) -> {
            int dateCmp = a.date.compareTo(b.date);
            if (dateCmp != 0) return dateCmp;
            return a.time.compareTo(b.time);
        });

        // Check if all same date
        String firstDate = sorted.get(0).date;
        for (Slot s : sorted) {
            if (!s.date.equals(firstDate)) return false; // Must be same day
        }

        // Check time continuity (30 min steps)
        for (int i = 1; i < sorted.size(); i++) {
            int prev = toMinutes(sorted.get(i-1).time);
            int curr = toMinutes(sorted.get(i).time);
            if (curr - prev != 30) return false;
        }
        return true;
    }

    private int toMinutes(String time) {
        String[] p = time.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private void sendAppointmentRequest(String estId, String service, String reason) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        String apptId = dbRef.child("establishments").child(estId).child("appointments").push().getKey();
        if (apptId == null) return;

        long timestamp = System.currentTimeMillis();

        dbRef.child("patients").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot patientSnap) {

                        String patientName = patientSnap.child("name").getValue(String.class);
                        if (patientName == null) patientName = "Unknown";

                        String contact = patientSnap.child("contactNumber").getValue(String.class);
                        if (contact == null) contact = "";

                        // Prepare slots list
                        List<Map<String, String>> slotsList = new ArrayList<>();
                        List<Slot> sortedSlots = new ArrayList<>();
                        for(String id : selectedSlotIds) sortedSlots.add(slotMap.get(id));
                        
                        Collections.sort(sortedSlots, (a, b) -> (a.date + a.time).compareTo(b.date + b.time));

                        for (Slot s : sortedSlots) {
                            Map<String, String> sm = new HashMap<>();
                            sm.put("date", s.date);
                            sm.put("time", s.time);
                            slotsList.add(sm);
                        }

                        Map<String, Object> apptData = new HashMap<>();
                        apptData.put("appointmentId", apptId);
                        apptData.put("patientId", userId);
                        apptData.put("estId", estId);
                        apptData.put("estName", estName);
                        apptData.put("service", service);
                        apptData.put("reason", reason);
                        apptData.put("status", "pending");
                        apptData.put("timestamp", timestamp);
                        apptData.put("patientName", patientName);
                        apptData.put("contactNumber", contact);
                        apptData.put("slots", slotsList);
                        // requestedDate is just the first date
                        apptData.put("requestedDate", sortedSlots.get(0).date); 

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("establishments/" + estId + "/appointments/" + apptId, apptData);
                        updates.put("patients/" + userId + "/appointments/" + apptId, apptData);

                        dbRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(BookAppointmentActivity.this,
                                            "Appointment request sent!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(BookAppointmentActivity.this,
                                            "Failed to send request", Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
