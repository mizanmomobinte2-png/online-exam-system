package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.StudentDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.model.Student;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TeacherResultsController {

    @FXML private ComboBox<Exam> examComboBox;
    @FXML private TableView<ResultRow> resultsTable;
    @FXML private TableColumn<ResultRow, String> studentNameCol;
    @FXML private TableColumn<ResultRow, Number> scoreCol;
    @FXML private TableColumn<ResultRow, Number> totalCol;
    @FXML private TableColumn<ResultRow, Number> percentCol;
    @FXML private TableColumn<ResultRow, String> statusCol;
    @FXML private TableColumn<ResultRow, String> submittedAtCol;
    @FXML private Label summaryLabel;
    @FXML private Label noDataLabel;

    private final ExamDao examDao = new ExamDao();
    private final ExamAttemptDao attemptDao = new ExamAttemptDao();
    private final StudentDao studentDao = new StudentDao();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public static class ResultRow {
        private final SimpleStringProperty studentName;
        private final SimpleIntegerProperty score;
        private final SimpleIntegerProperty total;
        private final SimpleDoubleProperty percent;
        private final SimpleStringProperty status;
        private final SimpleStringProperty submittedAt;

        public ResultRow(String studentName, int score, int total, double percent, String status, String submittedAt) {
            this.studentName = new SimpleStringProperty(studentName);
            this.score = new SimpleIntegerProperty(score);
            this.total = new SimpleIntegerProperty(total);
            this.percent = new SimpleDoubleProperty(percent);
            this.status = new SimpleStringProperty(status);
            this.submittedAt = new SimpleStringProperty(submittedAt);
        }

        public String getStudentName() { return studentName.get(); }
        public int getScore() { return score.get(); }
        public int getTotal() { return total.get(); }
        public double getPercent() { return percent.get(); }
        public String getStatus() { return status.get(); }
        public String getSubmittedAt() { return submittedAt.get(); }
    }

    @FXML
    public void initialize() {
        setupTable();
        loadTeacherExams();

        examComboBox.setOnAction(e -> loadResultsForSelectedExam());
    }

    private void setupTable() {
        studentNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        scoreCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getScore()));
        totalCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTotal()));
        percentCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPercent()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        submittedAtCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubmittedAt()));

        percentCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item.doubleValue()));
                }
            }
        });

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PASS".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadTeacherExams() {
        if (SessionManager.getCurrentTeacher() == null) {
            summaryLabel.setText("Teacher session not found.");
            return;
        }

        int teacherId = SessionManager.getCurrentTeacher().getTeacherId();
        List<Exam> exams = examDao.findByTeacherId(teacherId);

        examComboBox.setItems(FXCollections.observableArrayList(exams));

        if (!exams.isEmpty()) {
            examComboBox.getSelectionModel().selectFirst();
            loadResultsForSelectedExam();
        } else {
            summaryLabel.setText("No exams found.");
            noDataLabel.setVisible(true);
            noDataLabel.setText("No exams created yet.");
        }
    }

    private void loadResultsForSelectedExam() {
        Exam selectedExam = examComboBox.getValue();

        if (selectedExam == null) {
            resultsTable.setItems(FXCollections.observableArrayList());
            summaryLabel.setText("Select an exam.");
            noDataLabel.setVisible(true);
            noDataLabel.setText("No exam selected.");
            return;
        }

        List<ExamAttempt> attempts = attemptDao.findByExamId(selectedExam.getExamId());
        List<ResultRow> rows = new ArrayList<>();

        int passCount = 0;
        double avg = 0;

        for (ExamAttempt attempt : attempts) {
            Student student = studentDao.findById(attempt.getStudentId());

            String studentName = (student != null) ? student.getFullName() : ("Student ID " + attempt.getStudentId());
            int score = attempt.getScore();
            int total = attempt.getTotalMarks();
            double percent = total > 0 ? (100.0 * score / total) : 0.0;

            String status = score >= selectedExam.getPassingMarks() ? "PASS" : "FAIL";
            if ("PASS".equals(status)) {
                passCount++;
            }

            avg += percent;

            String submittedAt = attempt.getEndTime() != null
                    ? attempt.getEndTime().format(formatter)
                    : "Not submitted";

            rows.add(new ResultRow(studentName, score, total, percent, status, submittedAt));
        }

        resultsTable.setItems(FXCollections.observableArrayList(rows));

        if (rows.isEmpty()) {
            noDataLabel.setVisible(true);
            noDataLabel.setText("No submitted attempts for this exam.");
            summaryLabel.setText("Exam: " + selectedExam.getTitle() + " | Attempts: 0");
        } else {
            noDataLabel.setVisible(false);
            avg /= rows.size();
            summaryLabel.setText(
                    "Exam: " + selectedExam.getTitle() +
                    " | Attempts: " + rows.size() +
                    " | Passed: " + passCount +
                    " | Average: " + String.format("%.1f%%", avg)
            );
        }
    }

    @FXML
    private void goDashboard() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-dashboard.fxml", "Teacher Dashboard");
    }

    @FXML
    private void goCreateExam() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/create-exam.fxml", "Create Exam");
    }

    @FXML
    private void goMyExams() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-exam-list.fxml", "My Exams");
    }

    @FXML
    private void goResults() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-results.fxml", "Results & Reports");
    }

    @FXML
    private void logout() throws IOException {
        SessionManager.logout();
        SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
    }
}