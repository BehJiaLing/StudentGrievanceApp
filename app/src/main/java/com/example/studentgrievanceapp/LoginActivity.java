package com.example.studentgrievanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private ImageView togglePasswordVisibility;
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Progress Bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        // Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.login_username);
        passwordEditText = findViewById(R.id.login_password);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);

        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.profile_ic_visibility_off);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.profile_ic_visibility);
            }
            // Move cursor to the end of the text
            passwordEditText.setSelection(passwordEditText.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                showAlertDialog("Error", "Please enter your email");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                showAlertDialog("Error", "Please enter your password");
                return;
            }

            loginUser(email, password);
        });

        findViewById(R.id.resetPasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String email, String password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserInFirestoreByEmail(email, password);
                        }
                    } else {
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            showAlertDialog("Authentication Failed", "No account found with this email.");
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            showAlertDialog("Authentication Failed", "Invalid account or password. Please try again.");
                        } catch (Exception e) {
                            showAlertDialog("Authentication Failed", "Authentication failed. Please try again." + e.getMessage());
                        }
                    }
                });
    }

    private void checkUserInFirestoreByEmail(String email, String password) {
        String lowerCaseEmail = email.toLowerCase();
        db.collection("users")
                .whereEqualTo("email", lowerCaseEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        progressDialog.dismiss();
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Fetch the user's document
                            String userId = task.getResult().getDocuments().get(0).getId();
                            String firestorePassword = task.getResult().getDocuments().get(0).getString("password"); // Assuming password is stored in Firestore

                            // Check if Firestore password matches with Auth password
                            if (!password.equals(firestorePassword)) {
                                // Update Firestore password to match the Auth password
                                updatePasswordInFirestore(userId, password);
                            } else {
                                showAlertDialog("Success", "Login Successfully!!", true);
                            }
                        }
                    } else {
                        progressDialog.dismiss();
                        showAlertDialog("Error", "This is not a user account.");
                    }
                });
    }

    private void updatePasswordInFirestore(String userId, String newPassword) {
        db.collection("users").document(userId)
                .update("password", newPassword) // Update the password in Firestore
                .addOnSuccessListener(aVoid -> {
                    showAlertDialog("Success", "Login Successfully!!", true);
                })
                .addOnFailureListener(e -> {
                    showAlertDialog("Error", "Failed to update new password via email in Firestore: " + e.getMessage());
                });
    }

    // normal message to user read
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // login message and bring user to homepage
    private void showAlertDialog(String title, String message, boolean isSuccessfulLogin) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (isSuccessfulLogin) {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Close LoginActivity
                    }
                })
                .show();
    }
}
