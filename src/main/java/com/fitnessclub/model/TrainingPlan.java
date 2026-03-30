package com.fitnessclub.model;

import java.time.LocalDate;

public class TrainingPlan {
    private int id;
    private int clientId;
    private String clientName;
    private int trainerUserId;
    private String trainerName;
    private String name;
    private LocalDate createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public int getTrainerUserId() { return trainerUserId; }
    public void setTrainerUserId(int trainerUserId) { this.trainerUserId = trainerUserId; }
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " — " + clientName;
    }
}
