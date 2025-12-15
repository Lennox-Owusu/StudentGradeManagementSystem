package com.amalitech;


//Manages a collection of Grade objects and provides methods to calculate averages.
public class GradeManager {
    private final Grade[] grades;
    private int gradeCount;

    //Initializes the manager with a fixed-size array.
    public GradeManager(int size) {
        grades = new Grade[size];
        gradeCount = 0;
    }


    //Adds a grade to the collection if space is available.
    public void addGrade(Grade grade) {
        if (gradeCount < grades.length) {
            grades[gradeCount++] = grade;
        }
    }

    public Grade getGradeAt(int index) {
        if (index < 0 || index >= gradeCount) return null;
        return grades[index];
    }



    //Calculates the average of core subject grades for a student.
    public double calculateCoreAverage(String studentId) {
        double sum = 0; int count = 0;
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId().equalsIgnoreCase(studentId) &&
                    grades[i].getSubject().getSubjectType().equals("Core")) {
                sum += grades[i].getGrade();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    //similar for other methods
    public double calculateElectiveAverage(String studentId) {
        double sum = 0; int count = 0;
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId().equalsIgnoreCase(studentId) &&
                    grades[i].getSubject().getSubjectType().equals("Elective")) {
                sum += grades[i].getGrade();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    public double calculateOverallAverage(String studentId) {
        double sum = 0; int count = 0;
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId().equalsIgnoreCase(studentId)) {
                sum += grades[i].getGrade();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    public int getGradeCount() {
        return gradeCount;
    }
}

