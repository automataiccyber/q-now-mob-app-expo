package com.example.signin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    // UI Elements
    private Button btnLogout, btnUpdateInfo, btnJoinQueue, btnBookAppointment, btnMyRequests, btnInbox;
    private ImageButton btnMenu;
    private TextView userName;

    // Firebase References
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private FirebaseFirestore firestore;

    // RecyclerView Adapters
    private ActiveQueueAdapter activeQueueAdapter;
    private RecentlyVisitedAdapter recentlyVisitedAdapter;
    private AppointmentAdapter appointmentAdapter;
    private NotificationAdapter notificationAdapter;
    private final List<Notification> notificationList = new ArrayList<>();

    // RecyclerViews
    private RecyclerView rvActiveQueues, rvNotifications, rvRecentlyVisited, rvUpcomingAppointments;

    // Notification Listener
    private ChildEventListener notificationListener;
    private Query notificationQuery;

    // ArrivalIndex
    private ValueEventListener arrivalIndexListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.availability);

        // Handle window insets for devices with notches
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.availability), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "QNOW_CHANNEL", "Q-Now Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Bind UI elements and initialize RecyclerViews
        bindViews();
        setupRecyclerViews();

        // Setup button listeners and load data
        setupButtonListeners();
        setupArrivalIndexListeners();
        setupNotificationListener();
        setupUpcomingAppointmentsListener();
        setupRecentlyVisitedListener();
    }

    private void showAppointmentActionDialog(@NonNull Appointment appt) {
        String[] actions = new String[]{"Request Reschedule", "Cancel Appointment"};
        new AlertDialog.Builder(this)
                .setTitle("Manage Appointment")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        requestReschedule(appt);
                    } else if (which == 1) {
                        confirmAndCancelAppointment(appt);
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void requestReschedule(@NonNull Appointment appt) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        if (appt.estId == null || appt.appointmentId == null) {
            Toast.makeText(this, "Missing appointment data", Toast.LENGTH_SHORT).show();
            return;
        }
        long now = System.currentTimeMillis();
        Map<String, Object> req = new HashMap<>();
        req.put("appointmentId", appt.appointmentId);
        req.put("patientId", uid);
        req.put("status", "pending");
        req.put("createdAt", now);
        if (appt.service != null) req.put("service", appt.service);
        if (appt.reason != null) req.put("reason", appt.reason);
        dbRef.child("establishments").child(appt.estId)
                .child("rescheduleRequests").child(appt.appointmentId)
                .setValue(req)
                .addOnSuccessListener(v -> Toast.makeText(this, "Reschedule requested", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmAndCancelAppointment(@NonNull Appointment appt) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Cancel Appointment", (d, w) -> cancelAppointment(appt))
                .setNegativeButton("Keep", null)
                .show();
    }

    private void cancelAppointment(@NonNull Appointment appt) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        if (appt.estId == null || appt.appointmentId == null) {
            Toast.makeText(this, "Missing appointment data", Toast.LENGTH_SHORT).show();
            return;
        }
        long now = System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put("/establishments/" + appt.estId + "/appointments/" + appt.appointmentId + "/status", "cancelled");
        updates.put("/establishments/" + appt.estId + "/appointments/" + appt.appointmentId + "/cancelledAt", now);
        updates.put("/patients/" + uid + "/appointments/" + appt.appointmentId + "/status", "cancelled");
        updates.put("/patients/" + uid + "/appointments/" + appt.appointmentId + "/cancelledAt", now);
        dbRef.updateChildren(updates)
                .addOnSuccessListener(v -> Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void bindViews() {
        btnLogout = findViewById(R.id.logoutButton);
        btnUpdateInfo = findViewById(R.id.updateInfoButton);
        btnMenu = findViewById(R.id.btnMenu);
        btnJoinQueue = findViewById(R.id.btnJoinQueue);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnMyRequests = findViewById(R.id.btnMyRequests);
        btnInbox = findViewById(R.id.btnInbox);
        userName = findViewById(R.id.tvUsername);

        rvActiveQueues = findViewById(R.id.rvActiveQueues);
        rvNotifications = findViewById(R.id.rvNotifications); // new notifications RV in layout
        rvRecentlyVisited = findViewById(R.id.rvRecentlyVisited);
        rvUpcomingAppointments = findViewById(R.id.rvUpcomingAppointments);
    }

    private void setupRecyclerViews() {
        activeQueueAdapter = new ActiveQueueAdapter();
        recentlyVisitedAdapter = new RecentlyVisitedAdapter();
        appointmentAdapter = new AppointmentAdapter();
        notificationAdapter = new NotificationAdapter(notificationList);

        rvActiveQueues.setLayoutManager(new LinearLayoutManager(this));
        rvActiveQueues.setAdapter(activeQueueAdapter);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);

        rvRecentlyVisited.setLayoutManager(new LinearLayoutManager(this));
        rvRecentlyVisited.setAdapter(recentlyVisitedAdapter);

        rvUpcomingAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvUpcomingAppointments.setAdapter(appointmentAdapter);

        appointmentAdapter.setOnAppointmentClickListener(appointment -> {
            if (appointment == null) return;
            showAppointmentActionDialog(appointment);
        });
    }

    private void setupButtonListeners() {
        btnJoinQueue.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, JoinQueueActivity.class)));

        btnBookAppointment.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchEstablishmentActivity.class)));

        btnMyRequests.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MyRequestsActivity.class)));

        btnInbox.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, InboxActivity.class)));

        btnUpdateInfo.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, PatientInfo.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        });

        btnMenu.setOnClickListener(v -> toggleMenu());
    }

    private void toggleMenu() {
        int visibility = btnLogout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        btnLogout.setVisibility(visibility);
        btnUpdateInfo.setVisibility(visibility);
    }

    private void setupArrivalIndexListeners() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        arrivalIndexListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                activeQueueAdapter.clear();

                if (!snapshot.exists()) {
                    activeQueueAdapter.notifyDataSetChanged();
                    return;
                }

                String estId = snapshot.child("estId").getValue(String.class);
                if (estId == null) {
                    estId = snapshot.child("establishmentId").getValue(String.class); // legacy fallback
                }
                String counterId = snapshot.child("counterId").getValue(String.class);
                String requestKey = snapshot.child("requestKey").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);
                Long chainOrder = snapshot.child("chainOrder").getValue(Long.class);
                Long createdAt = snapshot.child("createdAt").getValue(Long.class);

                if (estId == null || counterId == null || requestKey == null || status == null) {
                    Log.w("HomeActivity", "arrivalIndex missing required fields");
                    activeQueueAdapter.notifyDataSetChanged();
                    return;
                }

                final String fEstId = estId;
                final String fCounterId = counterId;
                final String fRequestKey = requestKey;
                final String fStatus = status;
                final int chain = chainOrder != null ? chainOrder.intValue() : 0;
                final long timestamp = createdAt != null ? createdAt : 0L;

                DatabaseReference estRef = dbRef.child("establishments").child(fEstId);
                estRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot estSnap) {
                        String estName = estSnap.child("companyName").getValue(String.class);
                        if (estName == null) estName = estSnap.child("name").getValue(String.class);
                        if (estName == null) estName = "Unknown Establishment";

                        String counterName = estSnap.child("counters").child(fCounterId)
                                .child("name").getValue(String.class);
                        if (counterName == null) counterName = "Counter";

                        final String fEstName = estName;
                        final String fCounterName = counterName;

                        DatabaseReference patientReqRef = dbRef.child("patients").child(userId)
                                .child("queueRequests").child(fRequestKey);
                        patientReqRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot reqSnap) {
                                String reason = reqSnap.child("reason").getValue(String.class);
                                if (reason == null) reason = "-";
                                Long joinTimeVal = reqSnap.child("joinTime").getValue(Long.class);
                                long joinTime = joinTimeVal != null ? joinTimeVal : timestamp;

                                ActiveQueueItem item = new ActiveQueueItem(
                                        fEstId,
                                        fEstName,
                                        fCounterId,
                                        fCounterName,
                                        userId,
                                        reason,
                                        fRequestKey,
                                        null,
                                        fStatus,
                                        chain,
                                        joinTime,
                                        0L,
                                        timestamp
                                );

                                switch (fStatus) {
                                    case "awaiting_arrival":
                                        break;
                                    case "waiting":
                                    case "in_service":
                                        activeQueueAdapter.addQueue(item);
                                        break;
                                    default:
                                        Log.w("HomeActivity", "Unknown queue status: " + fStatus);
                                }

                                activeQueueAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("HomeActivity", "Failed to load queueRequests for active queue", error.toException());
                                activeQueueAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("HomeActivity", "Failed to load establishment for active queue", error.toException());
                        activeQueueAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeActivity", "Failed to load arrivalIndex: " + error.getMessage());
            }
        };

        dbRef.child("arrivalIndex").child(userId)
                .addValueEventListener(arrivalIndexListener);
    }

    // Load upcoming appointments from patients/{uid}/appointments
    private void setupUpcomingAppointmentsListener() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        dbRef.child("patients").child(uid).child("appointments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Appointment> upcoming = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Appointment appt = child.getValue(Appointment.class);
                            if (appt == null) continue;

                            // Filter to future/upcoming-like statuses (not rejected/completed)
                            String s = appt.status != null ? appt.status.toLowerCase(Locale.ROOT) : "";
                            if ("rejected".equals(s) || "completed".equals(s) || "cancelled".equals(s)) {
                                continue;
                            }
                            upcoming.add(appt);
                        }

                        appointmentAdapter.setAppointments(upcoming);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("HomeActivity", "Failed to load upcoming appointments", error.toException());
                    }
                });
    }

    // Load recently visited centers from establishments/*/arrivals filtered by current uid
    private void setupRecentlyVisitedListener() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        dbRef.child("establishments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recentlyVisitedAdapter.clear();

                for (DataSnapshot estSnap : snapshot.getChildren()) {
                    String estId = estSnap.getKey();
                    String estName = estSnap.child("companyName").getValue(String.class);
                    if (estName == null)
                        estName = estSnap.child("name").getValue(String.class);
                    if (estName == null) estName = "Unknown Establishment";

                    Map<String, String> processedToCounter = new HashMap<>();
                    DataSnapshot countersSnap = estSnap.child("counters");
                    for (DataSnapshot counterSnap : countersSnap.getChildren()) {
                        String counterId = counterSnap.getKey();
                        DataSnapshot processedSnap = counterSnap.child("processedArrivals");
                        for (DataSnapshot pa : processedSnap.getChildren()) {
                            String arrivalId = pa.getKey();
                            processedToCounter.put(arrivalId, counterId);
                        }
                    }

                    DataSnapshot arrivalsSnap = estSnap.child("arrivals");
                    for (DataSnapshot arrSnap : arrivalsSnap.getChildren()) {
                        String arrivalId = arrSnap.getKey();
                        String arrivalUid = arrSnap.child("uid").getValue(String.class);
                        if (arrivalUid == null || !arrivalUid.equals(uid)) continue;

                        String counterId = processedToCounter.get(arrivalId);
                        if (counterId == null) continue;

                        String counterName = estSnap.child("counters").child(counterId)
                                .child("name").getValue(String.class);
                        if (counterName == null) counterName = "Counter";

                        long timestamp = 0L;
                        Long tsVal = arrSnap.child("timestamp").getValue(Long.class);
                        if (tsVal != null) timestamp = tsVal;

                        VisitedItem visited = new VisitedItem();
                        visited.estId = estId;
                        visited.estName = estName;
                        visited.counterId = counterId;
                        visited.counterName = counterName;
                        visited.reason = "-"; // Reason resolution requires another async read; keep simple here
                        visited.timestamp = timestamp;

                        recentlyVisitedAdapter.addVisited(visited);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeActivity", "Failed to load recently visited", error.toException());
            }
        });
    }

    // Existing notification listener wiring for Firestore inbox
    private void setupNotificationListener() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        firestore.collection("patients").document(uid).collection("inbox")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("HomeActivity", "Failed to load notifications", e);
                            return;
                        }
                        notificationAdapter.clear();
                        notificationList.clear();
                        if (snapshots == null) return;
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Notification n = dc.getDocument().toObject(Notification.class);
                            if (n != null) {
                                notificationList.add(n);
                            }
                        }
                        notificationAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        String userId = mAuth.getCurrentUser().getUid();

        // Load username
        dbRef.child("patients").child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.exists() ? snapshot.getValue(String.class) : "User";
                        userName.setText(name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        userName.setText("User");
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificationQuery != null && notificationListener != null) {
            notificationQuery.removeEventListener(notificationListener);
        }
        if (arrivalIndexListener != null && mAuth.getCurrentUser() != null) {
            dbRef.child("arrivalIndex").child(mAuth.getCurrentUser().getUid())
                    .removeEventListener(arrivalIndexListener);
        }
        activeQueueAdapter.stopTimer();
    }
}
