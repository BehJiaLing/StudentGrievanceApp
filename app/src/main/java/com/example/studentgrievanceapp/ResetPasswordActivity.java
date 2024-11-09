package com.example.studentgrievanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText email;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Progress Bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking account...");

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        email = findViewById(R.id.reset_email);

        findViewById(R.id.loginPageButton).setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.resetButton).setOnClickListener(v -> {
            String emailAddress = email.getText().toString().trim(); // Get email from EditText
            if (TextUtils.isEmpty(emailAddress)) {
                showAlertDialog("Error", "Please enter your email address.");
                return;
            }
            checkIfEmailExists(emailAddress);
        });
    }

    private void checkIfEmailExists(String emailAddress) {
        progressDialog.show();
        db.collection("users")
                .whereEqualTo("email", emailAddress)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        progressDialog.dismiss();
                        sendPasswordResetEmail(emailAddress);
                    } else {
                        progressDialog.dismiss();
                        showAlertDialog("Error", "No account found with this email address.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showAlertDialog("Error", "Error checking account: " + e.getMessage());
                });
    }

    private void sendPasswordResetEmail(String emailAddress) {
        progressDialog.setMessage("Sending...");
        progressDialog.show();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showAlertDialog("Success", "Password reset email has been sent.", true);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showAlertDialog("Error", "Error sending reset email: " + e.getMessage());
                });
    }

    // Normal message to user
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAlertDialog(String title, String message, boolean isSuccessfulReset) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (isSuccessfulReset) {
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Close ResetPasswordActivity
                    }
                })
                .show();
    }
}
