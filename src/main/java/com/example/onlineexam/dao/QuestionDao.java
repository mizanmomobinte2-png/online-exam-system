package com.example.onlineexam.dao;

import com.example.onlineexam.model.Question;
import com.example.onlineexam.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDao {

    public List<Question> findByExamId(int examId) {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT question_id, exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer, marks, question_order FROM questions WHERE exam_id = ? ORDER BY question_order, question_id";
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

    public int addQuestion(Question q) {
        String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer, marks, question_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, q.getExamId());
            stmt.setString(2, q.getQuestionText());
            stmt.setString(3, q.getOptionA());
            stmt.setString(4, q.getOptionB());
            stmt.setString(5, q.getOptionC());
            stmt.setString(6, q.getOptionD());
            stmt.setString(7, String.valueOf(q.getCorrectAnswer()));
            stmt.setInt(8, q.getMarks());
            stmt.setInt(9, q.getQuestionOrder());
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

    public void deleteByExamId(int examId) {
        String sql = "DELETE FROM questions WHERE exam_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Question mapResultSet(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("question_id"));
        q.setExamId(rs.getInt("exam_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectAnswer(rs.getString("correct_answer").charAt(0));
        q.setMarks(rs.getInt("marks"));
        q.setQuestionOrder(rs.getInt("question_order"));
        return q;
    }

public Question findById(int questionId) {
    String sql = "SELECT question_id, exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer, marks, question_order " +
            "FROM questions WHERE question_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, questionId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return mapResultSet(rs);
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

public void updateQuestion(Question q) {
    String sql = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_answer=?, marks=?, question_order=? " +
            "WHERE question_id=?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, q.getQuestionText());
        stmt.setString(2, q.getOptionA());
        stmt.setString(3, q.getOptionB());
        stmt.setString(4, q.getOptionC());
        stmt.setString(5, q.getOptionD());
        stmt.setString(6, String.valueOf(q.getCorrectAnswer()));
        stmt.setInt(7, q.getMarks());
        stmt.setInt(8, q.getQuestionOrder());
        stmt.setInt(9, q.getQuestionId());

        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public void deleteQuestion(int questionId) {
    String sql = "DELETE FROM questions WHERE question_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, questionId);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


}
