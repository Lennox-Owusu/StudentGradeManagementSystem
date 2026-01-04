package com.amalitech;


//Manages a collection of Student objects
public class StudentManager {
    private final Student[] students;
    private int studentCount;


    //Initializes the manager with a fixed-size array
    public StudentManager(int size) {
        students = new Student[size];
        studentCount = 0;
    }

    public void addStudent(Student student) {
        if (studentCount < students.length) {
            students[studentCount++] = student;
        } else {
            System.out.println("Student list is full.");
        }
    }

    public Student findStudent(String studentId) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getStudentId().equalsIgnoreCase(studentId)) {
                return students[i];
            }

        }
        return null;

    }

    public Student[] getStudents() {
        Student[] copy = new Student[studentCount];
        for (int i = 0; i < studentCount; i++) copy[i] = students[i];
        return copy;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public double getAverageClassGrade() {
        if (studentCount == 0) return 0.0;
        double sum = 0;
        for (int i = 0; i < studentCount; i++) sum += students[i].calculateAverageGrade();
        return sum / studentCount;


    }

}

