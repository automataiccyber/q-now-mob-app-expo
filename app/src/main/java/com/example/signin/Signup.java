package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

/**
 * Signup Activity
 *
 * Handles:
 *  - Creating an Auth account in Firebase Authentication
 *  - Saving user info in Firebase Realtime Database
 *  - Validating user input
 *  - Redirecting to Login (MainActivity)
 */
public class Signup extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    // UI components
    private EditText emailField, passwordField, confirmPass, usernameField;
    private TextView AlreadyHaveAcc;
    private Button Signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable full screen edge-to-edge UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // Link UI elements
        Signup = findViewById(R.id.createAccountButton);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPass = findViewById(R.id.confirmpass);
        usernameField = findViewById(R.id.usernameField);
        AlreadyHaveAcc = findViewById(R.id.alreadyhaveaccount);

        // Navigate back to Login
        AlreadyHaveAcc.setOnClickListener(v -> {
            startActivity(new Intent(Signup.this, MainActivity.class));
        });

        // Start registration
        Signup.setOnClickListener(v -> registerUser());
    }

    /**
     * registerUser()
     *
     * Validates user input
     * Creates a Firebase Auth account
     * Saves user profile data in Realtime Database
     */
    private void registerUser() {

        // Read user input
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPass.getText().toString().trim();

        // ❗ FIXED: previously used confirmPass instead of usernameField
        String username = usernameField.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            return;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            confirmPass.setError("Passwords do not match");
            return;
        }

        // Validate username
        if (TextUtils.isEmpty(username)) {
            usernameField.setError("Username is required");
            return;
        }

        // Create account in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Signup.this, task -> {

                    if (task.isSuccessful()) {

                        // Get newly created user ID
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        // Create User object for database
                        User newUser = new User(username, email);

                        // Save it under "Users/{uid}"
                        databaseRef.child(userId).setValue(newUser)
                                .addOnCompleteListener(dbTask -> {

                                    if (dbTask.isSuccessful()) {

                                        Toast.makeText(Signup.this,
                                                "Account created & data saved!",
                                                Toast.LENGTH_SHORT).show();

                                        Toast.makeText(Signup.this,
                                                "Please check your email to verify your account.",
                                                Toast.LENGTH_LONG).show();

                                        // Redirect back to login
                                        startActivity(new Intent(Signup.this, MainActivity.class));
                                    } else {
                                        Toast.makeText(Signup.this,
                                                "Database Error: " + dbTask.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                    } else {
                        // Auth creation failed
                        Toast.makeText(Signup.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * User class = stored structure in Realtime Database
     */
    public static class User {
        public String username;
        public String email;

        public User() {
            // Required empty constructor for Firebase
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}
