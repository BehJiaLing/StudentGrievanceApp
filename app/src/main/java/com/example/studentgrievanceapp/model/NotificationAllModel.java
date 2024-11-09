package com.example.studentgrievanceapp.model;

import com.google.firebase.Timestamp;

public class NotificationAllModel {
    private String documentId;
    private String grievance_submission_id;
    private String name;
    private String grievance_submission_status;
    private String grievance_submission_category;
    private String grievance_submission_title;
    private String grievance_submission_description;
    private Timestamp updated_time;
    private boolean viewed;

    public NotificationAllModel() {
        // Default constructor required for calls to DataSnapshot.getValue(GrievanceAllModel.class)
    }

    public NotificationAllModel(String documentId, String grievance_submission_id, String name, String grievance_submission_status, String grievance_submission_category, String grievance_submission_title, String grievance_submission_description, Timestamp updated_time, Boolean viewed) {
        this.documentId = documentId;
        this.grievance_submission_id = grievance_submission_id;
        this.name = name;
        this.grievance_submission_status = grievance_submission_status;
        this.grievance_submission_category = grievance_submission_category;
        this.grievance_submission_title = grievance_submission_title;
        this.grievance_submission_description = grievance_submission_description;
        this.updated_time = updated_time;
        this.viewed = viewed;
    }

    // Getters


    public String getDocumentId() {
        return documentId;
    }

    public void setNotificationDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getGrievance_submission_title() {
        return grievance_submission_title;
    }

    public String getGrievance_submission_status() {
        return grievance_submission_status;
    }

    public String getGrievance_submission_category() {
        return grievance_submission_category;
    }

    public String getGrievance_submission_description() {
        return grievance_submission_description;
    }

    public Timestamp getUpdated_time() {
        return updated_time;
    }

    public String getName() {
        return name;
    }

    public String getGrievance_submission_id() {
        return grievance_submission_id;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }
}
