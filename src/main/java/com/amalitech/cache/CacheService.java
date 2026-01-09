
package com.amalitech.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

//Simple thread-safe cache with hit/miss tracking.
public final class CacheService<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final int maxSize;

    public CacheService(int maxSize) { this.maxSize = Math.max(32, maxSize); }

    public V get(K key) {
        V v = map.get(key);
        if (v != null) hits.increment(); else misses.increment();
        return v;
    }

    public void getOrLoad(K key, Supplier<V> loader) {
        V v = map.get(key);
        if (v != null) { hits.increment(); return; }
        misses.increment();
        v = loader.get();
        if (map.size() >= maxSize) {
            map.clear();
        }
        map.put(key, v);
    }

    //e.g., "87.3%"
    public String hitRateText() {
        double h = hits.sum();
        double m = misses.sum();
        double rate = (h + m == 0) ? 0.0 : (h * 100.0 / (h + m));
        return String.format("%.1f%%", rate);
    }




    //  getters
    public int size() { return map.size(); }
    public long hits() { return hits.sum(); }
    public long misses() { return misses.sum(); }


    private final java.util.concurrent.atomic.LongAdder evict = new java.util.concurrent.atomic.LongAdder();

    public long evictions() { return evict.sum(); }


    public void resetStats() { hits.reset(); misses.reset(); evict.reset(); }
    public void clearAll() { map.clear(); evict.increment(); }


}
