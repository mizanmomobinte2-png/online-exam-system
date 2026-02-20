package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.Question;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.ExcelUtil;
import com.example.onlineexam.util.SceneUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.example.onlineexam.service.ExamService;

public class CreateExamController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField durationField;
    @FXML private TextField totalMarksField;
    @FXML private TextField passingMarksField;
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> qTextCol;
    @FXML private TableColumn<Question, String> correctCol;
    @FXML private TableColumn<Question, Number> marksCol;
    @FXML private TableColumn<Question, String> removeCol;
    @FXML private Label errorLabel;

    private final ObservableList<Question> questions = FXCollections.observableArrayList();
    private final ExamDao examDao = new ExamDao();
    private final QuestionDao questionDao = new QuestionDao();
    private int editingExamId = -1;
private final ExamService examService = new ExamService();


    @FXML
    public void initialize() {
        qTextCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getQuestionText() != null ? c.getValue().getQuestionText().substring(0, Math.min(50, c.getValue().getQuestionText().length())) + "..." : ""));
        correctCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getCorrectAnswer())));
        marksCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getMarks()));
        removeCol.setCellFactory(col -> {
            TableCell<Question, String> cell = new TableCell<>();
            Button btn = new Button("Remove");
            btn.setOnAction(e -> {
                Question q = cell.getTableRow().getItem();
                if (q != null) questions.remove(q);
            });
            cell.setGraphic(null);
            cell.itemProperty().addListener((obs, old, item) -> cell.setGraphic(cell.getTableRow().getItem() != null ? btn : null));
            return cell;
        });
        questionsTable.setItems(questions);
        editingExamId = TeacherNavState.getExamId();
if (editingExamId > 0) {
    loadForEdit(editingExamId);
}

        
    }

    @FXML
    private void addQuestion() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Question");
        dialog.setHeaderText("Enter question details");

        Dialog<String> d = new Dialog<>();
        d.setTitle("Add Question");
        d.setHeaderText("Manual Question Entry");

        // Simple dialog with text fields
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField qText = new TextField();
        qText.setPromptText("Question text");
        qText.setPrefWidth(400);
        TextField optA = new TextField();
        optA.setPromptText("Option A");
        TextField optB = new TextField();
        optB.setPromptText("Option B");
        TextField optC = new TextField();
        optC.setPromptText("Option C");
        TextField optD = new TextField();
        optD.setPromptText("Option D");
        ComboBox<String> correct = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
        correct.setValue("A");
        TextField marks = new TextField("1");
        marks.setPromptText("Marks");

        grid.add(new Label("Question:"), 0, 0);
        grid.add(qText, 1, 0);
        grid.add(new Label("Option A:"), 0, 1);
        grid.add(optA, 1, 1);
        grid.add(new Label("Option B:"), 0, 2);
        grid.add(optB, 1, 2);
        grid.add(new Label("Option C:"), 0, 3);
        grid.add(optC, 1, 3);
        grid.add(new Label("Option D:"), 0, 4);
        grid.add(optD, 1, 4);
        grid.add(new Label("Correct:"), 0, 5);
        grid.add(correct, 1, 5);
        grid.add(new Label("Marks:"), 0, 6);
        grid.add(marks, 1, 6);

        d.getDialogPane().setContent(grid);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(b -> b == ButtonType.OK ? "ok" : null);
        if (d.showAndWait().orElse(null) != null) {
            try {
                int m = Integer.parseInt(marks.getText());
                Question q = new Question(qText.getText().trim(), optA.getText(), optB.getText(), optC.getText(), optD.getText(),
                    correct.getValue().charAt(0), m > 0 ? m : 1);
                questions.add(q);
            } catch (NumberFormatException ex) {
                showError("Invalid marks.");
            }
        }
    }

    @FXML
    private void importFromExcel() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File f = fc.showOpenDialog(questionsTable.getScene().getWindow());
        if (f != null) {
            try {
                List<Question> imported = ExcelUtil.importQuestions(f.getAbsolutePath());
                questions.addAll(imported);
                hideError();
            } catch (IOException e) {
                showError("Failed to import: " + e.getMessage());
            }
        }
    }

    @FXML
    private void downloadTemplate() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("question_template.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File f = fc.showSaveDialog(questionsTable.getScene().getWindow());
        if (f != null) {
            try {
                ExcelUtil.exportQuestionsTemplate(f.getAbsolutePath());
                new Alert(Alert.AlertType.INFORMATION, "Template saved.").showAndWait();
            } catch (IOException e) {
                showError("Failed: " + e.getMessage());
            }
        }
    }

  

    @FXML
    private void saveDraft() {
        saveOrPublish(false);
    }

    @FXML
    private void publishExam() {
        saveOrPublish(true);
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

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }


    private void loadForEdit(int examId) {
    Exam e = examService.getExamWithQuestions(examId);
    if (e == null) return;

    titleField.setText(e.getTitle());
    descriptionField.setText(e.getDescription());
    durationField.setText(String.valueOf(e.getDurationMinutes()));
    totalMarksField.setText(String.valueOf(e.getTotalMarks()));
    passingMarksField.setText(String.valueOf(e.getPassingMarks()));

    questions.clear();
    if (e.getQuestions() != null) questions.addAll(e.getQuestions());
}

private void saveOrPublish(boolean publish) {
    errorLabel.setVisible(false);

    String title = titleField.getText().trim();
    if (title.isEmpty()) { showError("Exam title is required."); return; }
    if (questions.isEmpty()) { showError("Add at least one question."); return; }

    int duration = 60, passing = 40;
    try {
        if (!durationField.getText().trim().isEmpty()) duration = Integer.parseInt(durationField.getText().trim());
        if (!passingMarksField.getText().trim().isEmpty()) passing = Integer.parseInt(passingMarksField.getText().trim());
    } catch (NumberFormatException e) {
        showError("Invalid numbers for duration/pass marks.");
        return;
    }

    int total = questions.stream().mapToInt(Question::getMarks).sum();
    totalMarksField.setText(String.valueOf(total));

    Exam exam = new Exam();
    exam.setTitle(title);
    exam.setDescription(descriptionField.getText().trim());
    exam.setDurationMinutes(duration);
    exam.setTotalMarks(total);
    exam.setPassingMarks(passing);
    exam.setCreatedBy(SessionManager.getCurrentTeacher().getTeacherId());
    exam.setStatus(publish ? Exam.ExamStatus.ACTIVE : Exam.ExamStatus.DRAFT);

    try {
        examService.validateExam(exam, questions);
    } catch (Exception ex) {
        showError(ex.getMessage());
        return;
    }

    if (editingExamId > 0) {
        
        exam.setExamId(editingExamId);
        examDao.update(exam);

        questionDao.deleteByExamId(editingExamId);
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            q.setExamId(editingExamId);
            q.setQuestionOrder(i);
            questionDao.addQuestion(q);
        }

        new Alert(Alert.AlertType.INFORMATION, "Exam updated successfully.").showAndWait();

    } else {
        
        int examId = examDao.create(exam);
        if (examId <= 0) { showError("Failed to create exam."); return; }

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            q.setExamId(examId);
            q.setQuestionOrder(i);
            questionDao.addQuestion(q);
        }

        new Alert(Alert.AlertType.INFORMATION, "Exam " + (publish ? "published" : "saved as draft") + " successfully.").showAndWait();
    }

    try {
        TeacherNavState.setExamId(-1);
        SceneUtil.loadFXML("/fxml/teacher/exam-list.fxml", "Manage Exams");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
