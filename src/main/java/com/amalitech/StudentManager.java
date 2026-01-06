
package com.amalitech;

import java.util.HashMap;
import java.util.Map;

public class StudentManager {
    private final Student[] students;
    private int studentCount;

    // NEW: O(1) lookup index (case-insensitive via uppercase normalization)
    private final Map<String, Student> index;

    // Initializes the manager with a fixed-size array and HashMap index
    public StudentManager(int size) {
        students = new Student[size];
        studentCount = 0;
        index = new HashMap<>(Math.max(32, size)); // pre-size to reduce rehashing
    }


     //Adds a student if capacity is available and ID is not a duplicate.
    public void addStudent(Student student) {
        if (student == null) {
            System.out.println("Cannot add null student.");
            return;
        }
        // Normalize ID to uppercase for case-insensitive behavior
        String id = student.getStudentId();
        String key = (id == null) ? "" : id.toUpperCase();

        // Prevent duplicate IDs (consistent system-wide identity)
        if (index.containsKey(key)) {
            System.out.println("Duplicate Student ID: " + id + " (already exists).");
            return;
        }

        if (studentCount < students.length) {
            students[studentCount++] = student;
            index.put(key, student);
        } else {
            System.out.println("Student list is full.");
        }
    }


     //O(1) expected-time lookup by Student ID (case-insensitive).

    public Student findStudent(String studentId) {
        if (studentId == null) return null;
        return index.get(studentId.toUpperCase());
    }


      //Returns a copy of the active students (array of length studentCount).

    public Student[] getStudents() {
        Student[] copy = new Student[studentCount];
        for (int i = 0; i < studentCount; i++) copy[i] = students[i];
        return copy;
    }

    public int getStudentCount() {
        return studentCount;
    }


      //Computes the average of each student's average grade.
      //Time complexity: O(n) where n = studentCount.

    public double getAverageClassGrade() {
        if (studentCount == 0) return 0.0;
        double sum = 0;
        for (int i = 0; i < studentCount; i++) sum += students[i].calculateAverageGrade();
        return sum / studentCount;
    }


     //Rebuilds the index from the array (useful if future code mutates the array directly).

    @SuppressWarnings("unused")
    private void rebuildIndex() {
        index.clear();
        for (int i = 0; i < studentCount; i++) {
            Student s = students[i];
            if (s != null && s.getStudentId() != null) {
                index.put(s.getStudentId().toUpperCase(), s);
            }
        }
    }
}
