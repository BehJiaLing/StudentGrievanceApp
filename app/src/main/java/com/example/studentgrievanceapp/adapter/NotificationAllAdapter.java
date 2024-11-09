package com.example.studentgrievanceapp.adapter;

import android.content.Context;
import android.content.Intent;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentgrievanceapp.NotificationActivity;
import com.example.studentgrievanceapp.R;
import com.example.studentgrievanceapp.StatusActivity;
import com.example.studentgrievanceapp.StatusClosedActivity;
import com.example.studentgrievanceapp.StatusInProgressActivity;
import com.example.studentgrievanceapp.StatusNotificationActivity;
import com.example.studentgrievanceapp.StatusPendingActivity;
import com.example.studentgrievanceapp.StatusResolvedActivity;
import com.example.studentgrievanceapp.model.NotificationAllModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAllAdapter extends RecyclerView.Adapter<NotificationAllAdapter.NotificationViewHolder> {

    private List<NotificationAllModel> notificationList;
    private Context context;
    private Set<String> viewedNotifications;

    public NotificationAllAdapter(List<NotificationAllModel> notificationList, Context context, Set<String> viewedNotifications) {
        this.notificationList = notificationList;
        this.context = context;
        this.viewedNotifications = viewedNotifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_notification_all_items, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationAllModel notification = notificationList.get(position);

        // Bind data to views
        holder.notificationTitle.setText(notification.getName());
        holder.grievanceTitle.setText(notification.getGrievance_submission_title());
        holder.grievanceStatus.setText(notification.getGrievance_submission_status());
        holder.redDot.setVisibility(notification.isViewed() ? View.GONE : View.VISIBLE);

        if (notification.getUpdated_time() != null) {
            Date date = notification.getUpdated_time().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            holder.notificationDate.setText(formattedDate);
        } else {
            holder.notificationDate.setText("No date available");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatusNotificationActivity.class);
            intent.putExtra("document_id", notification.getGrievance_submission_id());
            intent.putExtra("grievance_title", notification.getGrievance_submission_title());
            intent.putExtra("grievance_status", notification.getGrievance_submission_status());
            intent.putExtra("grievance_category", notification.getGrievance_submission_category());
            intent.putExtra("grievance_description", notification.getGrievance_submission_description());

            Date date = notification.getUpdated_time().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String formattedDate = formatter.format(date);
            intent.putExtra("grievance_updatedTime", formattedDate);

            // Mark notification as viewed
            notification.setViewed(true);
            holder.redDot.setVisibility(View.GONE);

            // Save viewed status in SharedPreferences
            if (context instanceof NotificationActivity) {
                ((NotificationActivity) context).saveViewedStatus(notification.getDocumentId());
            }

            // Refresh the adapter to keep the view consistent
            notifyItemChanged(holder.getAdapterPosition());

            context.startActivity(intent);
        });

    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView grievanceTitle, grievanceStatus, notificationTitle, notificationDate;
        View redDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            grievanceStatus = itemView.findViewById(R.id.grievanceStatus);
            grievanceTitle = itemView.findViewById(R.id.grievanceTitle);
            notificationDate = itemView.findViewById(R.id.notificationDate);
            redDot = itemView.findViewById(R.id.redDot);
        }
    }

    public void updateData(List<NotificationAllModel> newNotificationList) {
        this.notificationList.clear();
        this.notificationList.addAll(newNotificationList);
        notifyDataSetChanged();
    }
}
