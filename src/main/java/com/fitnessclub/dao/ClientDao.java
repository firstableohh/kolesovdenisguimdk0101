package com.fitnessclub.dao;

import com.fitnessclub.db.Database;
import com.fitnessclub.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDao {

    public List<Client> findAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT id, name, age, gender, phone, email, user_id FROM clients ORDER BY name";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Client> findById(int id) throws SQLException {
        String sql = "SELECT id, name, age, gender, phone, email, user_id FROM clients WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public int insert(String name, int age, String gender, String phone, String email, Integer userId) throws SQLException {
        String sql = "INSERT INTO clients (name, age, gender, phone, email, user_id) VALUES (?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, phone);
            ps.setString(5, email);
            if (userId == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Не получен id клиента");
    }

    public void update(int id, String name, int age, String gender, String phone, String email, Integer userId) throws SQLException {
        String sql = "UPDATE clients SET name=?, age=?, gender=?, phone=?, email=?, user_id=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, phone);
            ps.setString(5, email);
            if (userId == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, userId);
            ps.setInt(7, id);
            ps.executeUpdate();
        }
    }

    public void clearUserIdForUser(int userId) throws SQLException {
        String sql = "UPDATE clients SET user_id = NULL WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM clients WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void setUserId(int clientId, Integer userId) throws SQLException {
        String sql = "UPDATE clients SET user_id=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, userId);
            ps.setInt(2, clientId);
            ps.executeUpdate();
        }
    }

    private static Client map(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setAge(rs.getInt("age"));
        c.setGender(rs.getString("gender"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        int uid = rs.getInt("user_id");
        if (rs.wasNull()) c.setUserId(null);
        else c.setUserId(uid);
        return c;
    }
}
