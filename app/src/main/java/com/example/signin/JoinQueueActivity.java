package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * JoinQueueActivity
 * -------------------
 * Displays ALL counters (from ALL establishments) that are currently accepting customers.
 *
 * User can:
 *  - See a list of establishments + their counters
 *  - Filter them using a search bar
 *  - Select one to join the queue
 *
 * The selection then opens JoinConfirmActivity.
 */
public class JoinQueueActivity extends AppCompatActivity {

    private RecyclerView rvCounters;
    private CounterAdapter adapter;

    // counterList = displayed list
    // fullList = full loaded list before search filtering
    private final List<CounterItem> counterList = new ArrayList<>();
    private final List<CounterItem> fullList = new ArrayList<>();

    private DatabaseReference dbRef;
    private EditText searchBar;
    private Button back;

    // Priority lane requirement derived from patient info
    private boolean requirePriorityLane = false;
    private DatabaseReference patientRef;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_queue);

        // UI references
        rvCounters = findViewById(R.id.rvCounters);
        searchBar = findViewById(R.id.searchBar);
        back = findViewById(R.id.btnBack);

        // RecyclerView setup
        rvCounters.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CounterAdapter(counterList, this::onCounterClick);
        rvCounters.setAdapter(adapter);

        // Firebase reference to all establishments
        dbRef = FirebaseDatabase.getInstance().getReference("establishments");
        currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUid != null) {
            patientRef = FirebaseDatabase.getInstance().getReference("patients").child(currentUid);
        }

        // Load patient priority flags first, then counters
        loadPatientPriorityAndCounters();

        // Back button → return to HomeActivity
        back.setOnClickListener(v ->
                startActivity(new Intent(JoinQueueActivity.this, HomeActivity.class))
        );

        // Search listener for filtering list
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    /** Loads patient flags (pwd/senior/severecon) to decide if priority lane is required */
    private void loadPatientPriorityAndCounters() {
        if (patientRef == null) {
            loadCounters();
            return;
        }
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean pwd = snapshot.child("pwd").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("pwd").getValue(Boolean.class));
                boolean senior = snapshot.child("senior").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("senior").getValue(Boolean.class));
                boolean severe = snapshot.child("severecon").getValue(Boolean.class) != null
                        && Boolean.TRUE.equals(snapshot.child("severecon").getValue(Boolean.class));
                requirePriorityLane = pwd || senior || severe;
                loadCounters();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // If failed to read, default to not requiring priority
                requirePriorityLane = false;
                loadCounters();
            }
        });
    }

    /**
     * Loads all establishments → their counters → filters only those accepting customers.
     *
     * Firebase structure:
     * establishments
     *    └── estId
     *         ├── companyName
     *         └── counters
     *              └── counterId
     *                   ├── name
     *                   ├── accepting (boolean)
     */
    private void loadCounters() {
        dbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                counterList.clear();
                fullList.clear();

                for (DataSnapshot estSnap : snapshot.getChildren()) {

                    // Establishment ID (key)
                    String estId = estSnap.getKey();

                    // Establishment name
                    String estName = estSnap.child("companyName").getValue(String.class);

                    // Loop all counters in this establishment
                    for (DataSnapshot counterSnap : estSnap.child("counters").getChildren()) {

                        Boolean accepting = counterSnap.child("accepting").getValue(Boolean.class);

                        // Only include counters that are accepting customers
                        if (accepting != null && accepting) {
                            // If user requires priority lane, show only counters marked as priorityLane
                            Boolean priorityLane = counterSnap.child("priorityLane").getValue(Boolean.class);
                            if (requirePriorityLane && !(priorityLane != null && priorityLane)) {
                                continue;
                            }

                            String counterId = counterSnap.getKey();
                            String counterName = counterSnap.child("name").getValue(String.class);
                            String avgQueueTime = counterSnap.child("avgQueueTime").getValue(String.class);

                            // Create the model
                            CounterItem item = new CounterItem(estId, counterId, estName, counterName, avgQueueTime);

                            // Add to lists
                            counterList.add(item);
                            fullList.add(item);
                        }
                    }
                }

                // Update UI
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(JoinQueueActivity.this,
                        "Failed to load counters: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filters the displayed list by matching user text with:
     *  - establishment name
     *  - counter name
     *
     * Case insensitive.
     */
    private void filter(String text) {
        List<CounterItem> filteredList = new ArrayList<>();
        String lower = text.toLowerCase();

        for (CounterItem item : fullList) {

            boolean matchesEst = item.getEstName() != null &&
                    item.getEstName().toLowerCase().contains(lower);

            boolean matchesCounter = item.getCounterName() != null &&
                    item.getCounterName().toLowerCase().contains(lower);

            if (matchesEst || matchesCounter) {
                filteredList.add(item);
            }
        }

        counterList.clear();
        counterList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Called when the user clicks a counter.
     * Opens JoinConfirmActivity with all required details.
     */
    private void onCounterClick(CounterItem item) {
        Intent intent = new Intent(this, JoinConfrimActivity.class);

        intent.putExtra("estId", item.getEstId());
        intent.putExtra("counterId", item.getCounterId());
        intent.putExtra("displayName",
                item.getEstName() + " - " + item.getCounterName());

        startActivity(intent);
    }
}
