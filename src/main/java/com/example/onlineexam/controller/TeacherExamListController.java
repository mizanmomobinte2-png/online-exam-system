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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TeacherExamListController {

    @FXML private TableView<ExamRow> examTable;
    @FXML private TableColumn<ExamRow, String> titleCol;
    @FXML private TableColumn<ExamRow, Number> durationCol;
    @FXML private TableColumn<ExamRow, Number> totalCol;
    @FXML private TableColumn<ExamRow, Number> passingCol;
    @FXML private TableColumn<ExamRow, Number> questionCountCol;
    @FXML private TableColumn<ExamRow, String> statusCol;
    @FXML private TableColumn<ExamRow, String> createdAtCol;
    @FXML private TableColumn<ExamRow, Void> actionCol;

    @FXML private Label summaryLabel;
    @FXML private Label noDataLabel;

    private final ExamDao examDao = new ExamDao();
    private final QuestionDao questionDao = new QuestionDao();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public static class ExamRow {
        private final Exam exam;
        private final SimpleStringProperty title;
        private final SimpleIntegerProperty duration;
        private final SimpleIntegerProperty totalMarks;
        private final SimpleIntegerProperty passingMarks;
        private final SimpleIntegerProperty questionCount;
        private final SimpleStringProperty status;
        private final SimpleStringProperty createdAt;

        public ExamRow(Exam exam, int questionCount, String createdAt) {
            this.exam = exam;
            this.title = new SimpleStringProperty(exam.getTitle());
            this.duration = new SimpleIntegerProperty(exam.getDurationMinutes());
            this.totalMarks = new SimpleIntegerProperty(exam.getTotalMarks());
            this.passingMarks = new SimpleIntegerProperty(exam.getPassingMarks());
            this.questionCount = new SimpleIntegerProperty(questionCount);
            this.status = new SimpleStringProperty(exam.getStatus().name());
            this.createdAt = new SimpleStringProperty(createdAt);
        }

        public Exam getExam() { return exam; }
        public String getTitle() { return title.get(); }
        public int getDuration() { return duration.get(); }
        public int getTotalMarks() { return totalMarks.get(); }
        public int getPassingMarks() { return passingMarks.get(); }
        public int getQuestionCount() { return questionCount.get(); }
        public String getStatus() { return status.get(); }
        public String getCreatedAt() { return createdAt.get(); }
    }

    @FXML
    public void initialize() {
        setupTable();
        loadMyExams();
    }

    private void setupTable() {
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        durationCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDuration()));
        totalCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTotalMarks()));
        passingCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPassingMarks()));
        questionCountCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuestionCount()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        createdAtCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt()));

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIVE".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    }
                }
            }
        });

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button manageBtn = new Button("Questions");
            private final Button toggleBtn = new Button();
            private final Button resultsBtn = new Button("Results");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(8, manageBtn, toggleBtn, resultsBtn, deleteBtn);

            {
                manageBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                resultsBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

                manageBtn.setOnAction(e -> {
                    ExamRow row = getTableView().getItems().get(getIndex());
                    openQuestionManager(row.getExam());
                });

                toggleBtn.setOnAction(e -> {
                    ExamRow row = getTableView().getItems().get(getIndex());
                    toggleStatus(row.getExam());
                });

                resultsBtn.setOnAction(e -> {
                    try {
                        SceneUtil.loadFXML("/fxml/teacher/teacher-results.fxml", "Results & Reports");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                deleteBtn.setOnAction(e -> {
                    ExamRow row = getTableView().getItems().get(getIndex());
                    deleteExam(row.getExam());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    ExamRow row = getTableView().getItems().get(getIndex());
                    if (row.getExam().getStatus().name().equalsIgnoreCase("ACTIVE")) {
                        toggleBtn.setText("Deactivate");
                        toggleBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        toggleBtn.setText("Activate");
                        toggleBtn.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                    setGraphic(box);
                }
            }
        });
    }

    private void loadMyExams() {
        if (SessionManager.getCurrentTeacher() == null) {
            summaryLabel.setText("Teacher session not found.");
            return;
        }

        int teacherId = SessionManager.getCurrentTeacher().getTeacherId();
        List<Exam> exams = examDao.findByTeacherId(teacherId);

        List<ExamRow> rows = new ArrayList<>();
        int totalQuestions = 0;

        for (Exam exam : exams) {
            int qCount = questionDao.findByExamId(exam.getExamId()).size();
            totalQuestions += qCount;

            String created = exam.getCreatedAt() != null
                    ? exam.getCreatedAt().format(formatter)
                    : "N/A";

            rows.add(new ExamRow(exam, qCount, created));
        }

        examTable.setItems(FXCollections.observableArrayList(rows));

        if (rows.isEmpty()) {
            noDataLabel.setVisible(true);
            noDataLabel.setText("No exams created yet.");
            summaryLabel.setText("Total Exams: 0");
        } else {
            noDataLabel.setVisible(false);
            summaryLabel.setText("Total Exams: " + rows.size() + "   |   Total Questions: " + totalQuestions);
        }
    }

    private void openQuestionManager(Exam exam) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/question-list.fxml"));
            Parent root = loader.load();

            TeacherQuestionListController controller = loader.getController();
            controller.setExam(exam);

            Stage stage = new Stage();
            stage.setTitle("Manage Questions");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadMyExams();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleStatus(Exam exam) {
        try {
            boolean activate = !exam.getStatus().name().equalsIgnoreCase("ACTIVE");
            examDao.toggleActive(exam.getExamId(), activate);
            loadMyExams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteExam(Exam exam) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Exam");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will delete the exam. Make sure you really want to remove it.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                questionDao.deleteByExamId(exam.getExamId());
                examDao.delete(exam.getExamId());
                loadMyExams();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void createNewExam() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/create-exam.fxml", "Create Exam");
    }

    @FXML
    private void refreshPage() {
        loadMyExams();
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