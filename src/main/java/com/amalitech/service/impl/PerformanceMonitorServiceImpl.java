
package com.amalitech.service.impl;

import com.amalitech.GradeManager;
import com.amalitech.StudentManager;
import com.amalitech.cache.CacheService;
import com.amalitech.monitor.SystemPerformanceMonitor;
import com.amalitech.service.api.IPerformanceMonitorService;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class PerformanceMonitorServiceImpl implements IPerformanceMonitorService {
    private final Scanner scanner;
    private final StudentManager sm;
    private final GradeManager gm;
    private final ExecutorService fixedPool, cachedPool;
    private final ScheduledExecutorService scheduler;
    private final CacheService<String, Object> cache;

    public PerformanceMonitorServiceImpl(Scanner scanner,
                                         StudentManager sm,
                                         GradeManager gm,
                                         ExecutorService fixedPool,
                                         ExecutorService cachedPool,
                                         ScheduledExecutorService scheduler,
                                         CacheService<String, Object> cache) {
        this.scanner = scanner; this.sm = sm; this.gm = gm;
        this.fixedPool = fixedPool; this.cachedPool = cachedPool;
        this.scheduler = scheduler; this.cache = cache;
    }

    @Override public void run() {
        new SystemPerformanceMonitor(2).runInteractive(scanner, sm, gm, fixedPool, cachedPool, scheduler, cache);
    }
}
