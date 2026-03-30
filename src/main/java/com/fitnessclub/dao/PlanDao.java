package com.fitnessclub.dao;

import com.fitnessclub.db.Database;
import com.fitnessclub.model.PlanExercise;
import com.fitnessclub.model.TrainingPlan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlanDao {

    public List<TrainingPlan> findAll() throws SQLException {
        return queryPlans(null, null);
    }

    public List<TrainingPlan> findByTrainer(int trainerUserId) throws SQLException {
        return queryPlans(trainerUserId, null);
    }

    public List<TrainingPlan> findByClient(int clientId) throws SQLException {
        return queryPlans(null, clientId);
    }

    private List<TrainingPlan> queryPlans(Integer trainerUserId, Integer clientId) throws SQLException {
        List<TrainingPlan> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.client_id, c.name AS client_name, p.trainer_user_id, u.full_name AS trainer_name,
                   p.name, p.created_at
            FROM training_plans p
            JOIN clients c ON c.id = p.client_id
            JOIN users u ON u.id = p.trainer_user_id
            WHERE 1=1
            """);
        if (trainerUserId != null) sql.append(" AND p.trainer_user_id = ?");
        if (clientId != null) sql.append(" AND p.client_id = ?");
        sql.append(" ORDER BY p.created_at DESC, p.name");
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (trainerUserId != null) ps.setInt(i++, trainerUserId);
            if (clientId != null) ps.setInt(i, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPlan(rs));
                }
            }
        }
        return list;
    }

    public int insert(int clientId, int trainerUserId, String name) throws SQLException {
        String sql = "INSERT INTO training_plans (client_id, trainer_user_id, name, created_at) VALUES (?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, clientId);
            ps.setInt(2, trainerUserId);
            ps.setString(3, name);
            ps.setString(4, LocalDate.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Не получен id плана");
    }

    public void updateName(int planId, String name) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE training_plans SET name=? WHERE id=?")) {
            ps.setString(1, name);
            ps.setInt(2, planId);
            ps.executeUpdate();
        }
    }

    public void delete(int planId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM training_plans WHERE id=?")) {
            ps.setInt(1, planId);
            ps.executeUpdate();
        }
    }

    public List<PlanExercise> findExercisesForPlan(int planId) throws SQLException {
        List<PlanExercise> list = new ArrayList<>();
        String sql = """
            SELECT pe.id, pe.plan_id, pe.exercise_id, e.name AS exercise_name, pe.sets, pe.reps, pe.sort_order
            FROM plan_exercises pe
            JOIN exercises e ON e.id = pe.exercise_id
            WHERE pe.plan_id = ?
            ORDER BY pe.sort_order, pe.id
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlanExercise pe = new PlanExercise();
                    pe.setId(rs.getInt("id"));
                    pe.setPlanId(rs.getInt("plan_id"));
                    pe.setExerciseId(rs.getInt("exercise_id"));
                    pe.setExerciseName(rs.getString("exercise_name"));
                    pe.setSets(rs.getInt("sets"));
                    pe.setReps(rs.getInt("reps"));
                    pe.setSortOrder(rs.getInt("sort_order"));
                    list.add(pe);
                }
            }
        }
        return list;
    }

    public void addExerciseToPlan(int planId, int exerciseId, int sets, int reps, int sortOrder) throws SQLException {
        String sql = "INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, sort_order) VALUES (?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.setInt(2, exerciseId);
            ps.setInt(3, sets);
            ps.setInt(4, reps);
            ps.setInt(5, sortOrder);
            ps.executeUpdate();
        }
    }

    public void updatePlanExercise(int planExerciseId, int sets, int reps) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE plan_exercises SET sets=?, reps=? WHERE id=?")) {
            ps.setInt(1, sets);
            ps.setInt(2, reps);
            ps.setInt(3, planExerciseId);
            ps.executeUpdate();
        }
    }

    public void deletePlanExercise(int planExerciseId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM plan_exercises WHERE id=?")) {
            ps.setInt(1, planExerciseId);
            ps.executeUpdate();
        }
    }

    private static TrainingPlan mapPlan(ResultSet rs) throws SQLException {
        TrainingPlan p = new TrainingPlan();
        p.setId(rs.getInt("id"));
        p.setClientId(rs.getInt("client_id"));
        p.setClientName(rs.getString("client_name"));
        p.setTrainerUserId(rs.getInt("trainer_user_id"));
        p.setTrainerName(rs.getString("trainer_name"));
        p.setName(rs.getString("name"));
        p.setCreatedAt(LocalDate.parse(rs.getString("created_at")));
        return p;
    }
}
