package com.example.onlineexam.controller;

import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.model.Question;
import com.example.onlineexam.util.SceneUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TeacherQuestionListController {

    @FXML private Label headerLabel;

    @FXML private TableView<Question> table;
    @FXML private TableColumn<Question, Integer> colOrder;
    @FXML private TableColumn<Question, String> colText;
    @FXML private TableColumn<Question, Character> colCorrect;
    @FXML private TableColumn<Question, Integer> colMarks;
    @FXML private TableColumn<Question, Void> colActions;

    private final QuestionDao questionDao = new QuestionDao();
    private int examId;

    @FXML
    public void initialize() {
        examId = TeacherNavState.getExamId();

        colOrder.setCellValueFactory(new PropertyValueFactory<>("questionOrder"));
        colText.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colCorrect.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));
        colMarks.setCellValueFactory(new PropertyValueFactory<>("marks"));

        addActions();
        load();
    }

    private void load() {
        List<Question> qs = questionDao.findByExamId(examId);
        table.setItems(FXCollections.observableArrayList(qs));
        headerLabel.setText("Questions for Exam ID: " + examId);
    }

    private void addActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, delBtn);

            {
                editBtn.setOnAction(e -> edit(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> del(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void edit(Question q) {
        Optional<Question> updated = TeacherQuestionFormController.openDialog(q);
        updated.ifPresent(x -> {
            x.setQuestionId(q.getQuestionId());
            x.setExamId(examId);
            x.setQuestionOrder(q.getQuestionOrder());
            questionDao.updateQuestion(x);
            load();
        });
    }

    private void del(Question q) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete this question?");
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            questionDao.deleteQuestion(q.getQuestionId());
            load();
        }
    }

    @FXML
    private void addQuestion() {
        Optional<Question> created = TeacherQuestionFormController.openDialog(null);
        created.ifPresent(q -> {
            q.setExamId(examId);
            q.setQuestionOrder(table.getItems().size()); // your system uses 0-based in createExamController
            questionDao.addQuestion(q);
            load();
        });
    }

    @FXML
    private void back() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/exam-list.fxml", "Manage Exams");
    }
}
