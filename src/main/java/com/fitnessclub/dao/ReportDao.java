package com.fitnessclub.dao;

import com.fitnessclub.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDao {

    public static class AttendanceRow {
        public final int clientId;
        public final String clientName;
        public final int totalSessions;
        public final int attended;
        public final int missed;

        public AttendanceRow(int clientId, String clientName, int totalSessions, int attended, int missed) {
            this.clientId = clientId;
            this.clientName = clientName;
            this.totalSessions = totalSessions;
            this.attended = attended;
            this.missed = missed;
        }
    }

    public List<AttendanceRow> attendanceByClient() throws SQLException {
        Map<Integer, AttendanceRow> map = new LinkedHashMap<>();
        String schedulesPerClient = """
            SELECT client_id, c.name, COUNT(*) AS cnt
            FROM training_schedules s JOIN clients c ON c.id = s.client_id
            GROUP BY client_id, c.name
            """;
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(schedulesPerClient)) {
            while (rs.next()) {
                int cid = rs.getInt("client_id");
                map.put(cid, new AttendanceRow(cid, rs.getString("name"), rs.getInt("cnt"), 0, 0));
            }
        }
        String attended = """
            SELECT s.client_id, SUM(CASE WHEN a.attended = 1 THEN 1 ELSE 0 END) AS att,
                   SUM(CASE WHEN a.attended = 0 THEN 1 ELSE 0 END) AS miss
            FROM attendance a
            JOIN training_schedules s ON s.id = a.schedule_id
            GROUP BY s.client_id
            """;
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(attended)) {
            while (rs.next()) {
                int cid = rs.getInt("client_id");
                AttendanceRow row = map.get(cid);
                if (row != null) {
                    int att = rs.getInt("att");
                    int miss = rs.getInt("miss");
                    map.put(cid, new AttendanceRow(cid, row.clientName, row.totalSessions, att, miss));
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    public String buildAttendanceReportText() throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("Отчёт о посещаемости\n");
        sb.append("====================\n\n");
        for (AttendanceRow r : attendanceByClient()) {
            sb.append(String.format("Клиент: %s (id %d)\n", r.clientName, r.clientId));
            sb.append(String.format("  Записей в расписании: %d\n", r.totalSessions));
            sb.append(String.format("  Отмечено посещений: %d, пропусков: %d\n\n", r.attended, r.missed));
        }
        if (sb.length() < 80) {
            sb.append("(Нет данных — добавьте расписание и отметки посещаемости.)\n");
        }
        return sb.toString();
    }
}
