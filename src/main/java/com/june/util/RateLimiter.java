package com.june.util;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private final int maxPerMin;
    private final Map<String, Deque<Long>> map = new ConcurrentHashMap<>();

    public RateLimiter(int maxPerMin) {
        this.maxPerMin = maxPerMin;
    }

    public synchronized boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = map.computeIfAbsent(key, k -> new ArrayDeque<>());

        long cutoff = now - 60_000L;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();

        if (q.size() >= maxPerMin) return false;

        q.addLast(now);
        return true;
    }
}
