
package com.amalitech.service.api;

import com.amalitech.Student;
import com.amalitech.Grade;
import java.util.List;

public interface ISearchService {
    // ID supports wildcards: * and ?
    List<Student> searchStudentsById(String wildcardPattern);
    List<Student> searchStudentsByName(String contains);
    List<Student> searchStudentsByType(String label);

    /** sortMode: 1=Date desc, 2=Date asc, 3=Grade desc, 4=Grade asc */
    List<Grade> queryGradeHistory(String studentId, String subjContains, String type,
                                  String dateFrom, String dateTo, Double minGrade, Double maxGrade, int sortMode);
}
