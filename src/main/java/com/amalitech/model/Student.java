
package com.amalitech.model;

import com.amalitech.interfaces.Gradable;
import com.amalitech.calculation.GradingStrategy;
import com.amalitech.calculation.SimpleAverageStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Abstract base class representing a student with personal details and academic records.
public abstract class Student implements Gradable {
    private final String studentId;
    private final String name;
    private final int age;
    private final String email;

    //Dynamic list of grades */
    protected final List<Double> grades = new ArrayList<>();

    private final Set<String> enrolledSubjectCodes = new HashSet<>();
    private static int studentCounter = 0;

    //Pluggable grading strategy (OCP).
    private GradingStrategy gradingStrategy = new SimpleAverageStrategy();

    public Student(String name, int age, String email) {
        this.studentId = String.format("STU%03d", ++studentCounter);
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public abstract String getStudentType();
    public abstract double getPassingGrade();

    @Override
    public boolean recordGrade(double grade) {
        if (validateGrade(grade)) {
            grades.add(grade);
            return true;
        }
        return false;
    }

    @Override
    public boolean validateGrade(double grade) {
        return grade >= 0 && grade <= 100;
    }

    //Delegates to strategy
    public double calculateAverageGrade() {
        return gradingStrategy.computeAverage(grades);
    }

    //Delegates to strategy
    public boolean isPassing() {
        return gradingStrategy.isPassing(grades, getPassingGrade());
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

    //Provide a safe way to swap grading behavior at runtime (OCP/DIP).
    public void setGradingStrategy(GradingStrategy strategy) {
        if (strategy != null) this.gradingStrategy = strategy;
    }

    //Read-only copy if calculators need raw grades
    public List<Double> getGrades() {
        return new ArrayList<>(grades);
    }
}
