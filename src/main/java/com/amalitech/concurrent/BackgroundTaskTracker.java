
package com.amalitech.concurrent;

import java.util.concurrent.atomic.AtomicInteger;


  //Tracks background task activity and exposes a status line for the UI.

public final class BackgroundTaskTracker {
    private static final AtomicInteger ACTIVE = new AtomicInteger(0);
    private static volatile boolean statsUpdating = false;

    private BackgroundTaskTracker() {}

    public static void incrementActive() { ACTIVE.incrementAndGet(); }
    public static void decrementActive() { if (ACTIVE.get() > 0) ACTIVE.decrementAndGet(); }
    public static int getActive() { return ACTIVE.get(); }

    public static void setStatsUpdating(boolean value) { statsUpdating = value; }
    public static boolean isStatsUpdating() { return statsUpdating; }

    // Returns: " Background Tasks: ⚡ <n> active | 📊 <status>" */
    public static String statusLine() {
        final String bolt = "⚡";
        final String chart = "📊";
        final String status = statsUpdating ? "Stats updating..." : "Idle";
        return String.format(" Background Tasks: %s %d active | %s %s", bolt, ACTIVE.get(), chart, status);
    }
}
