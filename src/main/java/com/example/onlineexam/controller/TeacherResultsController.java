package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.StudentDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.model.Student;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.ExcelUtil;
import com.example.onlineexam.util.SceneUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherResultsController implements Initializable {

    @FXML private ComboBox<ExamItem> examCombo;
    @FXML private TableView<ResultRow> resultsTable;
    @FXML private TableColumn<ResultRow, String> studentCol;
    @FXML private TableColumn<ResultRow, String> enrollmentCol;
    @FXML private TableColumn<ResultRow, Number> scoreCol;
    @FXML private TableColumn<ResultRow, Number> totalCol;
    @FXML private TableColumn<ResultRow, Number> percentCol;
    @FXML private Label statsLabel;

    private final ExamDao examDao = new ExamDao();
    private final ExamAttemptDao attemptDao = new ExamAttemptDao();
    private final StudentDao studentDao = new StudentDao();

    public static class ExamItem {
        private final int examId;
        private final String title;
        public ExamItem(int examId, String title) { this.examId = examId; this.title = title; }
        public int getExamId() { return examId; }
        @Override public String toString() { return title; }
    }

    public static class ResultRow {
        public String studentName;
        public String enrollment;
        public int score;
        public int total;
        public double percent;
        public ResultRow(String n, String e, int s, int t) {
            studentName = n; enrollment = e; score = s; total = t;
            percent = t > 0 ? 100.0 * s / t : 0;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().studentName));
        enrollmentCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().enrollment));
        scoreCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().score));
        totalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().total));
        percentCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().percent));

        int teacherId = SessionManager.getCurrentTeacher().getTeacherId();
        List<Exam> exams = examDao.findByTeacherId(teacherId);
        List<ExamItem> items = new ArrayList<>();
        for (Exam ex : exams) {
            items.add(new ExamItem(ex.getExamId(), ex.getTitle()));
        }
        examCombo.setItems(FXCollections.observableArrayList(items));
        if (!items.isEmpty()) examCombo.setValue(items.get(0));
        onExamSelected();
    }

    @FXML
    private void onExamSelected() {
        ExamItem sel = examCombo.getValue();
        if (sel == null) return;
        List<ExamAttempt> attempts = attemptDao.findByExamId(sel.getExamId());
        List<ResultRow> rows = new ArrayList<>();
        for (ExamAttempt a : attempts) {
            Student s = studentDao.findByUserId(0); // we need student by student_id
            // StudentDao has findByUserId but we have student_id from attempt
            Student st = getStudentById(a.getStudentId());
            String name = st != null ? st.getFullName() : "Unknown";
            String enrol = st != null ? st.getEnrollmentNo() : "";
            rows.add(new ResultRow(name, enrol, a.getScore(), a.getTotalMarks()));
        }
        resultsTable.setItems(FXCollections.observableArrayList(rows));

        if (rows.isEmpty()) {
            statsLabel.setText("No attempts yet.");
        } else {
            double avg = rows.stream().mapToDouble(r -> r.percent).average().orElse(0);
            int max = rows.stream().mapToInt(r -> r.score).max().orElse(0);
            int min = rows.stream().mapToInt(r -> r.score).min().orElse(0);
            long passed = rows.stream().filter(r -> r.percent >= 40).count();
            statsLabel.setText(String.format("Average: %.1f%% | Highest: %d | Lowest: %d | Passed: %d/%d",
                avg, max, min, passed, rows.size()));
        }
    }

    private Student getStudentById(int studentId) {
        return studentDao.findById(studentId);
    }

    @FXML
    private void exportToExcel() {
        ExamItem sel = examCombo.getValue();
        if (sel == null) return;
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("results_" + sel.getExamId() + ".xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File f = fc.showSaveDialog(resultsTable.getScene().getWindow());
        if (f != null) {
            try {
                List<String[]> rows = new ArrayList<>();
                for (ResultRow r : resultsTable.getItems()) {
                    rows.add(new String[]{r.studentName, r.enrollment, String.valueOf(r.score), String.valueOf(r.total), String.format("%.1f", r.percent)});
                }
                ExcelUtil.exportResults(f.getAbsolutePath(), rows, new String[]{"Student", "Enrollment", "Score", "Total", "Percent"});
                new Alert(Alert.AlertType.INFORMATION, "Exported.").showAndWait();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage()).showAndWait();
            }
        }
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
