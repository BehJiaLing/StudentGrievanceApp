package com.example.studentgrievanceapp;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentgrievanceapp.adapter.GrievanceAllAdapter;
import com.example.studentgrievanceapp.adapter.NotificationAllAdapter;
import com.example.studentgrievanceapp.model.GrievanceAllModel;
import com.example.studentgrievanceapp.model.NotificationAllModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView notificationRecyclerView;
    private NotificationAllAdapter notificationAdapter;
    private List<NotificationAllModel> notificationList;
    private List<NotificationAllModel> originalNotificationList;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private Set<String> viewedNotifications;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Progress Bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("All Notification");

        db = FirebaseFirestore.getInstance();
        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load viewed notifications from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("notifications_prefs", MODE_PRIVATE);
        viewedNotifications = new HashSet<>(prefs.getStringSet("viewed_notifications", new HashSet<>()));

        notificationList = new ArrayList<>();
        originalNotificationList = new ArrayList<>();
        notificationAdapter = new NotificationAllAdapter(notificationList, this, viewedNotifications);
        notificationRecyclerView.setAdapter(notificationAdapter);

        loadNotification();
    }

    // Method to mark notification as viewed and save it to SharedPreferences
    public void saveViewedStatus(String documentId) {
        viewedNotifications.add(documentId);

        // Save updated viewedNotifications set to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("notifications_prefs", MODE_PRIVATE);
        prefs.edit().putStringSet("viewed_notifications", viewedNotifications).apply();
    }

    // Call loadNotification in onResume to refresh the list on every app resume
    @Override
    protected void onResume() {
        super.onResume();
        loadNotification();
    }

    private void loadNotification() {
        progressDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = user != null ? user.getEmail() : null;

        RecyclerView grievanceRecyclerView = findViewById(R.id.notificationRecyclerView);
        LinearLayout displayNothing = findViewById(R.id.display_nothing);

        if (currentUserEmail != null) {
            db.collection("notification")
                    .whereEqualTo("email", currentUserEmail)
                    .orderBy("updated_time", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            notificationList.clear();
                            originalNotificationList.clear();

                            for (DocumentSnapshot document : task.getResult()) {
                                NotificationAllModel notification = document.toObject(NotificationAllModel.class);
                                notification.setNotificationDocumentId(document.getId());
                                notification.setViewed(viewedNotifications.contains(document.getId()));
                                notificationList.add(notification);
                                originalNotificationList.add(notification);
                            }
                            notificationAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();

                            if (notificationList.isEmpty()) {
                                grievanceRecyclerView.setVisibility(View.GONE);
                                displayNothing.setVisibility(View.VISIBLE);
                            } else {
                                grievanceRecyclerView.setVisibility(View.VISIBLE);
                                displayNothing.setVisibility(View.GONE);
                            }
                        } else {
                            progressDialog.dismiss();
                            Exception e = task.getException();
                            showAlertDialog("Error", "Failed to load grievances: " + (e != null ? e.getMessage() : ""));
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_all_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Type");

        String[] types = getResources().getStringArray(R.array.grievance_filter_types);
        builder.setItems(types, (dialog, which) -> {
            String selectedType = types[which];
            filterByType(selectedType);
        });

        builder.create().show();
    }

    private void filterByType(String type) {
        List<NotificationAllModel> filteredList = new ArrayList<>();

        if (type.equals("All")) {
            filteredList.addAll(originalNotificationList);
        } else {
            for (NotificationAllModel notification : originalNotificationList) {
                if (notification.getGrievance_submission_category().equals(type)) {
                    filteredList.add(notification);
                }
            }
        }

        notificationAdapter.updateData(filteredList); // Update the adapter with filtered data
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
