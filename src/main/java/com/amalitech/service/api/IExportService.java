
package com.amalitech.service.api;

import com.amalitech.base.Student;
import com.amalitech.exceptions.ExportFailedException;
import com.amalitech.reporting.ReportType;

/** formatChoice: 1=CSV, 2=JSON, 3=Binary, 4=All */
public interface IExportService {
    /** Backward-compatibility */
    void exportDetailedReport(Student student, int formatChoice) throws ExportFailedException;


    ExportSummary exportReport(Student student, int formatChoice, ReportType reportType)
            throws ExportFailedException;
}
