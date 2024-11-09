package com.example.studentgrievanceapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentgrievanceapp.adapter.GrievanceAllAdapter;
import com.example.studentgrievanceapp.model.GrievanceAllModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class GrievanceInProgressActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView grievanceRecyclerView;
    private GrievanceAllAdapter grievanceAdapter;
    private List<GrievanceAllModel> grievanceList;
    private List<GrievanceAllModel> originalGrievanceList;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grievance);

        // Progress Bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("In Progress Grievances");

        db = FirebaseFirestore.getInstance();
        grievanceRecyclerView = findViewById(R.id.grievanceRecyclerView);
        grievanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        grievanceList = new ArrayList<>();
        originalGrievanceList = new ArrayList<>();
        grievanceAdapter = new GrievanceAllAdapter(grievanceList, this);
        grievanceRecyclerView.setAdapter(grievanceAdapter);

        loadGrievances();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGrievances();
    }

    private void loadGrievances() {
        progressDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = user != null ? user.getEmail() : null;

        RecyclerView grievanceRecyclerView = findViewById(R.id.grievanceRecyclerView);
        LinearLayout displayNothing = findViewById(R.id.display_nothing);

        if (currentUserEmail != null) {
            db.collection("grievance")
                    .whereEqualTo("email", currentUserEmail)
                    .whereEqualTo("status", "In Progress")
                    .orderBy("dateTime", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            grievanceList.clear(); // Clear previous data
                            originalGrievanceList.clear(); // Clear the original list

                            for (DocumentSnapshot document : task.getResult()) {
                                GrievanceAllModel grievance = document.toObject(GrievanceAllModel.class);
                                grievance.setGrievanceDocumentID(document.getId());
                                grievanceList.add(grievance);
                                originalGrievanceList.add(grievance);
                            }
                            grievanceAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();

                            if (grievanceList.isEmpty()) {
                                grievanceRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
                                displayNothing.setVisibility(View.VISIBLE); // Show "no results" message
                            } else {
                                grievanceRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
                                displayNothing.setVisibility(View.GONE); // Hide "no results" message
                            }
                        } else {
                            progressDialog.dismiss();
                            showAlertDialog("Error", "Failed to load grievances.");
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grievance_all_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.toolbar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Type here to search title...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });
        return true;
    }

    private void filterList(String newText) {
        RecyclerView grievanceRecyclerView = findViewById(R.id.grievanceRecyclerView);
        LinearLayout displayNothing = findViewById(R.id.display_nothing);

        String filterString = newText.toLowerCase().trim();
        List<GrievanceAllModel> filteredList = new ArrayList<>();

        for (GrievanceAllModel grievance : originalGrievanceList) {
            if (grievance.getTitle().toLowerCase().contains(filterString)) {
                filteredList.add(grievance);
            }
        }

        if (filteredList.isEmpty()) {
            grievanceRecyclerView.setVisibility(View.GONE);
            displayNothing.setVisibility(View.VISIBLE);
        } else {
            grievanceRecyclerView.setVisibility(View.VISIBLE);
            displayNothing.setVisibility(View.GONE);
            grievanceAdapter.setFilter(filteredList);
        }
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
        List<GrievanceAllModel> filteredList = new ArrayList<>();

        if (type.equals("All")) {
            filteredList.addAll(originalGrievanceList);
        } else {
            for (GrievanceAllModel grievance : originalGrievanceList) {
                if (grievance.getCategory().equals(type)) {
                    filteredList.add(grievance);
                }
            }
        }

        grievanceAdapter.updateData(filteredList); // Update the adapter with filtered data
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
