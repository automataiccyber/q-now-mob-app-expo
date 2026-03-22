package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity displaying all user requests (queue + appointment).
 */
public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyRequestsAdapter adapter;

    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private ValueEventListener listenerQueue, listenerAppt;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        // --- Initialize UI ---
        recyclerView = findViewById(R.id.rvMyRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRequestsAdapter();
        recyclerView.setAdapter(adapter);

        // --- Initialize Firebase ---
        dbRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        back = findViewById(R.id.btnBack);

        // Back button → return to HomeActivity
        back.setOnClickListener(v ->
                startActivity(new Intent(MyRequestsActivity.this, HomeActivity.class))
        );

        startLiveListeners();
    }

    /**
     * Start Firebase listeners for live updates of queue and appointment requests.
     */
    private void startLiveListeners() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Queue requests listener
        listenerQueue = dbRef.child("patients").child(userId).child("queueRequests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loadCombinedRequests();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Appointment requests listener
        listenerAppt = dbRef.child("patients").child(userId).child("appointments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loadCombinedRequests();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * Loads all queue + appointment requests for the user.
     * Enriches them with establishment and counter names before updating UI.
     */
    private void loadCombinedRequests() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        List<RequestItem> allRequests = new ArrayList<>();

        // Load queue requests
        dbRef.child("patients").child(userId).child("queueRequests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                RequestItem item = child.getValue(RequestItem.class);
                                if (item != null) {
                                    // Skip rejected/cancelled - they should not appear in My Requests
                                    if ("rejected".equalsIgnoreCase(item.status)
                                            || "cancelled".equalsIgnoreCase(item.status)
                                            || "accepted".equalsIgnoreCase(item.status)) {
                                        continue;
                                    }
                                    item.requestKey = child.getKey();
                                    item.type = "Queue Request";
                                    allRequests.add(item);
                                }
                            }
                        }

                        // Load appointment requests next
                        dbRef.child("patients").child(userId).child("appointments")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snap2) {
                                        if (snap2.exists()) {
                                            for (DataSnapshot child : snap2.getChildren()) {
                                                RequestItem item = child.getValue(RequestItem.class);
                                                if (item != null && (
                                                        "requested".equalsIgnoreCase(item.status)
                                                                || "pending".equalsIgnoreCase(item.status))) {
                                                    item.type = "Appointment Request";
                                                    allRequests.add(item);
                                                }
                                            }
                                        }

                                        attachEstablishmentAndCounterNames(allRequests);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * Enrich requests with establishment and counter names before updating adapter.
     *
     * @param requests List of RequestItem
     */
    private void attachEstablishmentAndCounterNames(List<RequestItem> requests) {
        if (requests.isEmpty()) {
            adapter.setRequests(requests);
            return;
        }

        final int total = requests.size();
        final int[] completed = {0};

        for (RequestItem item : requests) {
            if (item.estId == null) {
                completed[0]++;
                if (completed[0] == total) adapter.setRequests(requests);
                continue;
            }

            dbRef.child("establishments").child(item.estId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot estSnap) {
                            String estName = estSnap.child("companyName").getValue(String.class);
                            if (estName == null)
                                estName = estSnap.child("name").getValue(String.class);
                            item.estName = (estName != null) ? estName : "Unknown Establishment";

                            // For queue requests, add counter name
                            if ("Queue Request".equals(item.type) && item.counterId != null) {
                                String counterName = estSnap.child("counters")
                                        .child(item.counterId).child("name").getValue(String.class);
                                item.counterName = (counterName != null) ? counterName : "-";
                            }

                            completed[0]++;
                            if (completed[0] == total) {
                                // Sort by newest first
                                requests.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                                adapter.setRequests(requests);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            completed[0]++;
                            if (completed[0] == total) adapter.setRequests(requests);
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            if (listenerQueue != null)
                dbRef.child("patients").child(userId).child("queueRequests")
                        .removeEventListener(listenerQueue);
            if (listenerAppt != null)
                dbRef.child("patients").child(userId).child("appointments")
                        .removeEventListener(listenerAppt);
        }
    }

    /**
     * Formats a timestamp to a human-readable string.
     *
     * @param timestamp Long timestamp in milliseconds
     * @return Formatted string or "-" if invalid
     */
    public static String formatTime(long timestamp) {
        if (timestamp <= 0) return "-";
        return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
    }
}
