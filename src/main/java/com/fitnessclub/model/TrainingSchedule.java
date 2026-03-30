package com.fitnessclub.model;

import java.time.LocalDateTime;

public class TrainingSchedule {
    private int id;
    private int trainerUserId;
    private String trainerName;
    private int clientId;
    private String clientName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTrainerUserId() { return trainerUserId; }
    public void setTrainerUserId(int trainerUserId) { this.trainerUserId = trainerUserId; }
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
