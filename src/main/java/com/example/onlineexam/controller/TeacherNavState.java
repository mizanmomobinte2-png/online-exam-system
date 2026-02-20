package com.example.onlineexam.controller;

public class TeacherNavState {
    private static int examId = -1;
    public static void setExamId(int id) { examId = id; }
    public static int getExamId() { return examId; }
}

