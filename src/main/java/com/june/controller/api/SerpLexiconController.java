package com.june.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.june.service.external.SerpApiClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SERP Lexikon Controller – blockweises Paging, Schnittstellen‑Sicherung & Prüfung.
 *
 * Endpunkte:
 *  - GET /api/serp/ping      → { ok, hasKey }
 *  - GET /api/serp/lexicon   → gruppierte Ergebnisse (A–Z), blockweise
 */
@RestController
@RequestMapping("/api/serp")
@CrossOrigin(origins = "*")
public class SerpLexiconController {

    private final Environment env;
    private final ObjectMapper om = new ObjectMapper();
    private final RateLimiter rl = new RateLimiter(90); // 90 req/min pro IP

    public SerpLexiconController(Environment env) {
        this.env = env;
    }

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> ping() {
        SerpApiClient client = new SerpApiClient(env);
        return Map.of("ok", true, "hasKey", client.hasKey(), "time", Instant.now().toString());
    }

    @GetMapping(value = "/lexicon", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> lexicon(
            @RequestParam("q") String q,
            @RequestParam(value = "block", required = false, defaultValue = "0") int block,
            @RequestParam(value = "block_size", required = false, defaultValue = "10") int blockSize,
            @RequestParam Map<String, String> allParams,
            HttpServletRequest req
    ) {
        String ip = clientIp(req);
        if (!rl.allow("lex:" + ip)) {
            return ResponseEntity.status(429).body(Map.of("error", "Zu viele Anfragen – bitte warten"));
        }
        // Validation
        q = q == null ? "" : q.trim();
        if (q.isEmpty() || q.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ungültige Anfrage: q leer/zu lang"));
        }
        if (!isPrintable(q)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ungültige Zeichen in q"));
        }
        block = Math.max(0, block);
        blockSize = Math.max(1, Math.min(50, blockSize));

        SerpApiClient client = new SerpApiClient(env);
        if (!client.hasKey()) {
            // Graceful fallback: provide mock, grouped results so UI remains functional without a key
            String term = q;
            String letter = term.isEmpty() ? "#" : term.substring(0,1).toUpperCase(Locale.ROOT);
            if (!letter.matches("[A-ZÄÖÜ#]")) letter = "#";
            List<Map<String, Object>> list = new ArrayList<>();
            list.add(Map.of(
                    "term", term + " – Überblick",
                    "title", term + " – Überblick",
                    "url", "https://example.com/lex/" + term + "/overview",
                    "domain", "example.com",
                    "snippet", "Mock (Demo) – keine Live‑Daten."
            ));
            list.add(Map.of(
                    "term", term + " – Grundlagen",
                    "title", term + " – Grundlagen",
                    "url", "https://example.org/lex/" + term + "/basics",
                    "domain", "example.org",
                    "snippet", "Mock (Demo) – keine Live‑Daten."
            ));
            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            groups.put(letter, list);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("q", q);
            out.put("engine", Optional.ofNullable(allParams.get("engine")).orElse("google"));
            out.put("block", block);
            out.put("blockSize", blockSize);
            out.put("estimatedTotal", 2);
            out.put("groups", groups);
            out.put("count", list.size());
            out.put("mock", true);
            out.put("note", "SERPAPI_KEY fehlt – Demo-Modus aktiv");
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(out);
        }

        try {
            // Build options: pass-through a safe subset, map block→start/num
            Map<String, String> opts = new HashMap<>();
            String[] pass = new String[]{
                    "engine","location","hl","gl","tbm","google_domain","safe","tbs","time_period",
                    "device","as_sitesearch","include_domains","exclude_domains","filter","lr","cr","uule"
            };
            for (String k : pass) {
                String v = allParams.get(k);
                if (v != null && !v.isBlank()) opts.put(k, v);
            }
            opts.put("num", String.valueOf(blockSize));
            opts.put("start", String.valueOf(block * blockSize));

            String raw = client.search(q, opts);
            Map<String, Object> json = om.readValue(raw, new TypeReference<>() {});

            // Extract items (organic_results preferred)
            List<Map<String, Object>> items = new ArrayList<>();
            Object org = json.get("organic_results");
            if (org instanceof List<?>) {
                for (Object o : (List<?>) org) if (o instanceof Map<?, ?> m) items.add(cast(m));
            } else if (json.get("results") instanceof List<?>) {
                for (Object o : (List<?>) json.get("results")) if (o instanceof Map<?, ?> m) items.add(cast(m));
            }

            // Normalize entries
            List<Map<String, Object>> entries = new ArrayList<>();
            for (Map<String, Object> it : items) {
                String title = str(it.getOrDefault("title", it.get("name")));
                String link  = str(it.getOrDefault("link", it.get("url")));
                String snippet = str(it.getOrDefault("snippet", it.get("description")));
                if (title.isBlank() && snippet.isBlank()) continue;
                String domain = link.replaceFirst("(?i)^https?://", "");
                int slash = domain.indexOf('/'); if (slash>0) domain = domain.substring(0, slash);
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("term", title.isBlank() ? snippet : title);
                e.put("title", title);
                e.put("url", link);
                e.put("domain", domain);
                e.put("snippet", snippet);
                entries.add(e);
            }

            // Sort by title and group A–Z
            entries.sort(Comparator.comparing(a -> str(a.get("term")).toLowerCase(Locale.ROOT)));
            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Map<String, Object> e : entries) {
                String term = str(e.get("term")).trim();
                String letter = term.isEmpty() ? "#" : term.substring(0,1).toUpperCase(Locale.ROOT);
                if (!letter.matches("[A-ZÄÖÜ#]")) letter = "#";
                groups.computeIfAbsent(letter, k -> new ArrayList<>()).add(e);
            }

            // Pager info: echo block & size; try to forward total results if present
            long total = 0;
            try {
                Object si = json.get("search_information");
                if (si instanceof Map<?,?> m) {
                    Object tr = m.get("total_results");
                    if (tr instanceof Number n) total = n.longValue();
                    else if (tr instanceof String s) total = Long.parseLong(s.replaceAll("[^0-9]", ""));
                }
            } catch (Exception ignore) {}

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("q", q);
            out.put("engine", opts.getOrDefault("engine", "google"));
            out.put("block", block);
            out.put("blockSize", blockSize);
            out.put("estimatedTotal", total);
            out.put("groups", groups);
            out.put("count", entries.size());
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(out);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "Lexikon-Query fehlgeschlagen: " + e.getMessage()));
        }
    }

    // ===== Helpers =====
    private static boolean isPrintable(String s) {
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            if (Character.isISOControl(c)) return false;
        }
        return true;
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Map<?,?> m){ return (Map<String, Object>) m; }
    private static String str(Object o){ return o==null?"":String.valueOf(o); }
    private static String clientIp(HttpServletRequest req){
        try {
            String xf = req.getHeader("X-Forwarded-For");
            if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
            return Optional.ofNullable(req.getRemoteAddr()).orElse("public");
        } catch (Exception e) { return "public"; }
    }

    static class RateLimiter {
        private final int maxPerMin;
        private final Map<String, Deque<Long>> map = new ConcurrentHashMap<>();
        RateLimiter(int maxPerMin){ this.maxPerMin = maxPerMin; }
        synchronized boolean allow(String key){
            long now = System.currentTimeMillis(), win = now - 60_000L;
            Deque<Long> q = map.computeIfAbsent(key, k -> new ArrayDeque<>());
            while (!q.isEmpty() && q.peekFirst() < win) q.pollFirst();
            if (q.size() >= maxPerMin) return false;
            q.addLast(now);
            return true;
        }
    }
}
