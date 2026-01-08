
package com.amalitech.service.api;

import com.amalitech.model.Student;
import java.util.List;

public interface IReportService {
    String buildReport(Student student, int choice);
    String buildViewReport(Student student);
    String buildSearchResultsReport(List<Student> results);
}
