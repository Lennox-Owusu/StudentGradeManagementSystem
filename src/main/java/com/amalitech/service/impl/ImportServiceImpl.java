
package com.amalitech.service.impl;

import com.amalitech.manager.GradeManager;
import com.amalitech.manager.StudentManager;
import com.amalitech.exceptions.DomainException;
import com.amalitech.io.ImportCoordinator;
import com.amalitech.reporting.StudentReport;
import com.amalitech.service.api.IImportService;

import java.nio.file.Path;

public class ImportServiceImpl implements IImportService {
    private final ImportCoordinator coordinator;
    private final StudentManager sm;
    private final GradeManager gm;

    public ImportServiceImpl(ImportCoordinator coordinator, StudentManager sm, GradeManager gm) {
        this.coordinator = coordinator; this.sm = sm; this.gm = gm;
    }

    @Override
    public StudentReport importReport(Path path, ImportCoordinator.Format format) throws DomainException {
        return coordinator.loadReport(path, format);
    }

    @Override
    public void merge(StudentReport report) {
        coordinator.mergeIntoSystem(report, sm, gm);
    }
}
