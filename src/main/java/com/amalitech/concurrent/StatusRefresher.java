
package com.amalitech.concurrent;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Polls thread pools every few seconds and updates BackgroundTaskTracker with a live active count.
 * Call start() once at app boot; call stop() on shutdown.
 */
public final class StatusRefresher implements Runnable {
    private final ThreadPoolExecutor fixed;
    private final ThreadPoolExecutor cached;
    private final ScheduledThreadPoolExecutor scheduler;
    private volatile boolean running = true;
    private final int intervalSeconds;

    public StatusRefresher(ThreadPoolExecutor fixed,
                           ThreadPoolExecutor cached,
                           ScheduledThreadPoolExecutor scheduler,
                           int intervalSeconds) {
        this.fixed = fixed;
        this.cached = cached;
        this.scheduler = scheduler;
        this.intervalSeconds = Math.max(1, intervalSeconds);
    }

    /** Start in a daemon thread so it won’t block app shutdown. */
    public Thread start() {
        Thread t = new Thread(this, "StatusRefresher");
        t.setDaemon(true);
        t.start();
        return t;
    }

    /** Signal to stop the refresher loop. */
    public void stop() { running = false; }

    @Override
    public void run() {
        // initial mark: we consider stats updating while the monitor is active;
        BackgroundTaskTracker.setStatsUpdating(false);

        while (running) {
            int active = 0;
            try {
                if (fixed != null)   active += fixed.getActiveCount();
                if (cached != null)  active += cached.getActiveCount();
                if (scheduler != null) active += scheduler.getActiveCount();
            } catch (Throwable ignored) { /* keep resilient */ }

            BackgroundTaskTracker.setActiveCount(active);

            try { Thread.sleep(intervalSeconds * 1000L); }
            catch (InterruptedException ie) { break; }
        }
    }
}
