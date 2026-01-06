
package com.amalitech.monitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public final class PoolMetrics {
    private PoolMetrics() {}

    public static String fixedStatus(ExecutorService fixed) {
        if (fixed instanceof ThreadPoolExecutor tpe) {
            return String.format("Fixed Pool (Reports): %d/%d active, Queue: %d pending",
                    tpe.getActiveCount(), tpe.getPoolSize(), tpe.getQueue().size());
        }
        return "Fixed Pool (Reports): n/a";
    }

    public static String cachedStatus(ExecutorService cached) {
        if (cached instanceof ThreadPoolExecutor tpe) {
            return String.format("Cached Pool (Stats): %d/%d active", tpe.getActiveCount(), tpe.getPoolSize());
        }
        return "Cached Pool (Stats): n/a";
    }

    public static String scheduledStatus(ScheduledThreadPoolExecutor sched) {
        int queued = (sched == null) ? 0 : sched.getQueue().size();
        int poolSz = (sched == null) ? 0 : sched.getPoolSize();
        return String.format("Scheduled Pool: %d tasks scheduled (poolSize=%d)", queued, poolSz);
    }

    public static String memoryStatus() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long total = rt.maxMemory() / (1024 * 1024);
        return String.format("Memory Usage: %d MB / %d MB", used, total);
    }

    public static int activeThreads() {
        return Thread.activeCount();
    }
}
