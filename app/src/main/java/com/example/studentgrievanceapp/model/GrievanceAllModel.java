package com.example.studentgrievanceapp.model;

import com.google.firebase.Timestamp;

public class GrievanceAllModel {
    private String documentId;
    private String title;
    private String status;
    private String category;
    private String description;
    private Timestamp dateTime;

    public GrievanceAllModel() {
        // Default constructor required for calls to DataSnapshot.getValue(GrievanceAllModel.class)
    }

    public GrievanceAllModel(String documenId, String title, String status, String category, String description, Timestamp dateTime) {
        this.title = title;
        this.status = status;
        this.category = category;
        this.description = description;
        this.dateTime = dateTime;
        this.documentId = documentId;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setGrievanceDocumentID(String documentID) {
        this.documentId = documentID;
    }
}
