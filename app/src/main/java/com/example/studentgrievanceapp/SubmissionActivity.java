package com.example.studentgrievanceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SubmissionActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private TextView grievanceTypeName;
    private EditText titleEditText;
    private EditText descriptionEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting...");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Grievance");

        grievanceTypeName = findViewById(R.id.grievance_type_name);
        titleEditText = findViewById(R.id.grievance_title);
        descriptionEditText = findViewById(R.id.grievance_description);

        // Get grievance type from the Intent and set it in TextView
        String grievanceType = getIntent().getStringExtra("grievance_type");
        grievanceTypeName.setText(grievanceType);

        // Set up submit button click listener
        Button submitButton = findViewById(R.id.grievance_submit_button);
        submitButton.setOnClickListener(v -> submitGrievance(grievanceType));
    }

    private void submitGrievance(String grievanceType) {

        progressDialog.show();

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Check if the title or description is empty
        if (title.isEmpty() || description.isEmpty()) {
            progressDialog.dismiss();
            showAlertDialog("Error", "Please enter both title and description.");
            return; // Exit the method if validation fails
        }

        String email = mAuth.getCurrentUser().getEmail();
        Timestamp dateTime = Timestamp.now();
        String status = "Pending";
        String urgencyLevel = "Low";


        Map<String, Object> grievanceData = new HashMap<>();
        grievanceData.put("category", grievanceType);
        grievanceData.put("title", title);
        grievanceData.put("description", description);
        grievanceData.put("email", email);
        grievanceData.put("dateTime", dateTime);
        grievanceData.put("status", status);
        grievanceData.put("urgencyLevel", urgencyLevel);

        db.collection("grievance")
                .add(grievanceData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    showAlertDialog("Success", "Grievance submitted successfully!",true);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showAlertDialog("Error", "Failed to submit grievance. Please try again.");
                });
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAlertDialog(String title, String message, boolean isSuccessfullySubmitted) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (isSuccessfullySubmitted) {
                        Intent intent = new Intent(SubmissionActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}

