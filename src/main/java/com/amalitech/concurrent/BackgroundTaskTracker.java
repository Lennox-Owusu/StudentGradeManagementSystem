
package com.amalitech.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

/** Tracks background task activity and exposes a single-line status for the UI. */
public final class BackgroundTaskTracker {
    private static final AtomicInteger ACTIVE = new AtomicInteger(0);
    private static volatile boolean statsUpdating = false;

    private BackgroundTaskTracker() {}

    public static void incrementActive() { ACTIVE.incrementAndGet(); }
    public static void decrementActive() { if (ACTIVE.get() > 0) ACTIVE.decrementAndGet(); }
    public static void setStatsUpdating(boolean value) { statsUpdating = value; }

    /** Convenience: set initial count at boot or when recalculating from pools. */
    public static void setActiveCount(int n) { ACTIVE.set(Math.max(0, n)); }

    /** Returns: "Background Tasks: ⚡ <n> active | 📊 Stats updating..." */
    public static String statusLine() {
        final String bolt = "⚡";
        final String chart = "📊";
        final String status = statsUpdating ? "Stats updating..." : "Idle";
        return String.format("Background Tasks: %s %d active | %s %s", bolt, ACTIVE.get(), chart, status);
    }
}
