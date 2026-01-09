
package com.amalitech.service.api;

import com.amalitech.Student;
import com.amalitech.exceptions.ExportFailedException;

/** formatChoice: 1=CSV, 2=JSON, 3=Binary, 4=All */
public interface IExportService {
    void exportDetailedReport(Student student, int formatChoice) throws ExportFailedException;
}
