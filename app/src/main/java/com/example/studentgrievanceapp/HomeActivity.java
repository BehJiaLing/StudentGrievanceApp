package com.example.studentgrievanceapp;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.studentgrievanceapp.model.NotificationAllModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashSet;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private Toolbar toolbar;
    private View redDot;
    private static final String SENT_NOTIFICATIONS_PREFS = "sent_notifications_prefs";
    private static final String SENT_NOTIFICATIONS_KEY = "sent_notifications_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        // DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // ActionBarDrawerToggle to open/close drawer on button click
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Get the current user's email
        String email = mAuth.getCurrentUser().getEmail();
        TextView userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.nav_user_name);
        TextView userEmailTextView = navigationView.getHeaderView(0).findViewById(R.id.nav_user_email);

        // Query Firestore for the user document that has the matching email
        firestore.collection("users")
                .whereEqualTo("email", email) // Change this to match your field name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            // Get the first document (assuming unique email per user)
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String username = document.getString("username");
                            userNameTextView.setText(username != null ? username : "Unknown");
                            userEmailTextView.setText(email != null ? email : "Unknown");
                            Log.d(TAG, "Signed-in User's Email: " + email);
                        } else {
                            Log.d(TAG, "No user data found for the email: " + email);
                        }
                    } else {
                        Log.e(TAG, "Error getting user data", task.getException());
                    }
                });

        fetchGrievanceTotalCount(email);
        setGrievanceSubmission();

        findViewById(R.id.grievance_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, GrievanceActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.icon_pending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, GrievancePendingActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.icon_inProgress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, GrievanceInProgressActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.icon_confirmation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, GrievanceResolvedActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.icon_completed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, GrievanceClosedActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_profile) {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.nav_logout) {
            mAuth.signOut();
            showAlertDialog("Success", "Logged Out Successfully.", true);
            return true;
        }
        drawerLayout.closeDrawers();
        return true;
    }

    //    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.toolbar_notification) {
//            View actionView = item.getActionView();
//            redDot = actionView.findViewById(R.id.red_dot);
//
//            actionView.setOnClickListener(v -> {
//                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
//                startActivity(intent);
//                markNotificationsAsViewed();
//            });
//            updateRedDotVisibility();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public interface NotificationCallback {
        void onResult(boolean hasUnread);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        // Get the menu item by ID
        MenuItem notificationItem = menu.findItem(R.id.toolbar_notification);

        hasUnreadNotifications(hasUnread -> {
            if (hasUnread) {
                notificationItem.setIcon(R.drawable.menu_active_notification); // Active icon
            } else {
                notificationItem.setIcon(R.drawable.menu_notification); // Inactive icon
            }
        });

        notificationItem.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
            return true;
        });

        return true;
    }

    private void hasUnreadNotifications(NotificationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notification")
                .whereEqualTo("email", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasUnviewed = false;
                        SharedPreferences prefs = getSharedPreferences("notifications_prefs", MODE_PRIVATE);
                        Set<String> viewedNotifications = prefs.getStringSet("viewed_notifications", new HashSet<>());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!viewedNotifications.contains(document.getId())) {
                                hasUnviewed = true;
                                break;
                            }
                        }

                        // Use the callback to return the result
                        callback.onResult(hasUnviewed);
                    } else {
                        // In case of error, assume there are no unread notifications
                        callback.onResult(false);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUnreadNotifications();
        listenForNewNotifications();
    }

    private void refreshUnreadNotifications() {
        invalidateOptionsMenu(); // Forces the menu to be recreated
        hasUnreadNotifications(hasUnread -> {
            // Update the notification icon based on unread status
            MenuItem notificationItem = toolbar.getMenu().findItem(R.id.toolbar_notification);
            if (notificationItem != null) {
                if (hasUnread) {
                    notificationItem.setIcon(R.drawable.menu_active_notification); // Active icon
                } else {
                    notificationItem.setIcon(R.drawable.menu_notification); // Inactive icon
                }
            }
        });
    }

    private void sendNotification(String title, String message, String notificationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }

        SharedPreferences prefs = getSharedPreferences(SENT_NOTIFICATIONS_PREFS, MODE_PRIVATE);

        if (prefs.contains(notificationId)) {
            return;
        }

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "Grievance_Notification_ID",
                    "Grievance Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Grievance_Notification_ID")
                .setSmallIcon(R.drawable.notification_all_ic_ring)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId.hashCode(), builder.build());

        prefs.edit().putBoolean(notificationId, true).apply();
    }

    // Method to listen for new notifications in real-time
    private void listenForNewNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        // Listen to changes in the "notification" collection for the current user
        db.collection("notification")
                .whereEqualTo("email", user.getEmail())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed: ", e);
                        return;
                    }

                    if (snapshots != null) {
                        SharedPreferences prefs = getSharedPreferences(SENT_NOTIFICATIONS_PREFS, MODE_PRIVATE);
                        Set<String> sentNotifications = prefs.getStringSet(SENT_NOTIFICATIONS_KEY, new HashSet<>());

                        for (DocumentChange documentChange : snapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                QueryDocumentSnapshot document = documentChange.getDocument();
                                String notificationId = document.getId();
                                String title = document.getString("grievance_submission_title");
                                String name = document.getString("name");
                                String status = document.getString("grievance_submission_status");

                                // Check if this notification has already been sent
                                if (!sentNotifications.contains(notificationId)) {
                                    sendNotification("New Grievance Notification",
                                            name + " : " +
                                                    title +
                                                    " has updated the status to '" +
                                                    status + "'",
                                            notificationId);
                                }
                            }
                        }
                    }
                });
    }

    private void fetchGrievanceTotalCount(String userEmail) {
        // Reference to the TextView
        TextView grievanceTotalTextView = findViewById(R.id.grievance_total);

        // Fetch documents from "grievance" collection where userEmail matches
        firestore.collection("grievance")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Count the grievances and update the TextView
                        int grievanceCount = task.getResult().size();
                        grievanceTotalTextView.setText("(" + grievanceCount + ")");
                    } else {
                        // Handle the error, e.g., show a message or log it
                        grievanceTotalTextView.setText("0");
                    }
                });
    }

    private void setGrievanceSubmission() {
        findViewById(R.id.icon_food).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "Food");
            startActivity(intent);
        });

        findViewById(R.id.icon_facilities).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "Facility");
            startActivity(intent);
        });

        // Add more grievance buttons as needed
        findViewById(R.id.icon_academic).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "Academic");
            startActivity(intent);
        });

        findViewById(R.id.icon_administrative).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "Administrative");
            startActivity(intent);
        });

        findViewById(R.id.icon_harassment).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "Harassment");
            startActivity(intent);
        });

        findViewById(R.id.icon_financial).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "Financial");
            startActivity(intent);
        });

        findViewById(R.id.icon_it).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "IT");
            startActivity(intent);
        });

        findViewById(R.id.icon_other).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SubmissionActivity.class);
            intent.putExtra("grievance_type", "Others");
            startActivity(intent);
        });
    }

    // login message and bring user to homepage
    private void showAlertDialog(String title, String message, boolean isSuccessfulLoggout) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (isSuccessfulLoggout) {
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}