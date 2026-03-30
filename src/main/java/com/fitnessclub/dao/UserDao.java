package com.fitnessclub.dao;

import com.fitnessclub.db.Database;
import com.fitnessclub.model.Role;
import com.fitnessclub.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, full_name, role, client_id FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, full_name, role, client_id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, password, full_name, role, client_id FROM users ORDER BY id";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(String username, String password, String fullName, Role role, Integer clientId) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, role, client_id) VALUES (?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            if (clientId == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, clientId);
            ps.executeUpdate();
        }
    }

    public void update(int id, String username, String password, String fullName, Role role, Integer clientId) throws SQLException {
        String sql = "UPDATE users SET username=?, password=?, full_name=?, role=?, client_id=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            if (clientId == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, clientId);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<User> findTrainers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, password, full_name, role, client_id FROM users WHERE role='TRAINER' ORDER BY full_name";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(Role.valueOf(rs.getString("role")));
        int cid = rs.getInt("client_id");
        if (rs.wasNull()) u.setClientId(null);
        else u.setClientId(cid);
        return u;
    }
}
