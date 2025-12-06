package com.june.service.chat;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class SessionService {
    private static final int HISTORY_LIMIT = 50;

    private final Map<String, Boolean> consent = new ConcurrentHashMap<>();
    private final Map<String, Deque<Map<String, String>>> history = new ConcurrentHashMap<>();

    // simple rate limit: max 10 messages per 30 seconds per session
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();
    private static final int RL_MAX = 10;
    private static final long RL_WINDOW_MS = 30_000L;

    public void setConsent(String sessionId, boolean ok) {
        consent.put(sessionId, ok);
    }

    public boolean hasConsent(String sessionId) {
        return consent.getOrDefault(sessionId, false);
    }

    public void touch(String sessionId) {
        consent.putIfAbsent(sessionId, false); // ensure key exists
    }

    public boolean isRateLimited(String sessionId) {
        final long now = System.currentTimeMillis();
        buckets.putIfAbsent(sessionId, new ConcurrentLinkedDeque<>());
        Deque<Long> q = buckets.get(sessionId);
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > RL_WINDOW_MS) {
                q.pollFirst();
            }
            if (q.size() >= RL_MAX) return true;
            q.addLast(now);
        }
        return false;
    }

    public void addMessage(String sessionId, String role, String content) {
        history.putIfAbsent(sessionId, new ConcurrentLinkedDeque<>());
        Deque<Map<String, String>> q = history.get(sessionId);
        synchronized (q) {
            Map<String, String> m = new HashMap<>();
            m.put("role", role);
            m.put("content", content);
            m.put("ts", Instant.now().toString());
            q.addLast(m);
            while (q.size() > HISTORY_LIMIT) q.pollFirst();
        }
    }

    public List<Map<String, String>> getMessages(String sessionId) {
        Deque<Map<String, String>> q = history.get(sessionId);
        if (q == null) return Collections.emptyList();
        return new ArrayList<>(q);
    }
}
