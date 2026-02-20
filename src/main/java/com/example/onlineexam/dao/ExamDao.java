package com.example.onlineexam.dao;

import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.Exam.ExamStatus;
import com.example.onlineexam.model.Question;
import com.example.onlineexam.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamDao {

    public int create(Exam exam) {
        String sql = "INSERT INTO exams (title, description, duration_minutes, total_marks, passing_marks, created_by, status, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, exam.getTitle());
            stmt.setString(2, exam.getDescription());
            stmt.setInt(3, exam.getDurationMinutes());
            stmt.setInt(4, exam.getTotalMarks());
            stmt.setInt(5, exam.getPassingMarks());
            stmt.setInt(6, exam.getCreatedBy());
            stmt.setString(7, exam.getStatus() != null ? exam.getStatus().name() : ExamStatus.DRAFT.name());
            stmt.setObject(8, exam.getStartTime());
            stmt.setObject(9, exam.getEndTime());
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

    public Exam findById(int examId) {
        String sql = "SELECT exam_id, title, description, duration_minutes, total_marks, passing_marks, created_by, status, start_time, end_time, created_at FROM exams WHERE exam_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Exam> findByTeacherId(int teacherId) {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT exam_id, title, description, duration_minutes, total_marks, passing_marks, created_by, status, start_time, end_time, created_at FROM exams WHERE created_by = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Exam> findAvailableForStudent(int studentId) {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT e.exam_id, e.title, e.description, e.duration_minutes, e.total_marks, e.passing_marks, e.created_by, e.status, e.start_time, e.end_time, e.created_at " +
                "FROM exams e WHERE e.status = 'ACTIVE' AND e.exam_id NOT IN (SELECT exam_id FROM exam_attempts WHERE student_id = ?) " +
                "AND (e.start_time IS NULL OR e.start_time <= NOW()) AND (e.end_time IS NULL OR e.end_time >= NOW()) ORDER BY e.created_at DESC";
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

    public void updateStatus(int examId, ExamStatus status) {
        String sql = "UPDATE exams SET status = ? WHERE exam_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, examId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Exam exam) {
        String sql = "UPDATE exams SET title=?, description=?, duration_minutes=?, total_marks=?, passing_marks=?, status=?, start_time=?, end_time=? WHERE exam_id=?";
        try (Connection conn = DatabaseUtil.getConnection();
PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, exam.getTitle());
            stmt.setString(2, exam.getDescription());
            stmt.setInt(3, exam.getDurationMinutes());
            stmt.setInt(4, exam.getTotalMarks());
            stmt.setInt(5, exam.getPassingMarks());
            stmt.setString(6, exam.getStatus().name());
            stmt.setObject(7, exam.getStartTime());
            stmt.setObject(8, exam.getEndTime());
            stmt.setInt(9, exam.getExamId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Exam mapResultSet(ResultSet rs) throws SQLException {
        Exam e = new Exam();
        e.setExamId(rs.getInt("exam_id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setDurationMinutes(rs.getInt("duration_minutes"));
        e.setTotalMarks(rs.getInt("total_marks"));
        e.setPassingMarks(rs.getInt("passing_marks"));
        e.setCreatedBy(rs.getInt("created_by"));
        e.setStatus(ExamStatus.valueOf(rs.getString("status")));
        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) e.setStartTime(st.toLocalDateTime());
        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) e.setEndTime(et.toLocalDateTime());
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) e.setCreatedAt(ct.toLocalDateTime());
        return e;
    }


public List<Exam> findAll() {
    List<Exam> list = new ArrayList<>();
    String sql = "SELECT exam_id, title, description, duration_minutes, total_marks, passing_marks, created_by, status, start_time, end_time, created_at " +
            "FROM exams ORDER BY created_at DESC";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) list.add(mapResultSet(rs));
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}


public void delete(int examId) {
    String sql = "DELETE FROM exams WHERE exam_id = ?";
    try (Connection conn = DatabaseUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, examId);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public void toggleActive(int examId, boolean active) {
    updateStatus(examId, active ? ExamStatus.ACTIVE : ExamStatus.DRAFT);
}


}


