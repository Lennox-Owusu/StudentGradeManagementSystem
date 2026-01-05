
package com.amalitech.ui;

/**
 * Abstraction for core menu actions.
 * The Main class will provide an implementation that calls its existing methods.
 */
public interface CoreMenuHandlers {
    // STUDENT MANAGEMENT
    void addStudent();           // menu 1
    void viewStudents();         // menu 2
    void recordGrade();          // menu 3
    void viewGradeReport();      // menu 4

    // FILE OPERATIONS
    void exportGradeReport();            // (legacy) menu 5 - your existing single-format export
    void exportGradeReportMultiFormat(); // (new) menu 5 - multi-format export wizard

    // ANALYTICS & REPORTING
    void calculateStudentGpa(); // menu 8
    void viewClassStatistics(); // menu 9

    // SEARCH & QUERY
    void bulkImportGrades();    // menu 7
    void searchStudents();      // menu 14 (optional mapping for now)
}
