package com.example.onlineexam.dao;

import com.example.onlineexam.model.StudentAnswer;
import com.example.onlineexam.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentAnswerDao {

    public void saveAnswer(int attemptId, int questionId, Character selectedAnswer, boolean isCorrect, int marksObtained) {
        String sql = "INSERT INTO student_answers (attempt_id, question_id, selected_answer, is_correct, marks_obtained) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE selected_answer = VALUES(selected_answer), " +
                "is_correct = VALUES(is_correct), marks_obtained = VALUES(marks_obtained)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);

            if (selectedAnswer == null) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, String.valueOf(selectedAnswer));
            }

            stmt.setBoolean(4, isCorrect);
            stmt.setInt(5, marksObtained);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<StudentAnswer> findByAttemptId(int attemptId) {
        List<StudentAnswer> list = new ArrayList<>();
        String sql = "SELECT answer_id, attempt_id, question_id, selected_answer, is_correct, marks_obtained " +
                "FROM student_answers WHERE attempt_id = ? ORDER BY question_id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentAnswer sa = new StudentAnswer();
                sa.setAnswerId(rs.getInt("answer_id"));
                sa.setAttemptId(rs.getInt("attempt_id"));
                sa.setQuestionId(rs.getInt("question_id"));

                String ans = rs.getString("selected_answer");
                if (ans != null && !ans.isEmpty()) {
                    sa.setSelectedAnswer(ans.charAt(0));
                }

                sa.setCorrect(rs.getBoolean("is_correct"));
                sa.setMarksObtained(rs.getInt("marks_obtained"));
                list.add(sa);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}