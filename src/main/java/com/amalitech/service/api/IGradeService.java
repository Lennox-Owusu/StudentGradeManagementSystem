
package com.amalitech.service.api;

import com.amalitech.Grade;
import com.amalitech.Student;
import com.amalitech.Subject;
import com.amalitech.exceptions.InvalidGradeException;

import java.util.List;

public interface IGradeService {
    Grade recordGrade(Student student, Subject subject, double gradePct) throws InvalidGradeException;
    List<Grade> gradesOf(String studentId);
    double coreAverage(String studentId);
    double electiveAverage(String studentId);
    double overallAverage(String studentId);
}
