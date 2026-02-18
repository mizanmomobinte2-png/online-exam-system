package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyExamsController {

    @FXML private TableView<ExamRow> examTable;
    @FXML private TableColumn<ExamRow, String> titleCol;
    @FXML private TableColumn<ExamRow, Number> durationCol;
    @FXML private TableColumn<ExamRow, String> statusCol;
    @FXML private TableColumn<ExamRow, String> actionsCol;

    private final ExamDao examDao = new ExamDao();
    private final ExamAttemptDao attemptDao = new ExamAttemptDao();
    private Map<Integer, Exam> examMap = new HashMap<>();

    public static class ExamRow {
        private final SimpleStringProperty title;
        private final SimpleIntegerProperty duration;
        private final SimpleStringProperty status;
        private final int examId;

        public ExamRow(int examId, String title, int duration, String status) {
            this.examId = examId;
            this.title = new SimpleStringProperty(title);
            this.duration = new SimpleIntegerProperty(duration);
            this.status = new SimpleStringProperty(status);
        }
        public String getTitle() { return title.get(); }
        public int getDuration() { return duration.get(); }
        public String getStatus() { return status.get(); }
        public int getExamId() { return examId; }
    }

    @FXML
    public void initialize() {
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        durationCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDuration()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        actionsCol.setCellFactory(col -> {
            TableCell<ExamRow, String> cell = new TableCell<>();
            cell.setGraphic(null);
            cell.itemProperty().addListener((obs, old, item) -> {
                if (cell.getTableRow().getItem() != null) {
                    HBox box = new HBox(5);
                    ExamRow row = cell.getTableRow().getItem();
                    Button activate = new Button("Activate");
                    activate.setOnAction(e -> toggleStatus(row.getExamId(), "ACTIVE"));
                    Button deactivate = new Button("Deactivate");
                    deactivate.setOnAction(e -> toggleStatus(row.getExamId(), "DRAFT"));
                    box.getChildren().addAll(activate, deactivate);
                    cell.setGraphic(box);
                }
            });
            return cell;
        });
        loadExams();
    }

    private void loadExams() {
        int teacherId = SessionManager.getCurrentTeacher().getTeacherId();
        List<Exam> exams = examDao.findByTeacherId(teacherId);
        examMap.clear();
        var rows = new java.util.ArrayList<ExamRow>();
        for (Exam ex : exams) {
            examMap.put(ex.getExamId(), ex);
            rows.add(new ExamRow(ex.getExamId(), ex.getTitle(), ex.getDurationMinutes(), ex.getStatus().name()));
        }
        examTable.setItems(FXCollections.observableArrayList(rows));
    }

    private void toggleStatus(int examId, String status) {
        Exam ex = examMap.get(examId);
        if (ex == null) return;
        ex.setStatus(Exam.ExamStatus.valueOf(status));
        examDao.updateStatus(examId, ex.getStatus());
        loadExams();
    }

    @FXML
    private void goToDashboard() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-dashboard.fxml", "Teacher Dashboard");
    }

    @FXML
    private void goToCreateExam() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/create-exam.fxml", "Create Exam");
    }

    @FXML
    private void goToMyExams() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/my-exams.fxml", "My Exams");
    }

    @FXML
    private void goToResults() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-results.fxml", "Results & Reports");
    }

    @FXML
    private void logout() throws IOException {
        SessionManager.logout();
        SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
    }
}
