package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailableExamsController {

    @FXML private TableView<ExamRow> examTable;
    @FXML private TableColumn<ExamRow, String> titleCol;
    @FXML private TableColumn<ExamRow, Number> durationCol;
    @FXML private TableColumn<ExamRow, Number> marksCol;
    @FXML private TableColumn<ExamRow, String> actionCol;
    @FXML private Label noExamsLabel;

    private final ExamDao examDao = new ExamDao();
    private final QuestionDao questionDao = new QuestionDao();
    private Map<Integer, Exam> examMap = new HashMap<>();

    public static class ExamRow {
        private final SimpleStringProperty title;
        private final SimpleIntegerProperty duration;
        private final SimpleIntegerProperty marks;
        private final int examId;

        public ExamRow(int examId, String title, int duration, int marks) {
            this.examId = examId;
            this.title = new SimpleStringProperty(title);
            this.duration = new SimpleIntegerProperty(duration);
            this.marks = new SimpleIntegerProperty(marks);
        }
        public String getTitle() { return title.get(); }
        public int getDuration() { return duration.get(); }
        public int getMarks() { return marks.get(); }
        public int getExamId() { return examId; }
    }

    @FXML
    public void initialize() {
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        durationCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDuration()));
        marksCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMarks()));
        actionCol.setCellValueFactory(c -> new SimpleStringProperty("Start"));
        actionCol.setCellFactory(col -> {
            TableCell<ExamRow, String> cell = new TableCell<>();
            cell.setGraphic(null);
            cell.itemProperty().addListener((obs, old, item) -> {
                if (item != null) {
                    Button btn = new Button("Start Exam");
                    btn.setOnAction(e -> {
                        ExamRow row = cell.getTableRow().getItem();
                        if (row != null) startExam(row.getExamId());
                    });
                    cell.setGraphic(btn);
                }
            });
            return cell;
        });

        loadExams();
    }

    private void loadExams() {
        int studentId = SessionManager.getCurrentStudent().getStudentId();
        List<Exam> exams = examDao.findAvailableForStudent(studentId);
        examMap.clear();
        var rows = new java.util.ArrayList<ExamRow>();
        for (Exam ex : exams) {
            examMap.put(ex.getExamId(), ex);
            int qCount = questionDao.findByExamId(ex.getExamId()).size();
            rows.add(new ExamRow(ex.getExamId(), ex.getTitle(), ex.getDurationMinutes(), ex.getTotalMarks()));
        }
        examTable.setItems(FXCollections.observableArrayList(rows));
        noExamsLabel.setVisible(rows.isEmpty());
    }

    private void startExam(int examId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/exam-instructions.fxml"));
            Parent root = loader.load();
            ExamInstructionsController ctrl = loader.getController();
            ctrl.setExam(examMap.get(examId));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Exam Instructions");
            stage.showAndWait();
            loadExams(); // refresh after exam
        } catch (IOException e) {
            e.printStackTrace();
        }
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
