package com.example.studentgrievanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private Toolbar toolbar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String userEmail;
    private TextView profileUsername, profileEmail, profilePhone, profilePassword;
    private ImageView editIcon, editIcon2;
    private boolean isPasswordVisible = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Progress Bar
        progressDialog = new ProgressDialog(this);

        // toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        profileUsername = findViewById(R.id.profile_username);
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        editIcon = findViewById(R.id.edit_icon);

        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPhoneDialog();
            }
        });

        profilePassword = findViewById(R.id.profile_password);
        editIcon2 = findViewById(R.id.edit_icon_2);

        editIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReauthenticateDialog();
            }
        });

        userEmail = auth.getCurrentUser().getEmail();
        profileEmail.setText(userEmail);
        loadUserData(userEmail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(userEmail);
    }

    private void loadUserData(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String username = document.getString("username");
                                String phone = document.getString("phone");

                                profileUsername.setText(username != null ? username : "Unknown");
                                profilePhone.setText(phone != null ? phone : "Unknown");
                            }
                        } else {
                            Log.d(TAG, "User data not found");
                        }
                    } else {
                        Log.d(TAG, "Failed to fetch user data");
                    }
                });
    }

    private void showReauthenticateDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.profile_dialog_reauthenticate, null);

        EditText currentPassword = dialogView.findViewById(R.id.profile_current_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Next", (dialog, id) -> {
                    String password = currentPassword.getText().toString();
                    reauthenticateUser(password);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void reauthenticateUser(String currentPassword) {
        progressDialog.setMessage("Authenticating...");
        if (currentUser == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        progressDialog.show();

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showEditPasswordDialog();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showAlertDialog("Authentication Failed", "Incorrect password. Please try again.");
                });
    }

    private void showEditPasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.profile_dialog_edit_password, null);

        EditText newPassword = dialogView.findViewById(R.id.profile_new_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Update", (dialog, id) -> {
                    String newPasswordStr = newPassword.getText().toString();
                    updatePassword(newPasswordStr);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updatePassword(String newPassword) {
        progressDialog.setMessage("Updating...");
        progressDialog.show();
        String email = "jialingbeh0038@gmail.com";

        auth.getCurrentUser().updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    profilePassword.setText(newPassword);
                    updatePasswordInFirestore(newPassword); // Update Firestore with the new password
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showAlertDialog("Error", "Failed to update password. " + e.getMessage());
                });
    }

    private void updatePasswordInFirestore(String newPassword) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String documentId = document.getId();
                                // Update the password field in Firestore
                                db.collection("users").document(documentId)
                                        .update("password", newPassword)
                                        .addOnSuccessListener(aVoid -> {
                                            progressDialog.dismiss();
                                            showAlertDialog("Success", "Password updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            showAlertDialog("Error", "Failed to update password in Firestore.");
                                        });
                            }
                        } else {
                            progressDialog.dismiss();
                            showAlertDialog("Error", "User document not found in Firestore.");
                        }
                    } else {
                        progressDialog.dismiss();
                        showAlertDialog("Error", "Failed to query Firestore for user document.");
                    }
                });
    }

    private void showEditPhoneDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.profile_dialog_edit_phone, null);

        EditText newPhone = dialogView.findViewById(R.id.profile_new_phone);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Update", (dialog, id) -> {
                    dialog.dismiss();
                    progressDialog.show();
                    String newPhoneNumber = newPhone.getText().toString();
                    updatePhoneNumber(userEmail, newPhoneNumber);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updatePhoneNumber(String email, String newPhoneNumber) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String documentId = document.getId();
                                db.collection("users").document(documentId)
                                        .update("phone", newPhoneNumber)
                                        .addOnSuccessListener(aVoid -> {
                                            profilePhone.setText(newPhoneNumber);
                                            progressDialog.dismiss();
                                            showAlertDialog("Success", "Phone number updated.");
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            showAlertDialog("Error", "Failed to update phone number.");
                                        });
                            }
                        }
                    }
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
}
