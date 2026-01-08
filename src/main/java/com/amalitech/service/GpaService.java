
package com.amalitech.service;

import com.amalitech.model.Grade;
import com.amalitech.manager.GradeManager;
import com.amalitech.model.Student;
import com.amalitech.manager.StudentManager;
import com.amalitech.reporting.GPACalculator;

import java.util.ArrayList;
import java.util.List;

public class GpaService implements com.amalitech.service.api.IGpaService {
    private final StudentManager students;
    private final GradeManager grades;
    private final GPACalculator calculator;

    public GpaService(StudentManager students, GradeManager grades, GPACalculator calculator) {
        this.students = students;
        this.grades = grades;
        this.calculator = calculator;
    }

    public GPAData computeFor(String studentId) {
        Student s = students.findStudent(studentId);
        if (s == null) return GPAData.notFound();

        // Collect grades for the student
        List<Grade> gs = new ArrayList<>();
        for (int i = 0; i < grades.getGradeCount(); i++) {
            Grade g = grades.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(studentId)) gs.add(g);
        }
        if (gs.isEmpty()) return GPAData.noGrades(s);

        // Build line items + cumulative GPA
        double gpaSum = 0.0;
        List<GPAData.Line> lines = new ArrayList<>();
        for (Grade g : gs) {
            double pts = calculator.toFourPointScale(g.getGrade());
            String letter = calculator.toLetter(g.getGrade());
            lines.add(new GPAData.Line(g.getSubject().getSubjectName(), g.getGrade(), pts, letter));
            gpaSum += pts;
        }
        double cumulative = gpaSum / gs.size();

        // Rank among all students
        int totalStudents = students.getStudentCount();
        double[] allGpas = new double[totalStudents];
        Student[] all = students.getStudents();
        for (int i = 0; i < totalStudents; i++) {
            List<Double> pct = new ArrayList<>();
            String sid = all[i].getStudentId();
            for (int j = 0; j < grades.getGradeCount(); j++) {
                Grade gg = grades.getGradeAt(j);
                if (gg != null && gg.getStudentId().equalsIgnoreCase(sid)) {
                    pct.add(gg.getGrade());
                }
            }
            allGpas[i] = pct.isEmpty() ? 0.0 : calculator.computeGPA(pct);
        }
        int rank = 1;
        for (double other : allGpas) if (other > cumulative) rank++;

        String overallLetter = calculator.toLetter(s.calculateAverageGrade());
        return GPAData.ok(s, lines, cumulative, overallLetter, rank, totalStudents);
    }

    /** Builds a formatted console string for display (presentation helper). */
    public String toConsoleString(GPAData data) {
        if (!data.found) return "Student not found!";
        if (!data.hasGrades) return "No grades recorded for this student.";

        StringBuilder sb = new StringBuilder();
        sb.append("\nStudent: ").append(data.student.getStudentId()).append(" - ").append(data.student.getName()).append("\n");
        sb.append("Type: ").append(data.student.getStudentType()).append("\n");
        sb.append(String.format("Overall Average: %.2f%%%n", data.student.calculateAverageGrade()));
        sb.append("\nGPA CALCULATION (4.0 Scale)\n");
        sb.append(String.format("%-12s │ %-6s │ %-10s%n", "Subject", "Grade", "GPA Points"));
        sb.append("─".repeat(40)).append("\n");
        for (GPAData.Line line : data.lines) {
            sb.append(String.format("%-12s │ %5.0f%% │ %.1f (%s)%n",
                    line.subject, line.gradePct, line.points, line.letter));
        }
        sb.append("\n");
        sb.append(String.format("Cumulative GPA: %.2f / 4.0%n", data.cumulative));
        sb.append(String.format("Letter Grade: %s%n", data.overallLetter));
        sb.append(String.format("Class Rank: %d of %d%n", data.rank, data.totalStudents));
        return sb.toString();
    }
}
