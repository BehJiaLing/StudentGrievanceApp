package com.example.studentgrievanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatusInProgressActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private View pendingIndicator, inProgressIndicator, resolvedIndicator, closedIndicator;
    private View line1, line2, line3, line4, line5;
    private Button statusUpdateButton;
    private String grievanceRefreshStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Grievance Details");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Retrieve data from Intent
        Intent intent = getIntent();
        String documentId = intent.getStringExtra("document_id");
        Log.e("StatusActivity", "Document ID is: " + documentId);
        String grievanceTitle = intent.getStringExtra("grievance_title");
        String grievanceStatus = intent.getStringExtra("grievance_status");
        String grievanceCategory = intent.getStringExtra("grievance_category");
        String grievanceDescription = intent.getStringExtra("grievance_description");
        String grievanceDateTime = intent.getStringExtra("grievance_dateTime");

        grievanceRefreshStatus = grievanceStatus;

        // Use this data to display details
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(grievanceTitle);
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        categoryTextView.setText(grievanceCategory);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        descriptionTextView.setText(grievanceDescription);
        TextView dateTimeTextView = findViewById(R.id.dateTimeTextView);
        dateTimeTextView.setText(grievanceDateTime);

        pendingIndicator = findViewById(R.id.pendingIndicator);
        inProgressIndicator = findViewById(R.id.inProgressIndicator);
        resolvedIndicator = findViewById(R.id.resolvedIndicator);
        closedIndicator = findViewById(R.id.closedIndicator);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
        line4 = findViewById(R.id.line4);
        line5 = findViewById(R.id.line5);

        setStatusIndicators(grievanceStatus);

        statusUpdateButton = findViewById(R.id.status_update_button);
        if ("Resolved".equals(grievanceStatus)) {
            statusUpdateButton.setVisibility(View.VISIBLE);
            // statusUpdateButton.setEnabled(true); // Enable button only if status is "Resolved"
            // statusUpdateButton.setBackgroundColor(getResources().getColor(R.color.enabled_button_color));
            statusUpdateButton.setOnClickListener(view -> updateGrievanceStatusInDb(documentId));
        } else {
            statusUpdateButton.setVisibility(View.GONE);
            // statusUpdateButton.setEnabled(false);
            // statusUpdateButton.setBackgroundColor(getResources().getColor(R.color.disabled_button_color));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusIndicators(grievanceRefreshStatus);
    }

    private void setStatusIndicators(String status) {
        // Reset all indicators and lines to default color
        resetIndicators();

        // Highlight the relevant indicator and connecting lines based on status
        switch (status.toLowerCase()) {
            case "pending":
                line1.setBackgroundColor(getResources().getColor(R.color.grey));
                pendingIndicator.setBackgroundColor(getResources().getColor(R.color.pending_color));
                break;
            case "in progress":
                line1.setBackgroundColor(getResources().getColor(R.color.grey));
                pendingIndicator.setBackgroundColor(getResources().getColor(R.color.pending_color));
                line2.setBackgroundColor(getResources().getColor(R.color.grey));
                inProgressIndicator.setBackgroundColor(getResources().getColor(R.color.in_progress_color));
                break;
            case "resolved":
                line1.setBackgroundColor(getResources().getColor(R.color.grey));
                pendingIndicator.setBackgroundColor(getResources().getColor(R.color.pending_color));
                line2.setBackgroundColor(getResources().getColor(R.color.grey));
                inProgressIndicator.setBackgroundColor(getResources().getColor(R.color.in_progress_color));
                line3.setBackgroundColor(getResources().getColor(R.color.grey));
                resolvedIndicator.setBackgroundColor(getResources().getColor(R.color.resolved_color));
                break;
            case "closed":
                line1.setBackgroundColor(getResources().getColor(R.color.grey));
                pendingIndicator.setBackgroundColor(getResources().getColor(R.color.pending_color));
                line2.setBackgroundColor(getResources().getColor(R.color.grey));
                inProgressIndicator.setBackgroundColor(getResources().getColor(R.color.in_progress_color));
                line3.setBackgroundColor(getResources().getColor(R.color.grey));
                resolvedIndicator.setBackgroundColor(getResources().getColor(R.color.resolved_color));
                line4.setBackgroundColor(getResources().getColor(R.color.grey));
                closedIndicator.setBackgroundColor(getResources().getColor(R.color.closed_color));
                line5.setBackgroundColor(getResources().getColor(R.color.grey));
                break;
            default:
                break;
        }
    }

    private void resetIndicators() {
        // Reset all indicators to a default gray color
        int defaultColor = getResources().getColor(android.R.color.darker_gray);
        pendingIndicator.setBackgroundColor(defaultColor);
        inProgressIndicator.setBackgroundColor(defaultColor);
        resolvedIndicator.setBackgroundColor(defaultColor);
        closedIndicator.setBackgroundColor(defaultColor);
        line1.setBackgroundColor(defaultColor);
        line2.setBackgroundColor(defaultColor);
        line3.setBackgroundColor(defaultColor);
    }

    // Sample method to simulate database update (adjust based on your database setup)
    private void updateGrievanceStatusInDb(String documentId) {
        Log.e("StatusActivity", "Update ID is: " + documentId);
        progressDialog.show();

        if (documentId != null) {
            db.collection("grievance").document(documentId)
                    .update("status", "Closed")
                    .addOnSuccessListener(aVoid -> {
                        db.collection("grievance").document(documentId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String newStatus = documentSnapshot.getString("status");
                                        setStatusIndicators(newStatus);
                                        statusUpdateButton.setVisibility(View.GONE);
                                        showAlertDialog("Success", "This grievance is done, thank you for your support!!");
                                    } else {
                                        showAlertDialog("Error", "The latest status update unsuccessfully.");
                                    }
                                    progressDialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    showAlertDialog("Error", "Failed to retrieve updated status: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        showAlertDialog("Error", "Failed to update grievance status: " + e.getMessage());
                    });
        } else {
            progressDialog.dismiss();
            showAlertDialog("Error", "The ID is null ");
        }
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}