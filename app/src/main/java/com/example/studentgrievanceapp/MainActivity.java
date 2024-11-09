package com.example.studentgrievanceapp;

import static android.content.ContentValues.TAG;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.studentgrievanceapp.model.NotificationAllModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private static final String SENT_NOTIFICATIONS_PREFS = "sent_notifications_prefs";
    private static final String SENT_NOTIFICATIONS_KEY = "sent_notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Progress Bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking Logging Account...");
        progressDialog.show();

        // Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if the user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            Log.d(TAG, "User already logged in with email: " + userEmail);

            if (userEmail != null) {
                // Check if user exists in Firestore
                checkUserInFirestoreByEmail(userEmail);
            }
        } else {
            progressDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
//            }
//        }
//
//        listenForNewNotifications();
    }

    private void checkUserInFirestoreByEmail(String email) {
        String lowerCaseEmail = email.toLowerCase();

        db.collection("users")
                .whereEqualTo("email", lowerCaseEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // User exists in Firestore, proceed to HomeActivity
                            progressDialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            // No user found, redirect to LoginActivity
                            progressDialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        finish(); // Close MainActivity
                    } else {
                        Log.e(TAG, "Firestore error: ", task.getException());
                        progressDialog.dismiss();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Grievance Notification";
//            String description = "The notification for reminder the status have updated.";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel("Grievance_Notification_ID", name, importance);
//            channel.setDescription(description);
//
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
//
//    private void sendNotification(String title, String message, String notificationId) {
//        // Check if notification has already been sent
//        SharedPreferences prefs = getSharedPreferences(SENT_NOTIFICATIONS_PREFS, MODE_PRIVATE);
//        Set<String> sentNotifications = prefs.getStringSet(SENT_NOTIFICATIONS_KEY, new HashSet<>());
//
//        if (sentNotifications.contains(notificationId)) {
//            return; // Notification already sent, no need to resend
//        }
//
//        Intent intent = new Intent(this, NotificationActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Grievance_Notification_ID")
//                .setSmallIcon(R.drawable.notification_all_ic_ring)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        notificationManager.notify(1, builder.build());
//
//        // Add notification ID to sent notifications set and save it
//        sentNotifications.add(notificationId);
//        prefs.edit().putStringSet(SENT_NOTIFICATIONS_KEY, sentNotifications).apply();
//    }
//
//    private void listenForNewNotifications() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        String currentUserEmail = user != null ? user.getEmail() : null;
//
//        if (currentUserEmail != null) {
//            db.collection("notification")
//                    .whereEqualTo("email", currentUserEmail)
//                    .addSnapshotListener((snapshots, e) -> {
//                        if (e != null) {
//                            Log.w(TAG, "Listen failed.", e);
//                            return;
//                        }
//
//                        if (snapshots != null && !snapshots.isEmpty()) {
//                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
//                                if (dc.getType() == DocumentChange.Type.ADDED) {
//                                    NotificationAllModel notification = dc.getDocument().toObject(NotificationAllModel.class);
//                                    String notificationId = dc.getDocument().getId(); // Get document ID for unique identification
//
//                                    // Only send if this notification ID hasn't been sent before
//                                    sendNotification("New Grievance Notification",
//                                            notification.getName() + " : " +
//                                                    notification.getGrievance_submission_title() +
//                                                    " has updated the status to '" +
//                                                    notification.getGrievance_submission_status() + "'",
//                                            notificationId);
//                                }
//                            }
//                        }
//                    });
//        }
//    }
}
