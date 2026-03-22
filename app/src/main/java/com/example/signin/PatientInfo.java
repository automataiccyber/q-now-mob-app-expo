package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for viewing and updating patient personal information.
 */
public class PatientInfo extends AppCompatActivity {

    private EditText etName, etAge, etAddress, etContact, etEmail, etPin;
    private android.widget.ImageButton btnTogglePin;
    private boolean isPinVisible = false;
    private CheckBox cbPWD, cbElderly, cbSevere;
    private Button btnSave;

    private DatabaseReference patientsRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patientinfo);

        // Initialize UI elements
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etAddress = findViewById(R.id.etAddress);
        etContact = findViewById(R.id.etContact);
        etEmail = findViewById(R.id.etEmail);
        etPin = findViewById(R.id.etPin);
        btnTogglePin = findViewById(R.id.btnTogglePin);

        cbPWD = findViewById(R.id.cbPWD);
        cbElderly = findViewById(R.id.cbElderly);
        cbSevere = findViewById(R.id.cbSevere);

        btnSave = findViewById(R.id.btnSave);
        btnTogglePin.setOnClickListener(v -> togglePinVisibility());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        patientsRef = FirebaseDatabase.getInstance().getReference("patients").child(uid);

        loadPatientInfo();

        btnSave.setOnClickListener(v -> savePatientInfo());
    }

    private void togglePinVisibility() {
        if (isPinVisible) {
            // Hide PIN
            etPin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            btnTogglePin.setImageResource(R.drawable.ic_visibility_off);
            isPinVisible = false;
        } else {
            // Show PIN
            etPin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL);
            btnTogglePin.setImageResource(R.drawable.ic_visibility);
            isPinVisible = true;
        }
        // Move cursor to end
        etPin.setSelection(etPin.getText().length());
    }

    /**
     * Loads patient info from Firebase and populates the UI fields.
     */
    private void loadPatientInfo() {
        patientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                etName.setText(snapshot.child("name").getValue(String.class));
                etAge.setText(snapshot.child("age").getValue(String.class));
                etAddress.setText(snapshot.child("address").getValue(String.class));
                etContact.setText(snapshot.child("contactNumber").getValue(String.class));
                etEmail.setText(snapshot.child("email").getValue(String.class));
                
                // Load PIN if it exists
                if (snapshot.hasChild("pin")) {
                    etPin.setText(snapshot.child("pin").getValue(String.class));
                }

                Boolean pwd = snapshot.child("pwd").getValue(Boolean.class);
                Boolean senior = snapshot.child("senior").getValue(Boolean.class);
                Boolean severe = snapshot.child("severecon").getValue(Boolean.class);

                cbPWD.setChecked(pwd != null && pwd);
                cbElderly.setChecked(senior != null && senior);
                cbSevere.setChecked(severe != null && severe);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PatientInfo.this,
                        "Failed to load info: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves patient info back to Firebase.
     */
    private void savePatientInfo() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        boolean isPwd = cbPWD.isChecked();
        boolean isElderly = cbElderly.isChecked();
        boolean hasSevereCondition = cbSevere.isChecked();

        if (name.isEmpty() || age.isEmpty() || address.isEmpty() ||
                contact.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pin.length() != 4 || !pin.matches("\\d{4}")) {
            Toast.makeText(this, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("age", age);
        userMap.put("address", address);
        userMap.put("contactNumber", contact);
        userMap.put("email", email);
        userMap.put("pin", pin);
        userMap.put("pwd", isPwd);
        userMap.put("senior", isElderly);
        userMap.put("severecon", hasSevereCondition);

        patientsRef.updateChildren(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Patient info updated!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PatientInfo.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update info", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PatientInfo.this, HomeActivity.class));
                        finish();
                    }
                });
    }
}
