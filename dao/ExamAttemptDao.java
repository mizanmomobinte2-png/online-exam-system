package com.example.onlineexam.dao;

import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.model.ExamAttempt.AttemptStatus;
import com.example.onlineexam.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamAttemptDao {

    public int create(ExamAttempt attempt) {
        String sql = "INSERT INTO exam_attempts (exam_id, student_id, start_time, total_marks, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attempt.getExamId());
            stmt.setInt(2, attempt.getStudentId());
            stmt.setObject(3, attempt.getStartTime());
            stmt.setInt(4, attempt.getTotalMarks());
            stmt.setString(5, AttemptStatus.IN_PROGRESS.name());
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

    public ExamAttempt findByExamAndStudent(int examId, int studentId) {
        String sql = "SELECT attempt_id, exam_id, student_id, start_time, end_time, score, total_marks, status FROM exam_attempts WHERE exam_id = ? AND student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            stmt.setInt(2, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ExamAttempt findById(int attemptId) {
        String sql = "SELECT attempt_id, exam_id, student_id, start_time, end_time, score, total_marks, status FROM exam_attempts WHERE attempt_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ExamAttempt> findByStudentId(int studentId) {
        List<ExamAttempt> list = new ArrayList<>();
        String sql = "SELECT attempt_id, exam_id, student_id, start_time, end_time, score, total_marks, status FROM exam_attempts WHERE student_id = ? AND status = 'SUBMITTED' ORDER BY end_time DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ExamAttempt> findByExamId(int examId) {
        List<ExamAttempt> list = new ArrayList<>();
        String sql = "SELECT attempt_id, exam_id, student_id, start_time, end_time, score, total_marks, status FROM exam_attempts WHERE exam_id = ? AND status = 'SUBMITTED' ORDER BY score DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void submit(int attemptId, int score, LocalDateTime endTime) {
        String sql = "UPDATE exam_attempts SET score = ?, end_time = ?, status = 'SUBMITTED' WHERE attempt_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setObject(2, endTime);
            stmt.setInt(3, attemptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ExamAttempt mapResultSet(ResultSet rs) throws SQLException {
        ExamAttempt a = new ExamAttempt();
        a.setAttemptId(rs.getInt("attempt_id"));
        a.setExamId(rs.getInt("exam_id"));
        a.setStudentId(rs.getInt("student_id"));
        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) a.setStartTime(st.toLocalDateTime());
        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) a.setEndTime(et.toLocalDateTime());
        a.setScore(rs.getInt("score"));
        a.setTotalMarks(rs.getInt("total_marks"));
        a.setStatus(AttemptStatus.valueOf(rs.getString("status")));
        return a;
    }
}
