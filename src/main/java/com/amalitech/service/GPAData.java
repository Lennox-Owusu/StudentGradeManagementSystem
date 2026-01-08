
package com.amalitech.service;

import com.amalitech.model.Student;
import java.util.Collections;
import java.util.List;

public class GPAData {
    public static class Line {
        public final String subject;
        public final double gradePct;
        public final double points;
        public final String letter;
        public Line(String subject, double gradePct, double points, String letter) {
            this.subject = subject; this.gradePct = gradePct; this.points = points; this.letter = letter;
        }
    }

    public final boolean found;
    public final boolean hasGrades;
    public final Student student;
    public final List<Line> lines;
    public final double cumulative;
    public final String overallLetter;
    public final int rank;
    public final int totalStudents;

    private GPAData(boolean found, boolean hasGrades, Student student, List<Line> lines,
                    double cumulative, String overallLetter, int rank, int totalStudents) {
        this.found = found; this.hasGrades = hasGrades; this.student = student;
        this.lines = lines == null ? Collections.emptyList() : Collections.unmodifiableList(lines);
        this.cumulative = cumulative; this.overallLetter = overallLetter;
        this.rank = rank; this.totalStudents = totalStudents;
    }

    public static GPAData notFound() {
        return new GPAData(false, false, null, null, 0, null, 0, 0);
    }
    public static GPAData noGrades(Student s) {
        return new GPAData(true, false, s, null, 0, null, 0, 0);
    }
    public static GPAData ok(Student s, List<Line> lines, double cumulative,
                             String overallLetter, int rank, int total) {
        return new GPAData(true, true, s, lines, cumulative, overallLetter, rank, total);
    }
}
