
package com.amalitech.service.api;

import com.amalitech.reporting.StudentReport;
import com.amalitech.exceptions.DomainException;
import com.amalitech.io.ImportCoordinator;
import java.nio.file.Path;

public interface IImportService {
    StudentReport importReport(Path path, ImportCoordinator.Format format) throws DomainException;
    void merge(StudentReport report);
}
