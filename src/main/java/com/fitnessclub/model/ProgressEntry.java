package com.fitnessclub.model;

import java.time.LocalDate;

public class ProgressEntry {
    private int id;
    private int planExerciseId;
    private String exerciseName;
    private LocalDate logDate;
    private int completedSets;
    private int completedReps;
    private String notes;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPlanExerciseId() { return planExerciseId; }
    public void setPlanExerciseId(int planExerciseId) { this.planExerciseId = planExerciseId; }
    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public int getCompletedSets() { return completedSets; }
    public void setCompletedSets(int completedSets) { this.completedSets = completedSets; }
    public int getCompletedReps() { return completedReps; }
    public void setCompletedReps(int completedReps) { this.completedReps = completedReps; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
