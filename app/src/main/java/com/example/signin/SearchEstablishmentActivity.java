package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that allows users to search for establishments in real-time
 * and navigate to book an appointment.
 */
public class SearchEstablishmentActivity extends AppCompatActivity {

    // UI Elements
    private EditText etSearch;
    private ListView lvResults;
    private Button back;

    // Adapter and data
    private EstablishmentAdapter adapter;
    private final List<EstablishmentItem> allEstablishments = new ArrayList<>();

    // Firebase reference
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_establishment);

        // Bind UI elements
        etSearch = findViewById(R.id.etSearch);
        back = findViewById(R.id.btnBack);
        lvResults = findViewById(R.id.lvResults);

        // Initialize Firebase reference
        dbRef = FirebaseDatabase.getInstance().getReference("establishments");

        // Initialize adapter with empty list
        adapter = new EstablishmentAdapter(this, new ArrayList<>());
        lvResults.setAdapter(adapter);

        // Load all establishments from Firebase
        loadEstablishments();

        // Back button → return to HomeActivity
        back.setOnClickListener(v ->
                startActivity(new Intent(SearchEstablishmentActivity.this, HomeActivity.class))
        );


        // Search bar: filter list dynamically
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Click listener: open BookAppointmentActivity for selected establishment
        lvResults.setOnItemClickListener((parent, view, position, id) -> {
            EstablishmentItem est = adapter.getItem(position);
            if (est != null) {
                Intent intent = new Intent(SearchEstablishmentActivity.this, BookAppointmentActivity.class);
                intent.putExtra("estId", est.getId());
                intent.putExtra("estName", est.getName());
                startActivity(intent);
            }
        });
    }

    /**
     * Loads all establishments from Firebase and populates the adapter.
     */
    private void loadEstablishments() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEstablishments.clear();

                for (DataSnapshot estSnap : snapshot.getChildren()) {
                    String id = estSnap.getKey();
                    String name = estSnap.child("companyName").getValue(String.class);
                    String address = estSnap.child("address").getValue(String.class);
                    String contactPerson = estSnap.child("contactPerson").getValue(String.class);

                    // Optional fields with defaults if missing
                    String hours = estSnap.child("hours").exists() ? estSnap.child("hours").getValue(String.class) : "N/A";
                    String phone = estSnap.child("phone").exists() ? estSnap.child("phone").getValue(String.class) : "N/A";
                    String email = estSnap.child("email").exists() ? estSnap.child("email").getValue(String.class) : "N/A";

                    // Departments list
                    List<String> departments = new ArrayList<>();
                    if (estSnap.hasChild("departments")) {
                        for (DataSnapshot depSnap : estSnap.child("departments").getChildren()) {
                            String dep = depSnap.getValue(String.class);
                            if (dep != null) departments.add(dep);
                        }
                    }

                    // Services list
                    List<String> services = new ArrayList<>();
                    if (estSnap.hasChild("services")) {
                        for (DataSnapshot servSnap : estSnap.child("services").getChildren()) {
                            String serv = servSnap.getValue(String.class);
                            if (serv != null) services.add(serv);
                        }
                    }

                    // Only add establishment if it has a name
                    if (name != null && !name.isEmpty()) {
                        allEstablishments.add(new EstablishmentItem(
                                id, name, hours, phone, email, contactPerson,
                                departments, services
                        ));
                    }
                }

                // Update adapter with full list
                adapter.updateList(allEstablishments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle error
            }
        });
    }

    /**
     * Filters the establishment list by search query (case-insensitive).
     *
     * @param query The search string.
     */
    private void filterList(String query) {
        List<EstablishmentItem> filtered = new ArrayList<>();
        for (EstablishmentItem est : allEstablishments) {
            if (est.getName() != null && est.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(est);
            }
        }
        adapter.updateList(filtered);
    }
}
