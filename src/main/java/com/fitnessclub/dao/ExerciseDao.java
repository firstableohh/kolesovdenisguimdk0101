package com.fitnessclub.dao;

import com.fitnessclub.db.Database;
import com.fitnessclub.model.Exercise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDao {

    public List<Exercise> findAll() throws SQLException {
        List<Exercise> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM exercises ORDER BY name";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Exercise(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
        }
        return list;
    }

    public void insert(String name, String description) throws SQLException {
        String sql = "INSERT INTO exercises (name, description) VALUES (?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description == null ? "" : description);
            ps.executeUpdate();
        }
    }
}
