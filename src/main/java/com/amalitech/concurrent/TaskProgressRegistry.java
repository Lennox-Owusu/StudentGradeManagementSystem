
package com.amalitech.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Registry of named task progress: percentage + threads + status.
public final class TaskProgressRegistry {
    public static final class TaskInfo {
        public final String name;
        public final int percent;          // 0–100
        public final int threads;          // number of threads working
        public final boolean completed;

        public TaskInfo(String name, int percent, int threads, boolean completed) {
            this.name = name; this.percent = percent; this.threads = threads; this.completed = completed;
        }
    }

    private static final ConcurrentHashMap<String, TaskInfo> TASKS = new ConcurrentHashMap<>();

    private TaskProgressRegistry() {}

    public static void set(String name, int percent, int threads, boolean completed) {
        TASKS.put(name, new TaskInfo(name, Math.max(0, Math.min(100, percent)), Math.max(0, threads), completed));
    }

    public static Map<String, TaskInfo> snapshot() { return Map.copyOf(TASKS); }
}
