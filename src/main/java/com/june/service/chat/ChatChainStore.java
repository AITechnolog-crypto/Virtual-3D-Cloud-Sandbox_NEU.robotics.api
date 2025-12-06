package com.june.service.chat;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * ChatChainStore — sehr kleine, append-only Hash-Chain ("wie Blockchain", ohne Sidechains)
 * - Speichert je Channel (admin, customer, ...) in data/chat/<channel>.jsonl
 * - Jeder Eintrag enthält prevHash und hash (SHA-256), gebildet über prevHash+ts+sender+role+text
 * - Kein Forking/Sidechain: Es wird immer der aktuelle Head als prevHash verwendet
 */
@Component
public class ChatChainStore {
    private static final Path CHAT_DIR = Paths.get("data", "chat");

    public ChatChainStore(){
        try { Files.createDirectories(CHAT_DIR); } catch (Exception ignored) {}
    }

    public static final class ChatMessage {
        public String id;           // monotonisch steigend als String
        public long ts;             // epoch millis
        public String channel;      // z.B. "admin" oder "customer"
        public String sender;       // frei: "admin", "assistant", Benutzername, etc.
        public String role;         // frei: "user" | "assistant" | "system"
        public String text;         // Nachrichtentext
        public String prevHash;     // Head vor Append
        public String hash;         // SHA-256(prevHash+ts+sender+role+text)
    }

    public synchronized ChatMessage append(String channel, String sender, String role, String text){
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(text, "text");
        try {
            Path file = fileFor(channel);
            Files.createDirectories(file.getParent());
            String prev = headHash(file);
            long ts = System.currentTimeMillis();
            String id = String.valueOf(nextId(file));
            String hash = sha256(prev + "|" + ts + "|" + sender + "|" + role + "|" + text);
            String line = toJsonLine(id, ts, channel, sender, role, text, prev, hash) + "\n";
            Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            ChatMessage m = new ChatMessage();
            m.id = id; m.ts = ts; m.channel = channel; m.sender = sender; m.role = role; m.text = text; m.prevHash = prev; m.hash = hash;
            return m;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public synchronized List<ChatMessage> list(String channel, int limit){
        try {
            Path file = fileFor(channel);
            if (!Files.exists(file)) return Collections.emptyList();
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            int start = Math.max(0, lines.size() - Math.max(1, limit));
            List<ChatMessage> out = new ArrayList<>();
            for (int i=start; i<lines.size(); i++){
                ChatMessage m = parse(lines.get(i));
                if (m != null) out.add(m);
            }
            return out;
        } catch (Exception e){
            return Collections.emptyList();
        }
    }

    public synchronized Map<String,Object> validate(String channel){
        Map<String,Object> res = new LinkedHashMap<>();
        try{
            Path file = fileFor(channel);
            if (!Files.exists(file)){
                res.put("length", 0);
                res.put("valid", true);
                res.put("headHash", "");
                return res;
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            String prev = "";
            int i = 0;
            for (String line : lines){
                i++;
                ChatMessage m = parse(line);
                if (m == null){ res.put("valid", false); res.put("errorAt", i); return res; }
                String expect = sha256(prev + "|" + m.ts + "|" + m.sender + "|" + m.role + "|" + m.text);
                if (!Objects.equals(expect, m.hash)){
                    res.put("valid", false); res.put("errorAt", i); return res;
                }
                prev = m.hash;
            }
            res.put("length", lines.size());
            res.put("valid", true);
            res.put("headHash", prev);
            return res;
        }catch(Exception e){
            res.put("valid", false);
            res.put("error", e.getMessage());
            return res;
        }
    }

    private static Path fileFor(String channel){
        String safe = channel == null ? "default" : channel.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
        return CHAT_DIR.resolve(safe + ".jsonl");
    }

    private static String headHash(Path file){
        try{
            if (!Files.exists(file)) return "";
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i=lines.size()-1; i>=0; i--){
                ChatMessage m = parse(lines.get(i));
                if (m != null) return m.hash == null ? "" : m.hash;
            }
            return "";
        }catch(Exception e){ return ""; }
    }

    private static long nextId(Path file){
        try{
            if (!Files.exists(file)) return 1L;
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i=lines.size()-1; i>=0; i--){
                ChatMessage m = parse(lines.get(i));
                if (m != null){
                    try { return Long.parseLong(m.id) + 1; } catch(Exception ignored) { return lines.size()+1L; }
                }
            }
            return lines.size()+1L;
        }catch(Exception e){ return 1L; }
    }

    private static ChatMessage parse(String line){
        if (line == null || line.isBlank()) return null;
        try{
            // sehr einfacher Parser: erwartet flache JSON-Schlüssel
            ChatMessage m = new ChatMessage();
            m.id = str(line, "id");
            m.ts = longVal(line, "ts");
            m.channel = str(line, "channel");
            m.sender = str(line, "sender");
            m.role = str(line, "role");
            m.text = str(line, "text");
            m.prevHash = str(line, "prevHash");
            m.hash = str(line, "hash");
            return m;
        }catch(Exception e){ return null; }
    }

    private static String toJsonLine(String id, long ts, String channel, String sender, String role, String text, String prev, String hash){
        return "{"+
                quote("id")+":"+quote(id)+","+
                quote("ts")+":"+ts+","+
                quote("channel")+":"+quote(channel)+","+
                quote("sender")+":"+quote(sender)+","+
                quote("role")+":"+quote(role)+","+
                quote("text")+":"+quote(text)+","+
                quote("prevHash")+":"+quote(prev)+","+
                quote("hash")+":"+quote(hash)+
                "}";
    }

    private static String quote(String s){
        return '"' + escape(s) + '"';
    }
    private static String escape(String s){
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"","\\\"").replace("\n","\\n");
    }

    private static String str(String json, String key){
        int i = json.indexOf('"'+key+'"'); if (i<0) return "";
        int c = json.indexOf(':', i); if (c<0) return "";
        int q1 = json.indexOf('"', c+1); if (q1<0) return "";
        int q2 = json.indexOf('"', q1+1); if (q2<0) return "";
        return json.substring(q1+1, q2).replace("\\\"","\"").replace("\\n","\n").replace("\\\\","\\");
    }
    private static long longVal(String json, String key){
        int i = json.indexOf('"'+key+'"'); if (i<0) return 0L;
        int c = json.indexOf(':', i); if (c<0) return 0L;
        int j=c+1; StringBuilder b=new StringBuilder();
        while (j<json.length()){
            char ch=json.charAt(j++);
            if ((ch>='0'&&ch<='9')) b.append(ch); else if (b.length()>0) break;
        }
        try { return Long.parseLong(b.toString()); } catch(Exception e){ return 0L; }
    }

    private static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch(Exception e){ return ""; }
    }
}
