
package com.amalitech.service.impl;

import com.amalitech.Grade;
import com.amalitech.GradeManager;
import com.amalitech.Student;
import com.amalitech.Subject;
import com.amalitech.exceptions.InvalidGradeException;
import com.amalitech.monitor.GradeEventTracker;
import com.amalitech.service.api.IGradeService;
import com.amalitech.util.Validators;

import java.util.ArrayList;
import java.util.List;

public class GradeServiceImpl implements IGradeService {
    private final GradeManager gm;

    public GradeServiceImpl(GradeManager gm) { this.gm = gm; }

    @Override
    public Grade recordGrade(Student student, Subject subject, double gradePct) throws InvalidGradeException {
        Validators.requireGradeInRange(gradePct);

        long t0 = System.currentTimeMillis();
        Grade g = new Grade(student.getStudentId(), subject, gradePct);
        gm.addGrade(g);

        // Persist also on student aggregates (keeps your model behavior intact)
        student.recordGrade(gradePct);
        student.enrollSubject(subject);

        // Metrics for dashboards
        GradeEventTracker.recordEventNow();
        GradeEventTracker.recordProcessingTime(System.currentTimeMillis() - t0);
        return g;
    }

    @Override
    public List<Grade> gradesOf(String studentId) {
        List<Grade> out = new ArrayList<>();
        for (int i = 0; i < gm.getGradeCount(); i++) {
            Grade g = gm.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(studentId)) out.add(g);
        }
        return out;
    }

    @Override public double coreAverage(String studentId) { return gm.calculateCoreAverage(studentId); }
    @Override public double electiveAverage(String studentId) { return gm.calculateElectiveAverage(studentId); }
    @Override public double overallAverage(String studentId) { return gm.calculateOverallAverage(studentId); }
}
