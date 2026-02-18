package com.example.onlineexam.dao;

import com.example.onlineexam.util.DatabaseUtil;

import java.sql.*;

public class StudentAnswerDao {

    public void saveAnswer(int attemptId, int questionId, Character selectedAnswer, boolean isCorrect, int marksObtained) {
        String sql = "INSERT INTO student_answers (attempt_id, question_id, selected_answer, is_correct, marks_obtained) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE selected_answer = VALUES(selected_answer), is_correct = VALUES(is_correct), marks_obtained = VALUES(marks_obtained)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            stmt.setString(3, selectedAnswer != null ? String.valueOf(selectedAnswer) : null);
            stmt.setBoolean(4, isCorrect);
            stmt.setInt(5, marksObtained);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Character getSelectedAnswer(int attemptId, int questionId) {
        String sql = "SELECT selected_answer FROM student_answers WHERE attempt_id = ? AND question_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String ans = rs.getString("selected_answer");
                return ans != null && !ans.isEmpty() ? ans.charAt(0) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteByAttemptId(int attemptId) {
        String sql = "DELETE FROM student_answers WHERE attempt_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
