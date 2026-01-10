
package com.amalitech.service.api;

import com.amalitech.base.Grade;
import com.amalitech.base.Student;
import com.amalitech.base.Subject;
import com.amalitech.exceptions.InvalidGradeException;

import java.util.List;

public interface IGradeService {
    Grade recordGrade(Student student, Subject subject, double gradePct) throws InvalidGradeException;
    List<Grade> gradesOf(String studentId);
    double coreAverage(String studentId);
    double electiveAverage(String studentId);
    double overallAverage(String studentId);
}
