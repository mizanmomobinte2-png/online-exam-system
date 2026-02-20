package com.example.onlineexam.controller;

import com.example.onlineexam.model.Question;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class TeacherQuestionFormController {

    @FXML private Label titleLabel;
    @FXML private Label errorLabel;

    @FXML private TextArea questionText;
    @FXML private TextField optA;
    @FXML private TextField optB;
    @FXML private TextField optC;
    @FXML private TextField optD;
    @FXML private ComboBox<String> correct;
    @FXML private TextField marks;

    private Stage stage;
    private Question editing;
    private boolean saved = false;

    @FXML
    public void initialize() {
        correct.getItems().setAll("A", "B", "C", "D");
        correct.getSelectionModel().select("A");
        marks.setText("1");
    }

    public void setStage(Stage stage) { this.stage = stage; }

    public void setEditing(Question q) {
        this.editing = q;
        if (q == null) {
            titleLabel.setText("Add Question");
        } else {
            titleLabel.setText("Edit Question");
            questionText.setText(q.getQuestionText());
            optA.setText(q.getOptionA());
            optB.setText(q.getOptionB());
            optC.setText(q.getOptionC());
            optD.setText(q.getOptionD());
            correct.getSelectionModel().select(String.valueOf(q.getCorrectAnswer()));
            marks.setText(String.valueOf(q.getMarks()));
        }
    }

    @FXML
    private void cancel() {
        saved = false;
        stage.close();
    }

    @FXML
    private void save() {
        errorLabel.setText("");
        try {
            String qt = questionText.getText().trim();
            if (qt.isEmpty()) throw new IllegalArgumentException("Question required");

            String a = optA.getText().trim();
            String b = optB.getText().trim();
            String c = optC.getText().trim();
            String d = optD.getText().trim();
            if (a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty())
                throw new IllegalArgumentException("All options required");

            int m = Integer.parseInt(marks.getText().trim());
            if (m <= 0) throw new IllegalArgumentException("Marks must be > 0");

            if (editing == null) editing = new Question();

            editing.setQuestionText(qt);
            editing.setOptionA(a);
            editing.setOptionB(b);
            editing.setOptionC(c);
            editing.setOptionD(d);
            editing.setCorrectAnswer(correct.getValue().charAt(0));
            editing.setMarks(m);

            saved = true;
            stage.close();

        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
    public Question getQuestion() { return editing; }

    public static Optional<Question> openDialog(Question edit) {
        try {
            FXMLLoader loader = new FXMLLoader(TeacherQuestionFormController.class.getResource("/fxml/teacher/question-form.fxml"));
            Parent root = loader.load();
            TeacherQuestionFormController c = loader.getController();

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle(edit == null ? "Add Question" : "Edit Question");
            st.setScene(new Scene(root));

            c.setStage(st);
            c.setEditing(edit);

            st.showAndWait();
            return c.isSaved() ? Optional.of(c.getQuestion()) : Optional.empty();

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
