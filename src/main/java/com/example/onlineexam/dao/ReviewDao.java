package com.example.onlineexam.dao;

import com.example.onlineexam.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    public static class ReviewItem {
        public String questionText;
        public Character selectedAnswer;  // nullable
        public char correctAnswer;
    }

    // ✅ include ALL questions of that exam, even unanswered
    public List<ReviewItem> getReviewItems(int attemptId) {
        List<ReviewItem> list = new ArrayList<>();

        String sql =
            "SELECT q.question_text, q.correct_answer, sa.selected_answer " +
            "FROM exam_attempts ea " +
            "JOIN questions q ON q.exam_id = ea.exam_id " +
            "LEFT JOIN student_answers sa ON sa.attempt_id = ea.attempt_id AND sa.question_id = q.question_id " +
            "WHERE ea.attempt_id = ? " +
            "ORDER BY q.question_order, q.question_id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ReviewItem it = new ReviewItem();
                it.questionText = rs.getString("question_text");
                String corr = rs.getString("correct_answer");
                it.correctAnswer = (corr != null && !corr.isEmpty()) ? corr.charAt(0) : 'A';

                String sel = rs.getString("selected_answer");
                it.selectedAnswer = (sel != null && !sel.isEmpty()) ? sel.charAt(0) : null;

                list.add(it);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}

