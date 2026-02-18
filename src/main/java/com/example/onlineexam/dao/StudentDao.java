package com.example.onlineexam.dao;

import com.example.onlineexam.model.Student;
import com.example.onlineexam.util.DatabaseUtil;

import java.sql.*;

public class StudentDao {

    public Student findByUserId(int userId) {
        String sql = "SELECT student_id, user_id, full_name, enrollment_no, contact FROM students WHERE user_id = ?";
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

    public Student findById(int studentId) {
        String sql = "SELECT student_id, user_id, full_name, enrollment_no, contact FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Student findByEnrollmentNo(String enrollmentNo) {
        String sql = "SELECT student_id, user_id, full_name, enrollment_no, contact FROM students WHERE enrollment_no = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, enrollmentNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int create(Student student) {
        String sql = "INSERT INTO students (user_id, full_name, enrollment_no, contact) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, student.getUserId());
            stmt.setString(2, student.getFullName());
            stmt.setString(3, student.getEnrollmentNo());
            stmt.setString(4, student.getContact());
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

    private Student mapResultSet(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setFullName(rs.getString("full_name"));
        s.setEnrollmentNo(rs.getString("enrollment_no"));
        s.setContact(rs.getString("contact"));
        return s;
    }
}
