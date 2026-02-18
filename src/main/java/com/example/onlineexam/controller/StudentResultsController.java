package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentResultsController {

    @FXML private TableView<ResultRow> resultsTable;
    @FXML private TableColumn<ResultRow, String> examNameCol;
    @FXML private TableColumn<ResultRow, Number> scoreCol;
    @FXML private TableColumn<ResultRow, Number> totalCol;
    @FXML private TableColumn<ResultRow, Number> percentCol;
    @FXML private TableColumn<ResultRow, String> dateCol;
    @FXML private Label noResultsLabel;

    private final ExamAttemptDao attemptDao = new ExamAttemptDao();
    private final ExamDao examDao = new ExamDao();

    public static class ResultRow {
        private final SimpleStringProperty examName = new SimpleStringProperty();
        private final SimpleIntegerProperty score = new SimpleIntegerProperty();
        private final SimpleIntegerProperty total = new SimpleIntegerProperty();
        private final SimpleDoubleProperty percent = new SimpleDoubleProperty();
        private final SimpleStringProperty date = new SimpleStringProperty();

        public ResultRow(String examName, int score, int total, String date) {
            this.examName.set(examName);
            this.score.set(score);
            this.total.set(total);
            this.percent.set(total > 0 ? 100.0 * score / total : 0);
            this.date.set(date);
        }
    }

    @FXML
    public void initialize() {
        examNameCol.setCellValueFactory(c -> c.getValue().examName);
        scoreCol.setCellValueFactory(c -> c.getValue().score);
        totalCol.setCellValueFactory(c -> c.getValue().total);
        percentCol.setCellValueFactory(c -> c.getValue().percent);
        dateCol.setCellValueFactory(c -> c.getValue().date);

        int studentId = SessionManager.getCurrentStudent().getStudentId();
        List<ExamAttempt> attempts = attemptDao.findByStudentId(studentId);
        List<ResultRow> rows = new ArrayList<>();
        for (ExamAttempt a : attempts) {
            Exam ex = examDao.findById(a.getExamId());
            String name = ex != null ? ex.getTitle() : "Unknown";
            String date = a.getEndTime() != null ? a.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
            rows.add(new ResultRow(name, a.getScore(), a.getTotalMarks(), date));
        }
        resultsTable.setItems(FXCollections.observableArrayList(rows));
        noResultsLabel.setVisible(rows.isEmpty());
    }

    @FXML
    private void goToDashboard() throws IOException {
        SceneUtil.loadFXML("/fxml/student/student-dashboard.fxml", "Student Dashboard");
    }

    @FXML
    private void goToAvailableExams() throws IOException {
        SceneUtil.loadFXML("/fxml/student/available-exams.fxml", "Available Exams");
    }

    @FXML
    private void goToResults() throws IOException {
        SceneUtil.loadFXML("/fxml/student/student-results.fxml", "My Results");
    }

    @FXML
    private void logout() throws IOException {
        SessionManager.logout();
        SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
    }
}
