package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.dao.StudentAnswerDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.model.Question;
import com.example.onlineexam.model.StudentAnswer;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewScreenController {

    @FXML private Label examTitleLabel;
    @FXML private Label summaryLabel;
    @FXML private TableView<ReviewRow> reviewTable;
    @FXML private TableColumn<ReviewRow, Number> noCol;
    @FXML private TableColumn<ReviewRow, String> questionCol;
    @FXML private TableColumn<ReviewRow, String> yourAnswerCol;
    @FXML private TableColumn<ReviewRow, String> correctAnswerCol;
    @FXML private TableColumn<ReviewRow, String> resultCol;
    @FXML private TableColumn<ReviewRow, Number> marksCol;

    private final ExamAttemptDao attemptDao = new ExamAttemptDao();
    private final ExamDao examDao = new ExamDao();
    private final QuestionDao questionDao = new QuestionDao();
    private final StudentAnswerDao answerDao = new StudentAnswerDao();

    public static class ReviewRow {
        private final SimpleIntegerProperty no;
        private final SimpleStringProperty question;
        private final SimpleStringProperty yourAnswer;
        private final SimpleStringProperty correctAnswer;
        private final SimpleStringProperty result;
        private final SimpleIntegerProperty marks;

        public ReviewRow(int no, String question, String yourAnswer, String correctAnswer, String result, int marks) {
            this.no = new SimpleIntegerProperty(no);
            this.question = new SimpleStringProperty(question);
            this.yourAnswer = new SimpleStringProperty(yourAnswer);
            this.correctAnswer = new SimpleStringProperty(correctAnswer);
            this.result = new SimpleStringProperty(result);
            this.marks = new SimpleIntegerProperty(marks);
        }

        public int getNo() { return no.get(); }
        public String getQuestion() { return question.get(); }
        public String getYourAnswer() { return yourAnswer.get(); }
        public String getCorrectAnswer() { return correctAnswer.get(); }
        public String getResult() { return result.get(); }
        public int getMarks() { return marks.get(); }
    }

    public static void open(int attemptId) {
        try {
            FXMLLoader loader = new FXMLLoader(ReviewScreenController.class.getResource("/fxml/student/review-screen.fxml"));
            Parent root = loader.load();

            ReviewScreenController controller = loader.getController();
            controller.loadData(attemptId);

            Stage stage = new Stage();
            stage.setTitle("Review Answers");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        noCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNo()));
        questionCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQuestion()));
        yourAnswerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYourAnswer()));
        correctAnswerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorrectAnswer()));
        resultCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getResult()));
        marksCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMarks()));

        resultCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Correct".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    public void loadData(int attemptId) {
        ExamAttempt attempt = attemptDao.findById(attemptId);
        if (attempt == null) {
            examTitleLabel.setText("Review Answers");
            summaryLabel.setText("Attempt not found.");
            return;
        }

        Exam exam = examDao.findById(attempt.getExamId());
        if (exam != null) {
            examTitleLabel.setText("Review: " + exam.getTitle());
        } else {
            examTitleLabel.setText("Review Answers");
        }

        List<Question> questions = questionDao.findByExamId(attempt.getExamId());
        List<StudentAnswer> answers = answerDao.findByAttemptId(attemptId);

        Map<Integer, StudentAnswer> answerMap = new HashMap<>();
        for (StudentAnswer sa : answers) {
            answerMap.put(sa.getQuestionId(), sa);
        }

        var rows = FXCollections.<ReviewRow>observableArrayList();

        int totalScore = 0;
        int totalPossible = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            StudentAnswer sa = answerMap.get(q.getQuestionId());

            String yourAnswer = "Not Answered";
            String correctAnswer = String.valueOf(q.getCorrectAnswer());
            String result = "Wrong";
            int marks = 0;

            if (sa != null) {
                if (sa.getSelectedAnswer() != null) {
                    yourAnswer = String.valueOf(sa.getSelectedAnswer());
                }
                result = sa.isCorrect() ? "Correct" : "Wrong";
                marks = sa.getMarksObtained();
            }

            totalScore += marks;
            totalPossible += q.getMarks();

            rows.add(new ReviewRow(
                    i + 1,
                    q.getQuestionText(),
                    yourAnswer,
                    correctAnswer,
                    result,
                    marks
            ));
        }

        reviewTable.setItems(rows);
        summaryLabel.setText("Score: " + totalScore + " / " + totalPossible + "   |   Questions: " + questions.size());
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) reviewTable.getScene().getWindow();
        stage.close();
    }
}