package com.fitnessclub.dao;

import com.fitnessclub.db.Database;
import com.fitnessclub.model.ProgressEntry;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProgressDao {

    public List<ProgressEntry> findByPlan(int planId) throws SQLException {
        List<ProgressEntry> list = new ArrayList<>();
        String sql = """
            SELECT pl.id, pl.plan_exercise_id, e.name AS exercise_name, pl.log_date, pl.completed_sets, pl.completed_reps, pl.notes
            FROM progress_log pl
            JOIN plan_exercises pe ON pe.id = pl.plan_exercise_id
            JOIN exercises e ON e.id = pe.exercise_id
            WHERE pe.plan_id = ?
            ORDER BY pl.log_date DESC, pl.id DESC
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<ProgressEntry> findByClientPlans(int clientId) throws SQLException {
        List<ProgressEntry> list = new ArrayList<>();
        String sql = """
            SELECT pl.id, pl.plan_exercise_id, e.name AS exercise_name, pl.log_date, pl.completed_sets, pl.completed_reps, pl.notes
            FROM progress_log pl
            JOIN plan_exercises pe ON pe.id = pl.plan_exercise_id
            JOIN training_plans tp ON tp.id = pe.plan_id
            JOIN exercises e ON e.id = pe.exercise_id
            WHERE tp.client_id = ?
            ORDER BY pl.log_date DESC, pl.id DESC
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void insert(int planExerciseId, LocalDate date, int completedSets, int completedReps, String notes) throws SQLException {
        String sql = "INSERT INTO progress_log (plan_exercise_id, log_date, completed_sets, completed_reps, notes) VALUES (?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planExerciseId);
            ps.setString(2, date.toString());
            ps.setInt(3, completedSets);
            ps.setInt(4, completedReps);
            ps.setString(5, notes == null ? "" : notes);
            ps.executeUpdate();
        }
    }

    public void update(int id, LocalDate date, int completedSets, int completedReps, String notes) throws SQLException {
        String sql = "UPDATE progress_log SET log_date=?, completed_sets=?, completed_reps=?, notes=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setInt(2, completedSets);
            ps.setInt(3, completedReps);
            ps.setString(4, notes == null ? "" : notes);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM progress_log WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static ProgressEntry map(ResultSet rs) throws SQLException {
        ProgressEntry p = new ProgressEntry();
        p.setId(rs.getInt("id"));
        p.setPlanExerciseId(rs.getInt("plan_exercise_id"));
        p.setExerciseName(rs.getString("exercise_name"));
        p.setLogDate(LocalDate.parse(rs.getString("log_date")));
        p.setCompletedSets(rs.getInt("completed_sets"));
        p.setCompletedReps(rs.getInt("completed_reps"));
        p.setNotes(rs.getString("notes"));
        return p;
    }
}
