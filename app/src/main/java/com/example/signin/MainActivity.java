package com.example.signin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    private EditText emailField, passwordField;
    private Button signIn;
    private CheckBox rememberMe;

    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "loginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.signin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("patients");

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        signIn = findViewById(R.id.signInButton);
        rememberMe = findViewById(R.id.rememberMe);

        Button signup = findViewById(R.id.createAccountButton);
        TextView resetpass = findViewById(R.id.forgotPassword);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Restore checkbox state
        boolean isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        rememberMe.setChecked(isRemembered);

        // Auto-fill email if saved
        if (isRemembered) {
            emailField.setText(sharedPreferences.getString(KEY_EMAIL, ""));
        }

        signIn.setOnClickListener(v -> loginUser());
        signup.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Signup.class)));
        resetpass.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Resetpass.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);

        if (remember && mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this,
                                "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Save Remember Me state (ONLY when login succeeds)
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (rememberMe.isChecked()) {
                        editor.putBoolean(KEY_REMEMBER, true);
                        editor.putString(KEY_EMAIL, email);
                        editor.putString(KEY_PASSWORD, password);
                    } else {
                        editor.putBoolean(KEY_REMEMBER, false);
                        editor.remove(KEY_EMAIL);
                        editor.remove(KEY_PASSWORD);
                    }
                    editor.apply();

                    String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                    databaseRef.child(uid).get().addOnCompleteListener(task1 -> {

                        if (!task1.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Database check failed: " +
                                            Objects.requireNonNull(task1.getException()).getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        DataSnapshot userSnapshot = task1.getResult();

                        if (!userSnapshot.exists()) {
                            showUnregisteredDialog();
                            return;
                        }

                        // Now check patient info fields
                        boolean hasName = userSnapshot.child("name").exists();
                        boolean hasAge = userSnapshot.child("age").exists();
                        boolean hasAddress = userSnapshot.child("address").exists();
                        boolean hasPhone = userSnapshot.child("contactNumber").exists();

                        boolean hasAllInfo = hasName && hasAge && hasAddress && hasPhone;

                        if (hasAllInfo) {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, PatientInfo.class));
                        }

                        finish();
                    });
                });
    }

    private void showUnregisteredDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Unregistered Account")
                .setMessage("This account is not registered in our database. " +
                        "Registering will help you recover your account.")
                .setPositiveButton("Register", (dialog, which) -> {
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this,
                                                "Registration email sent.",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(MainActivity.this,
                                                "Failed to send verification email.",
                                                Toast.LENGTH_LONG).show();
                                    }

                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    finish();
                                });
                    }
                })
                .setNegativeButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
