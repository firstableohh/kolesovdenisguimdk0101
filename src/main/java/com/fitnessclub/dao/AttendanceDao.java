package com.fitnessclub.dao;

import com.fitnessclub.db.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceDao {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void record(int scheduleId, int clientId, boolean attended) throws SQLException {
        String sql = "INSERT INTO attendance (schedule_id, client_id, attended, recorded_at) VALUES (?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ps.setInt(2, clientId);
            ps.setInt(3, attended ? 1 : 0);
            ps.setString(4, LocalDateTime.now().format(ISO));
            ps.executeUpdate();
        }
    }

    /** Удалить предыдущую запись по тому же занятию и клиенту (для перезаписи). */
    public void replaceForScheduleClient(int scheduleId, int clientId, boolean attended) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM attendance WHERE schedule_id=? AND client_id=?")) {
                del.setInt(1, scheduleId);
                del.setInt(2, clientId);
                del.executeUpdate();
            }
            record(scheduleId, clientId, attended);
        }
    }
}
