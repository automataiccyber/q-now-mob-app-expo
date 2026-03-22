package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Resetpass extends AppCompatActivity {

    // UI Components
    private Button sendEmail;       // Button to send reset email
    private EditText emailField;    // Input field for user email
    private TextView backtoSignin;  // "Back" text that returns user to login page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables Android edge-to-edge layout (fullscreen behavior)
        EdgeToEdge.enable(this);

        // Loads the screen layout from resetpass.xml
        setContentView(R.layout.resetpass);

        // Makes sure UI elements avoid system bars (camera notch, gesture bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect variables to XML UI elements
        sendEmail = findViewById(R.id.sendEmail);
        emailField = findViewById(R.id.emailField);
        backtoSignin = findViewById(R.id.backtoSignin);

        // Go back to login activity
        backtoSignin.setOnClickListener(view -> {
            Intent intent = new Intent(Resetpass.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close current screen so user cannot come back with back button
        });

        // Handle "Send Email" button click
        sendEmail.setOnClickListener(view -> {

            // Get entered email
            String email = emailField.getText().toString().trim();

            // Validate input
            if (email.isEmpty()) {
                Toast.makeText(Resetpass.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send password reset email via Firebase
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            // Successfully sent
                            Toast.makeText(Resetpass.this,
                                    "Password reset email sent!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Firebase returned an error
                            Toast.makeText(
                                    Resetpass.this,
                                    "Error: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }
}
