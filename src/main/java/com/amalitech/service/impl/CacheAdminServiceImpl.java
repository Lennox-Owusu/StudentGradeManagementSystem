
package com.amalitech.service.impl;

import com.amalitech.GradeManager;
import com.amalitech.StudentManager;
import com.amalitech.cache.CacheService;
import com.amalitech.service.api.ICacheAdminService;

public class CacheAdminServiceImpl implements ICacheAdminService {
    private final CacheService<String, Object> cache;
    private final StudentManager sm;
    private final GradeManager gm;

    public CacheAdminServiceImpl(CacheService<String, Object> cache,
                                 StudentManager sm,
                                 GradeManager gm) {
        this.cache = cache; this.sm = sm; this.gm = gm;
    }

    @Override public void warm() {
        cache.getOrLoad("subjects", () -> java.util.List.of("Mathematics", "English", "Science"));
        cache.getOrLoad("students", () -> java.util.Arrays.asList(sm.getStudents()));
        cache.getOrLoad("gradeCount", gm::getGradeCount);
    }

    @Override public void benchmark() {
        for (int i = 0; i < 500; i++) {
            cache.get("subjects"); cache.get("students"); cache.get("gradeCount");
        }
    }

    @Override public void clear() {
        try { cache.clearAll(); } catch (Throwable ignored) { }
    }

    @Override public void resetCounters() {
        try { cache.resetStats(); } catch (Throwable ignored) { }
    }

    @Override public String status() { return cache.hitRateText(); }
}
