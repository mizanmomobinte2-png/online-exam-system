package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.Exam.ExamStatus;
import com.example.onlineexam.util.SceneUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TeacherExamListController {

    @FXML private TableView<Exam> table;
    @FXML private TableColumn<Exam, String> colTitle;
    @FXML private TableColumn<Exam, Integer> colDuration;
    @FXML private TableColumn<Exam, Integer> colTotal;
    @FXML private TableColumn<Exam, Integer> colPass;
    @FXML private TableColumn<Exam, ExamStatus> colStatus;
    @FXML private TableColumn<Exam, Void> colActions;

    private final ExamDao examDao = new ExamDao();
    private final QuestionDao questionDao = new QuestionDao();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
        colPass.setCellValueFactory(new PropertyValueFactory<>("passingMarks"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActions();
        load();
    }

    private void load() {
        List<Exam> exams = examDao.findAll(); 
        table.setItems(FXCollections.observableArrayList(exams));
    }

    private void addActions() {
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button editBtn = new Button("Edit");
            private final Button qBtn = new Button("Questions");
            private final Button delBtn = new Button("Delete");
            private final Button toggleBtn = new Button("Toggle");

            {
                editBtn.setOnAction(e -> openEdit(getTableView().getItems().get(getIndex())));
                qBtn.setOnAction(e -> openQuestions(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> deleteExam(getTableView().getItems().get(getIndex())));
                toggleBtn.setOnAction(e -> toggle(getTableView().getItems().get(getIndex())));
            }

            private final HBox box = new HBox(6, editBtn, qBtn, delBtn, toggleBtn);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void openEdit(Exam exam) {
        TeacherNavState.setExamId(exam.getExamId());
        SceneUtil.loadFXML("/fxml/teacher/create-exam.fxml", "Edit Exam");
    }

    private void openQuestions(Exam exam) {
        TeacherNavState.setExamId(exam.getExamId());
        SceneUtil.loadFXML("/fxml/teacher/question-list.fxml", "Manage Questions");
    }

    private void deleteExam(Exam exam) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete exam: " + exam.getTitle());
        a.setContentText("This will delete all questions too.");
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            questionDao.deleteByExamId(exam.getExamId());
            examDao.delete(exam.getExamId());
            load();
        }
    }

    private void toggle(Exam exam) {
        boolean makeActive = exam.getStatus() != ExamStatus.ACTIVE;
        examDao.toggleActive(exam.getExamId(), makeActive);
        load();
    }

    @FXML
    private void createExam() throws IOException {
        TeacherNavState.setExamId(-1);
        SceneUtil.loadFXML("/fxml/teacher/create-exam.fxml", "Create Exam");
    }

    @FXML
    private void goDashboard() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-dashboard.fxml", "Teacher Dashboard");
    }
}
