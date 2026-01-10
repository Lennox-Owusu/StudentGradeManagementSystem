
package com.amalitech.service.impl;

import com.amalitech.base.Grade;
import com.amalitech.manager.GradeManager;
import com.amalitech.base.Student;
import com.amalitech.manager.StudentManager;
import com.amalitech.service.api.ISearchService;

import java.util.*;
import java.util.regex.Pattern;

public class SearchServiceImpl implements ISearchService {
    private final StudentManager sm;
    private final GradeManager gm;

    public SearchServiceImpl(StudentManager sm, GradeManager gm) {
        this.sm = sm; this.gm = gm;
    }

    @Override
    public List<Student> searchStudentsById(String pattern) {
        // wildcard (*, ?) to regex
        String rx = "^" + pattern.replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".") + "$";
        Pattern p = Pattern.compile(rx, Pattern.CASE_INSENSITIVE);
        List<Student> out = new ArrayList<>();
        for (Student s : sm.getStudents()) if (p.matcher(s.getStudentId()).find()) out.add(s);
        return out;
    }

    @Override
    public List<Student> searchStudentsByName(String contains) {
        String needle = (contains == null ? "" : contains).toLowerCase();
        List<Student> out = new ArrayList<>();
        for (Student s : sm.getStudents()) if (s.getName().toLowerCase().contains(needle)) out.add(s);
        return out;
    }

    @Override
    public List<Student> searchStudentsByType(String label) {
        List<Student> out = new ArrayList<>();
        for (Student s : sm.getStudents()) if (s.getStudentType().equalsIgnoreCase(label)) out.add(s);
        return out;
    }

    @Override
    public List<Grade> queryGradeHistory(String studentId, String subjContains, String type,
                                         String dateFrom, String dateTo,
                                         Double minGrade, Double maxGrade, int sortMode) {
        List<Grade> matches = new ArrayList<>();
        for (int i = 0; i < gm.getGradeCount(); i++) {
            Grade g = gm.getGradeAt(i);
            if (g == null) continue;

            if (studentId != null && !studentId.isBlank() &&
                    !g.getStudentId().equalsIgnoreCase(studentId)) continue;

            if (subjContains != null && !subjContains.isBlank()) {
                String sname = g.getSubject().getSubjectName();
                if (sname == null || !sname.toLowerCase().contains(subjContains.toLowerCase())) continue;
            }

            if (type != null && !type.isBlank()) {
                String t = g.getSubject().getSubjectType();
                if (t == null || !t.equalsIgnoreCase(type)) continue;
            }

            if (dateFrom != null && !dateFrom.isBlank()) {
                if (g.getDate() == null || g.getDate().compareTo(dateFrom) < 0) continue;
            }
            if (dateTo != null && !dateTo.isBlank()) {
                if (g.getDate() == null || g.getDate().compareTo(dateTo) > 0) continue;
            }

            if (minGrade != null && g.getGrade() < minGrade) continue;
            if (maxGrade != null && g.getGrade() > maxGrade) continue;

            matches.add(g);
        }

        Comparator<Grade> byDateAsc  = Comparator.comparing(Grade::getDate, Comparator.nullsLast(String::compareTo));
        Comparator<Grade> byGradeAsc = Comparator.comparingDouble(Grade::getGrade);

        switch (sortMode) {
            case 2 -> matches.sort(byDateAsc);
            case 3 -> matches.sort(byGradeAsc.reversed());
            case 4 -> matches.sort(byGradeAsc);
            default -> matches.sort(byDateAsc.reversed());
        }
        return matches;
    }
}
