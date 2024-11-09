package com.example.studentgrievanceapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentgrievanceapp.R;
import com.example.studentgrievanceapp.StatusActivity;
import com.example.studentgrievanceapp.StatusClosedActivity;
import com.example.studentgrievanceapp.StatusInProgressActivity;
import com.example.studentgrievanceapp.StatusPendingActivity;
import com.example.studentgrievanceapp.StatusResolvedActivity;
import com.example.studentgrievanceapp.model.GrievanceAllModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GrievanceAllAdapter extends RecyclerView.Adapter<GrievanceAllAdapter.GrievanceViewHolder> {

    private List<GrievanceAllModel> grievanceList;
    private Context context;

    public GrievanceAllAdapter(List<GrievanceAllModel> grievanceList, Context context) {
        this.grievanceList = grievanceList;
        this.context = context;
    }

    @NonNull
    @Override
    public GrievanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_grievance_all_items, parent, false);
        return new GrievanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrievanceViewHolder holder, int position) {
        GrievanceAllModel grievance = grievanceList.get(position);
        holder.grievanceTitle.setText(grievance.getTitle());
        holder.grievanceStatus.setText(grievance.getStatus());
        holder.grievanceCategory.setText(grievance.getCategory());

        if (grievance.getDateTime() != null) {
            Date date = grievance.getDateTime().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            holder.grievanceDateTime.setText(formattedDate);
        } else {
            holder.grievanceDateTime.setText("No date available");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatusActivity.class);
            intent.putExtra("document_id", grievance.getDocumentId());
            intent.putExtra("grievance_title", grievance.getTitle());
            intent.putExtra("grievance_status", grievance.getStatus());
            intent.putExtra("grievance_category", grievance.getCategory());
            intent.putExtra("grievance_description", grievance.getDescription());
            Date date = grievance.getDateTime().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            intent.putExtra("grievance_dateTime", formattedDate);
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatusPendingActivity.class);
            intent.putExtra("document_id", grievance.getDocumentId());
            intent.putExtra("grievance_title", grievance.getTitle());
            intent.putExtra("grievance_status", grievance.getStatus());
            intent.putExtra("grievance_category", grievance.getCategory());
            intent.putExtra("grievance_description", grievance.getDescription());
            Date date = grievance.getDateTime().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            intent.putExtra("grievance_dateTime", formattedDate);
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatusInProgressActivity.class);
            intent.putExtra("document_id", grievance.getDocumentId());
            intent.putExtra("grievance_title", grievance.getTitle());
            intent.putExtra("grievance_status", grievance.getStatus());
            intent.putExtra("grievance_category", grievance.getCategory());
            intent.putExtra("grievance_description", grievance.getDescription());
            Date date = grievance.getDateTime().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            intent.putExtra("grievance_dateTime", formattedDate);
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatusResolvedActivity.class);
            intent.putExtra("document_id", grievance.getDocumentId());
            intent.putExtra("grievance_title", grievance.getTitle());
            intent.putExtra("grievance_status", grievance.getStatus());
            intent.putExtra("grievance_category", grievance.getCategory());
            intent.putExtra("grievance_description", grievance.getDescription());
            Date date = grievance.getDateTime().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            intent.putExtra("grievance_dateTime", formattedDate);
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatusClosedActivity.class);
            intent.putExtra("document_id", grievance.getDocumentId());
            intent.putExtra("grievance_title", grievance.getTitle());
            intent.putExtra("grievance_status", grievance.getStatus());
            intent.putExtra("grievance_category", grievance.getCategory());
            intent.putExtra("grievance_description", grievance.getDescription());
            Date date = grievance.getDateTime().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            intent.putExtra("grievance_dateTime", formattedDate);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return grievanceList.size();
    }

    public static class GrievanceViewHolder extends RecyclerView.ViewHolder {
        TextView grievanceTitle, grievanceStatus, grievanceCategory, grievanceDateTime;

        public GrievanceViewHolder(@NonNull View itemView) {
            super(itemView);
            grievanceTitle = itemView.findViewById(R.id.grievanceTitle);
            grievanceStatus = itemView.findViewById(R.id.grievanceStatus);
            grievanceCategory = itemView.findViewById(R.id.grievanceType);
            grievanceDateTime = itemView.findViewById(R.id.grievanceDate);
        }
    }

    public void updateData(List<GrievanceAllModel> newGrievanceList) {
        this.grievanceList.clear();
        this.grievanceList.addAll(newGrievanceList);
        notifyDataSetChanged(); // Refresh the view
    }

    public void setFilter(List<GrievanceAllModel> filteredList) {
        this.grievanceList.clear();  // Clear previous items
        this.grievanceList.addAll(filteredList);
        notifyDataSetChanged();
    }
}
