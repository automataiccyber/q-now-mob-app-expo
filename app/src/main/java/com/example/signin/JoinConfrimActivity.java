package com.example.signin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for confirming and joining a queue at a specific counter of an establishment.
 */
public class JoinConfrimActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvQueueName;
    private EditText etReason;
    private Button btnJoinQueue, btnCancel;

    // Firebase
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private ArrivalIndexRepository arrivalIndexRepository;

    // Intent extras
    private String estId, counterId, displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_confirm);

        // Retrieve extras passed from JoinQueueActivity
        estId = getIntent().getStringExtra("estId");
        counterId = getIntent().getStringExtra("counterId");
        displayName = getIntent().getStringExtra("displayName");

        // Bind UI elements
        tvQueueName = findViewById(R.id.tvQueueName);
        etReason = findViewById(R.id.etReason);
        btnJoinQueue = findViewById(R.id.btnJoinQueue);
        btnCancel = findViewById(R.id.btnCancel);

        // Show the counter/establishment name at the top
        tvQueueName.setText(displayName);

        // Initialize Firebase
        dbRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        arrivalIndexRepository = new ArrivalIndexRepository();

        // Button listeners
        btnJoinQueue.setOnClickListener(v -> joinQueue());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Handles the logic for joining the queue.
     */
    private void joinQueue() {
        // Check if user is logged in
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate reason input
        String reason = etReason.getText().toString().trim();
        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch patient info from Firebase
        dbRef.child("patients").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(JoinConfrimActivity.this,
                            "No patient info found. Please complete your info first.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get patient's essential information
                String patientName = snapshot.child("name").getValue(String.class);
                String patientEmail = snapshot.child("email").getValue(String.class);

                // Fetch establishment and counter info
                dbRef.child("establishments").child(estId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot estSnap) {
                                // Establishment name fallback logic
                                String estName = estSnap.child("companyName").getValue(String.class);
                                if (estName == null) estName = estSnap.child("name").getValue(String.class);
                                if (estName == null) estName = "Unknown Establishment";

                                // Counter name fallback logic
                                String counterName = estSnap.child("counters").child(counterId)
                                        .child("name").getValue(String.class);
                                if (counterName == null) counterName = "Counter";

                                // Build full request data for establishment
                                Map<String, Object> fullData = new HashMap<>();
                                fullData.put("uid", userId);
                                fullData.put("patientName", patientName != null ? patientName : "Unknown");
                                fullData.put("patientEmail", patientEmail != null ? patientEmail : "Unknown");
                                fullData.put("reason", reason);
                                // Queue request starts as PENDING until counter accepts
                                fullData.put("status", "pending");
                                fullData.put("timestamp", ServerValue.TIMESTAMP);
                                fullData.put("joinTime", ServerValue.TIMESTAMP);

                                // Generate unique request key
                                DatabaseReference requestRef = dbRef.child("establishments")
                                        .child(estId).child("counters").child(counterId)
                                        .child("requests").push();
                                String requestKey = requestRef.getKey();

                                if (requestKey == null) {
                                    Toast.makeText(JoinConfrimActivity.this,
                                            "Error generating request key", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Prepare atomic updates for patient and establishment sides
                                Map<String, Object> updates = new HashMap<>();

                                // 1️⃣ Establishment-side record (counter's requests node)
                                updates.put("/establishments/" + estId + "/counters/" + counterId + "/requests/" + requestKey, fullData);

                                // 2️⃣ Patient-side queueRequests record (mirror of the request)
                                Map<String, Object> patientRequest = new HashMap<>();
                                patientRequest.put("estId", estId);
                                patientRequest.put("estName", estName);
                                patientRequest.put("counterId", counterId);
                                patientRequest.put("counterName", counterName);
                                patientRequest.put("reason", reason);
                                // Mirror status as PENDING here as well
                                patientRequest.put("status", "pending");
                                patientRequest.put("timestamp", ServerValue.TIMESTAMP);
                                patientRequest.put("joinTime", ServerValue.TIMESTAMP);

                                updates.put("/patients/" + userId + "/queueRequests/" + requestKey, patientRequest);

                                // Execute atomic update
                                dbRef.updateChildren(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            // IMPORTANT: arrivalIndex and awaitingArrival are NOT created at this stage.
                                            // They will be created by the counter/web side when the request is accepted.

                                            Toast.makeText(JoinConfrimActivity.this,
                                                    "Request sent! Please wait for confirmation.",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(JoinConfrimActivity.this,
                                                    "Failed to send request", Toast.LENGTH_SHORT).show();
                                        });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(JoinConfrimActivity.this,
                                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinConfrimActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
