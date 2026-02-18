package com.example.onlineexam.dao;

import com.example.onlineexam.model.Teacher;
import com.example.onlineexam.util.DatabaseUtil;

import java.sql.*;

public class TeacherDao {

    public Teacher findByUserId(int userId) {
        String sql = "SELECT teacher_id, user_id, full_name, department, contact FROM teachers WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int create(Teacher teacher) {
        String sql = "INSERT INTO teachers (user_id, full_name, department, contact) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, teacher.getUserId());
            stmt.setString(2, teacher.getFullName());
            stmt.setString(3, teacher.getDepartment());
            stmt.setString(4, teacher.getContact());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private Teacher mapResultSet(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setTeacherId(rs.getInt("teacher_id"));
        t.setUserId(rs.getInt("user_id"));
        t.setFullName(rs.getString("full_name"));
        t.setDepartment(rs.getString("department"));
        t.setContact(rs.getString("contact"));
        return t;
    }
}
