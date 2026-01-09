
package com.amalitech.service.impl;

import com.amalitech.GradeManager;
import com.amalitech.StudentManager;
import com.amalitech.cache.CacheService;
import com.amalitech.dashboard.RealTimeDashboard;
import com.amalitech.service.api.IDashboardService;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class DashboardServiceImpl implements IDashboardService {
    private final Scanner scanner;
    private final StudentManager sm;
    private final GradeManager gm;
    private final ExecutorService fixedPool;
    private final ExecutorService cachedPool;
    private final ScheduledThreadPoolExecutor scheduler;
    private final CacheService<String, Object> cache;

    public DashboardServiceImpl(Scanner scanner,
                                StudentManager sm,
                                GradeManager gm,
                                ExecutorService fixedPool,
                                ExecutorService cachedPool,
                                ScheduledThreadPoolExecutor scheduler,
                                CacheService<String, Object> cache) {
        this.scanner = scanner; this.sm = sm; this.gm = gm;
        this.fixedPool = fixedPool; this.cachedPool = cachedPool;
        this.scheduler = scheduler; this.cache = cache;
    }

    @Override public void run() {
        new RealTimeDashboard().runInteractive(scanner, sm, gm, fixedPool, cachedPool, scheduler, cache);
    }
}
