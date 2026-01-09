
package com.amalitech.app;

import com.amalitech.GradeManager;
import com.amalitech.StudentManager;
import com.amalitech.cache.CacheService;
import com.amalitech.io.ExportCoordinator;
import com.amalitech.io.ImportCoordinator;
import com.amalitech.service.api.*;
import com.amalitech.service.impl.*;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class AppContext {
    // Core data managers
    public final StudentManager studentManager = new StudentManager(50);
    public final GradeManager gradeManager     = new GradeManager(200);

    // Executors (meets project’s concurrency requirements)
    public final ExecutorService fixedPool = Executors.newFixedThreadPool(4);
    public final ExecutorService cachedPool = Executors.newCachedThreadPool();
    public final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);

    // Shared cache
    public final CacheService<String,Object> cache = new CacheService<>(256);

    // Coordinators (your existing classes)
    public final ExportCoordinator exportCoordinator = new ExportCoordinator();
    public final ImportCoordinator importCoordinator = new ImportCoordinator();

    // Console IO
    public final Scanner scanner = new Scanner(System.in);

    // Services (Batch 2 implementations)
    public final IStudentService students = new StudentServiceImpl(studentManager);
    public final IGradeService grades = new GradeServiceImpl(gradeManager);
    public final IExportService exporter = new ExportServiceImpl(gradeManager, exportCoordinator);
    public final IImportService importer = new ImportServiceImpl(importCoordinator, studentManager, gradeManager);
    public final ISearchService search = new SearchServiceImpl(studentManager, gradeManager);
    public final IDashboardService dashboard =
            new DashboardServiceImpl(scanner, studentManager, gradeManager, fixedPool, cachedPool, scheduler, cache);
    public final IPerformanceMonitorService monitor =
            new PerformanceMonitorServiceImpl(scanner, studentManager, gradeManager, fixedPool, cachedPool, scheduler, cache);
    public final ICacheAdminService cacheAdmin = new CacheAdminServiceImpl(cache, studentManager, gradeManager);
    public final IAuditService audit = new AuditServiceImpl();

    public void shutdown() {
        try { scanner.close(); } catch (Exception ignore) {}
        try { scheduler.shutdownNow(); } catch (Exception ignore) {}
        try { cachedPool.shutdownNow(); } catch (Exception ignore) {}
        try { fixedPool.shutdownNow(); } catch (Exception ignore) {}
    }
}
