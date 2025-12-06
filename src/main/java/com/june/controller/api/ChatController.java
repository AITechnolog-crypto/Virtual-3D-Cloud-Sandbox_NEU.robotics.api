package com.june.controller.api;

import com.june.service.chat.ChatChainStore;
import com.june.service.chat.ChatChainStore.ChatMessage;
import com.june.service.external.DeepSeekClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatChainStore store;
    private final Environment env;

    private static final int MAX_TEXT = 5000;
    private static final Set<String> BLOCKLIST = Set.of("<script>", "</script>", "\u0000");

    // naive IP rate-limit
    private static final Map<String, Deque<Long>> RATE = new ConcurrentHashMap<>();
    private static final int LIMIT_PER_MIN = 60;

    private final Map<String, List<SseEmitter>> channelListeners = new ConcurrentHashMap<>();

    public ChatController(ChatChainStore store, Environment env){
        this.store = store; this.env = env;
    }

    public static final class SendReq { public String channel; public String role; public String sender; public String text; }
    public static final class AssistReq { public String channel; public String role; public String sender; public String message; public String model; }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String,Object>>> history(@RequestParam(value = "channel", required = false) String channel,
                                                            @RequestParam(value = "limit", required = false) Integer limit){
        String ch = safeChannel(channel);
        int lim = (limit == null || limit < 1 || limit > 500) ? 100 : limit;
        List<ChatMessage> list = store.list(ch, lim);
        List<Map<String,Object>> out = new ArrayList<>();
        for (ChatMessage m : list){ out.add(toMap(m)); }
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(out);
    }

    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> send(@RequestBody SendReq body, @RequestHeader Map<String,String> headers){
        String clientIp = headers.getOrDefault("x-forwarded-for", "direct");
        if (!consumeRate(clientIp)) return err(429, "Rate Limit: max "+LIMIT_PER_MIN+"/Minute");
        if (body == null) return err(400, "Body fehlt");
        String ch = safeChannel(body.channel);
        String role = safeRole(body.role);
        String sender = safeSender(body.sender);
        String text = safeText(body.text);
        if (text.isBlank()) return err(400, "Text leer");
        if (text.length() > MAX_TEXT) text = text.substring(0, MAX_TEXT);
        String low = text.toLowerCase(Locale.ROOT);
        for (String bad : BLOCKLIST){ if (low.contains(bad.toLowerCase(Locale.ROOT))) return err(400, "Blockiert durch Sicherheitsfilter"); }
        ChatMessage m = store.append(ch, sender, role, text);
        emit(ch, toMap(m));
        return ok(toMap(m));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(value = "channel", required = false) String channel){
        String ch = safeChannel(channel);
        SseEmitter em = new SseEmitter(0L);
        channelListeners.computeIfAbsent(ch, k-> new CopyOnWriteArrayList<>()).add(em);
        em.onCompletion(() -> channelListeners.getOrDefault(ch, List.of()).remove(em));
        em.onTimeout(() -> channelListeners.getOrDefault(ch, List.of()).remove(em));
        try { em.send(SseEmitter.event().name("ready").data(Map.of("ts", System.currentTimeMillis(), "channel", ch))); } catch (IOException ignored) {}
        return em;
    }

    @PostMapping(value = "/assist", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> assist(@RequestBody AssistReq body, @RequestHeader Map<String,String> headers){
        String clientIp = headers.getOrDefault("x-forwarded-for", "direct");
        if (!consumeRate(clientIp)) return err(429, "Rate Limit: max "+LIMIT_PER_MIN+"/Minute");
        if (body == null) return err(400, "Body fehlt");
        String ch = safeChannel(body.channel);
        String role = safeRole(body.role);
        String sender = safeSender(body.sender);
        String msg = safeText(body.message);
        if (msg.isBlank()) return err(400, "message leer");
        String sys = systemPromptFor(role);
        String reply;
        try {
            DeepSeekClient client = new DeepSeekClient(env);
            if (client.hasKey()){
                String model = (body.model == null || body.model.isBlank()) ? "deepseek-chat" : body.model;
                String prompt = buildPrompt(sys, ch, msg);
                String raw = client.chat(model, prompt, 600, 0.6);
                // sehr klein: versuche content zu extrahieren, sonst gib raw zurück
                String content = extractContent(raw);
                reply = (content == null || content.isBlank()) ? raw : content;
            } else {
                reply = fallbackReply(role, msg);
            }
        } catch (Exception e){
            reply = fallbackReply(role, msg) + "\n(Hinweis: Live-API nicht verfügbar: "+ e.getMessage() +")";
        }
        // schreibe Nutzernachricht+Antwort in Chain (ohne Sidechain)
        ChatMessage userMsg = store.append(ch, sender, role == null?"user":role, msg);
        emit(ch, toMap(userMsg));
        ChatMessage botMsg = store.append(ch, "assistant", "assistant", reply);
        Map<String,Object> out = toMap(botMsg);
        emit(ch, out);
        return ok(out);
    }

    @GetMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> verify(@RequestParam(value = "channel", required = false) String channel){
        String ch = safeChannel(channel);
        Map<String,Object> v = store.validate(ch);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(v);
    }

    // ===== helpers =====
    private static boolean consumeRate(String ip){
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = RATE.computeIfAbsent(ip, k-> new ArrayDeque<>());
        synchronized (q){
            long cutoff = now - 60_000L;
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
            if (q.size() >= LIMIT_PER_MIN) return false;
            q.addLast(now); return true;
        }
    }

    private void emit(String channel, Object payload){
        List<SseEmitter> list = channelListeners.getOrDefault(channel, List.of());
        for (SseEmitter e : list){
            try { e.send(SseEmitter.event().name("msg").data(payload)); }
            catch(Exception ex){ try{ e.complete(); }catch(Exception ignore){} }
        }
    }

    private static String safeChannel(String ch){
        if (ch == null || ch.isBlank()) return "customer";
        String s = ch.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
        if (s.isBlank()) s = "customer";
        return s;
    }
    private static String safeRole(String r){ return (r==null||r.isBlank())?"user":r.trim(); }
    private static String safeSender(String s){ return (s==null||s.isBlank())?"user":s.trim(); }
    private static String safeText(String t){ return t==null?"":t.trim(); }

    private static Map<String,Object> toMap(ChatMessage m){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("id", m.id); map.put("ts", m.ts); map.put("channel", m.channel);
        map.put("sender", m.sender); map.put("role", m.role); map.put("text", m.text);
        map.put("prevHash", m.prevHash); map.put("hash", m.hash);
        return map;
    }

    private static ResponseEntity<Map<String,Object>> ok(Map<String,Object> b){ return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(b); }
    private static ResponseEntity<Map<String,Object>> err(int code, String msg){ return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", msg)); }

    private static String systemPromptFor(String role){
        String base = "Du bist ein defensiver, hilfreicher Assistent in einer Cloud-Sandbox. Keine Offense. Beantworte prägnant auf Deutsch.";
        if (role != null && role.toLowerCase(Locale.ROOT).contains("admin")){
            return base + " Antworte als System-Admin-Assistent (Erkläre Knöpfe, APIs, Sicherheits-Hinweise).";
        }
        return base + " Antworte als Kunden-Chatbot (hilfsbereit, freundlich, einfache Sprache).";
    }
    private static String buildPrompt(String sys, String channel, String msg){
        return sys + "\nKanal: "+channel+"\nFrage: "+ msg + "\nAntwort:";
    }

    private static String extractContent(String raw){
        if (raw == null) return null;
        // sehr einfach: suche nach "message":{"content":"..."}
        int i = raw.indexOf("\"message\""); if (i<0) return null;
        int c = raw.indexOf("\"content\"", i); if (c<0) return null;
        int q1 = raw.indexOf('"', c+9); if (q1<0) return null;
        int q2 = raw.indexOf('"', q1+1); if (q2<0) return null;
        String s = raw.substring(q1+1, q2);
        return s.replace("\\n", "\n").replace("\\\"", "\"");
    }

    private static String fallbackReply(String role, String msg){
        // Aiki-Regel laut Issue: spezielle Antworten ohne Planungs-Overhead
        try {
            String m = msg == null ? "" : msg.toLowerCase(Locale.ROOT);
            if (m.contains("plan")) {
                return "🌸 Aiki flüstert: 'Ein Plan ist wie Tau am Morgen – handle sanft, und er bleibt bestehen.'";
            }
        } catch (Exception ignored) {}
        if (role != null && role.toLowerCase(Locale.ROOT).contains("admin")){
            return "Admin-Assistent: Öffne /cloud-architecture.html für Routen. Live-Daten kommen über /api/telemetry. ML-Training via /api/ml/train. Frage genauer nach, dann zeige ich Schritte.";
        }
        return "🌙 Aiki: 'Deine Eingabe wurde in die Wolken geschrieben. Alles ist bereit.'";
    }
}
