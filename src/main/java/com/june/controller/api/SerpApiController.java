package com.june.controller.api;

import com.june.service.external.SerpApiClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/serp")
@CrossOrigin(origins = "*")
public class SerpApiController {

    private final Environment env;

    public SerpApiController(Environment env) {
        this.env = env;
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> search(
            @RequestParam("q") String q,
            @RequestParam Map<String, String> allParams
    ) {
        SerpApiClient client = new SerpApiClient(env);
        if (!client.hasKey()) {
            // Graceful fallback: return deterministic mock payload so the UI remains usable without a key
            String engine = Optional.ofNullable(allParams.get("engine")).filter(s->!s.isBlank()).orElse("google");
            String escQ = q.replace("\\", "\\\\").replace("\"", "\\\"");
            String mock = "{"+
                    "\"mock\":true,"+
                    "\"engine\":\""+engine+"\","+
                    "\"query\":\""+escQ+"\","+
                    "\"note\":\"SERPAPI_KEY fehlt – Demo-Modus aktiv\","+
                    "\"organic_results\":["+
                    "{\"title\":\"Demo: "+escQ+" – Ergebnis 1\",\"link\":\"https://example.com/"+escQ+"/1\",\"snippet\":\"Mock-Ergebnis (keine Live-Daten).\"},"+
                    "{\"title\":\"Demo: "+escQ+" – Ergebnis 2\",\"link\":\"https://example.org/"+escQ+"/2\",\"snippet\":\"Mock-Ergebnis (keine Live-Daten).\"},"+
                    "{\"title\":\"Demo: "+escQ+" – Ergebnis 3\",\"link\":\"https://example.net/"+escQ+"/3\",\"snippet\":\"Mock-Ergebnis (keine Live-Daten).\"}"
                    + "]}";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mock);
        }
        try {
            Map<String, String> opts = new HashMap<>();
            // Default broader result count if none provided
            if (!allParams.containsKey("num")) {
                opts.put("num", "20");
            }
            // Pass-through allowed parameters
            String[] pass = new String[]{
                    "engine","location","num","hl","gl","tbm","start","page",
                    "google_domain","safe","tbs","time_period","device","as_sitesearch",
                    "include_domains","exclude_domains","filter","lr","cr","uule"
            };
            for (String k : pass) {
                String v = allParams.get(k);
                if (v != null && !v.isBlank()) opts.put(k, v);
            }
            String json = client.search(q, opts);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (IllegalArgumentException iae) {
            String msg = "{\"error\":\"" + iae.getMessage().replace("\"", "'") + "\"}";
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(msg);
        } catch (Exception e) {
            String msg = "{\"error\":\"SERP Anfrage fehlgeschlagen: " + e.getMessage().replace("\"", "'") + "\"}";
            return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON).body(msg);
        }
    }

    /**
     * Safe page fetch proxy – returns full HTML for a given URL with basic SSRF/XSS safeguards.
     */
    @GetMapping(value = "/fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> fetch(@RequestParam("url") String url,
                                                     @RequestParam(value = "max", required = false, defaultValue = "2000000") int maxBytes) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nur http/https erlaubt"));
            }
            String host = uri.getHost();
            if (host == null) return ResponseEntity.badRequest().body(Map.of("error", "Ungültige URL"));
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()) {
                return ResponseEntity.status(403).body(Map.of("error", "Interne/Private Hosts sind blockiert"));
            }
            HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "Mozilla/5.0 (SERP Preview Proxy)")
                    .header("Accept", "text/html,application/xhtml+xml,text/plain;q=0.9,*/*;q=0.8")
                    .GET().build();
            HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            String ct = Optional.ofNullable(res.headers().firstValue("Content-Type").orElse("text/html")).orElse("text/html");
            long len = res.body() == null ? 0 : res.body().length;
            if (len > maxBytes) {
                return ResponseEntity.status(413).body(Map.of("error", "Seite zu groß", "contentType", ct, "length", len));
            }
            String html = new String(res.body(), java.nio.charset.Charset.forName("UTF-8"));
            // Basic script stripping
            html = html.replaceAll("(?is)<script[^>]*>.*?</script>", "");
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("url", url);
            out.put("status", res.statusCode());
            out.put("contentType", ct);
            out.put("length", len);
            out.put("html", html);
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(out);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(502).body(Map.of("error", "Fetch fehlgeschlagen: "+ e.getMessage()));
        }
    }
}
