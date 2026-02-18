package com.example.onlineexam.service;

import com.example.onlineexam.model.Student;
import com.example.onlineexam.model.Teacher;
import com.example.onlineexam.model.User;

/**
 * Holds the currently logged-in user and role-specific data.
 */
public class SessionManager {
    private static User currentUser;
    private static Student currentStudent;
    private static Teacher currentTeacher;

    public static void setCurrentUser(User user, Student student, Teacher teacher) {
        currentUser = user;
        currentStudent = student;
        currentTeacher = teacher;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static Teacher getCurrentTeacher() {
        return currentTeacher;
    }

    public static void logout() {
        currentUser = null;
        currentStudent = null;
        currentTeacher = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
