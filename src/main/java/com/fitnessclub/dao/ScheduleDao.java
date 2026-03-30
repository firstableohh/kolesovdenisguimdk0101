package com.fitnessclub.dao;

import com.fitnessclub.db.Database;
import com.fitnessclub.model.TrainingSchedule;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDao {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<TrainingSchedule> findAll() throws SQLException {
        String sql = """
            SELECT s.id, s.trainer_user_id, u.full_name AS trainer_name, s.client_id, c.name AS client_name,
                   s.start_time, s.end_time, s.notes
            FROM training_schedules s
            JOIN users u ON u.id = s.trainer_user_id
            JOIN clients c ON c.id = s.client_id
            ORDER BY s.start_time
            """;
        return queryList(sql, null, null);
    }

    public List<TrainingSchedule> findByTrainer(int trainerUserId) throws SQLException {
        String sql = """
            SELECT s.id, s.trainer_user_id, u.full_name AS trainer_name, s.client_id, c.name AS client_name,
                   s.start_time, s.end_time, s.notes
            FROM training_schedules s
            JOIN users u ON u.id = s.trainer_user_id
            JOIN clients c ON c.id = s.client_id
            WHERE s.trainer_user_id = ?
            ORDER BY s.start_time
            """;
        return queryList(sql, trainerUserId, null);
    }

    public List<TrainingSchedule> findByClient(int clientId) throws SQLException {
        String sql = """
            SELECT s.id, s.trainer_user_id, u.full_name AS trainer_name, s.client_id, c.name AS client_name,
                   s.start_time, s.end_time, s.notes
            FROM training_schedules s
            JOIN users u ON u.id = s.trainer_user_id
            JOIN clients c ON c.id = s.client_id
            WHERE s.client_id = ?
            ORDER BY s.start_time
            """;
        return queryList(sql, null, clientId);
    }

    private List<TrainingSchedule> queryList(String sql, Integer trainerId, Integer clientId) throws SQLException {
        List<TrainingSchedule> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (trainerId != null) ps.setInt(1, trainerId);
            else if (clientId != null) ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void insert(int trainerUserId, int clientId, LocalDateTime start, LocalDateTime end, String notes) throws SQLException {
        String sql = "INSERT INTO training_schedules (trainer_user_id, client_id, start_time, end_time, notes) VALUES (?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerUserId);
            ps.setInt(2, clientId);
            ps.setString(3, start.format(ISO));
            ps.setString(4, end.format(ISO));
            ps.setString(5, notes == null ? "" : notes);
            ps.executeUpdate();
        }
    }

    public void update(int id, int trainerUserId, int clientId, LocalDateTime start, LocalDateTime end, String notes) throws SQLException {
        String sql = "UPDATE training_schedules SET trainer_user_id=?, client_id=?, start_time=?, end_time=?, notes=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainerUserId);
            ps.setInt(2, clientId);
            ps.setString(3, start.format(ISO));
            ps.setString(4, end.format(ISO));
            ps.setString(5, notes == null ? "" : notes);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM training_schedules WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static TrainingSchedule map(ResultSet rs) throws SQLException {
        TrainingSchedule s = new TrainingSchedule();
        s.setId(rs.getInt("id"));
        s.setTrainerUserId(rs.getInt("trainer_user_id"));
        s.setTrainerName(rs.getString("trainer_name"));
        s.setClientId(rs.getInt("client_id"));
        s.setClientName(rs.getString("client_name"));
        s.setStartTime(LocalDateTime.parse(rs.getString("start_time"), ISO));
        s.setEndTime(LocalDateTime.parse(rs.getString("end_time"), ISO));
        s.setNotes(rs.getString("notes"));
        return s;
    }
}
