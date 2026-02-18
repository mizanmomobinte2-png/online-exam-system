package com.example.onlineexam.service;

import com.example.onlineexam.dao.StudentDao;
import com.example.onlineexam.dao.TeacherDao;
import com.example.onlineexam.dao.UserDao;
import com.example.onlineexam.model.Student;
import com.example.onlineexam.model.Teacher;
import com.example.onlineexam.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private final StudentDao studentDao = new StudentDao();
    private final TeacherDao teacherDao = new TeacherDao();

    public User login(String usernameOrEmail, String password) {
        User user = userDao.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userDao.findByEmail(usernameOrEmail);
        }
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public boolean registerStudent(String username, String email, String password, String fullName, String enrollmentNo, String contact) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole(User.UserRole.STUDENT);
        int userId = userDao.create(user);
        if (userId <= 0) return false;
        Student student = new Student();
        student.setUserId(userId);
        student.setFullName(fullName);
        student.setEnrollmentNo(enrollmentNo);
        student.setContact(contact);
        return studentDao.create(student) > 0;
    }

    public boolean registerTeacher(String username, String email, String password, String fullName, String department, String contact) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole(User.UserRole.TEACHER);
        int userId = userDao.create(user);
        if (userId <= 0) return false;
        Teacher teacher = new Teacher();
        teacher.setUserId(userId);
        teacher.setFullName(fullName);
        teacher.setDepartment(department);
        teacher.setContact(contact);
        return teacherDao.create(teacher) > 0;
    }

    public Student getStudentByUserId(int userId) {
        return studentDao.findByUserId(userId);
    }

    public Teacher getTeacherByUserId(int userId) {
        return teacherDao.findByUserId(userId);
    }
}
