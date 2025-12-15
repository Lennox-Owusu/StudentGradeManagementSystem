package com.amalitech;


import java.util.HashSet;
import java.util.Set;

//Abstract base class representing a student with personal details and academic records.
public abstract class Student implements Gradable {
    private final String studentId;
    private final String name;
    private final int age;
    private final String email;

    protected double[] grades;
    protected int gradeCount;

    private final Set<String> enrolledSubjectCodes = new HashSet<>();
    private static int studentCounter = 0;

    //Constructs a student with personal details and initializes grade storage.
    public Student(String name, int age, String email) {
        this.studentId = String.format("STU%03d", ++studentCounter);
        this.name = name;
        this.age = age;
        this.email = email;
        this.grades = new double[50];
        this.gradeCount = 0;
    }

    public abstract String getStudentType();
    public abstract double getPassingGrade();

    // Implement Gradable interface
    @Override
    public boolean recordGrade(double grade) {
        if (validateGrade(grade)) {
            if (gradeCount < grades.length) {
                grades[gradeCount++] = grade;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validateGrade(double grade) {
        return grade >= 0 && grade <= 100;
    }

    public double calculateAverageGrade() {
        if (gradeCount == 0) return 0.0;
        double sum = 0;
        for (int i = 0; i < gradeCount; i++) sum += grades[i];
        return sum / gradeCount;
    }

    public boolean isPassing() {
        return calculateAverageGrade() >= getPassingGrade();
    }

    public void enrollSubject(Subject subject) {
        if (subject != null) {
            enrolledSubjectCodes.add(subject.getSubjectCode());
        }
    }

    public int getEnrolledSubjectCount() {
        return enrolledSubjectCodes.size();
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }


    public boolean recordGrade(CoreSubject math, double v) {
        return false;
    }

    public boolean passed(CoreSubject sci) {
        return false;
    }
}

