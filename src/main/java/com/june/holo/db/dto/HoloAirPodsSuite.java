package com.june.holo;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/* ========================================================================
   HoloAirPodsSuite – Offline Hologramm + AirPods Bridge + Ritual + REST + Metrics
   - Keine Internet-Abhängigkeit, alles lokal
   - Biometrie-Gate via registrierte Hashes (Key-/Marker-Hash), KEINE echte DNA!
   - AirPods-Gesten: doubleTap / tripleTap / longPress / tilt
   - Rituale: Wind (wickeln), Inschallah (Segen), Globe öffnen
   - Energie (Simulation): Solar, Plasma, Levitation
   - Wallet, Kontakte, Notizen
   - REST & /metrics (Prometheus-Style)

   Starten (Beispiel):
     java com.june.holo.HoloAirPodsSuite 9090

   Beispiel-Auth Header (POST-Endpoints):
     X-User: Sanel Crnkic
     X-Auth: b8d0d1ff1e0d0a207ed2f3eb0f1f2af33138997934796c6850c650f8d4339526
   ======================================================================== */
public class HoloAirPodsSuite {

    /* ============================== ENV + UTILS ============================== */
    static class Env {
        static final Path ROOT      = Paths.get(".").toAbsolutePath().normalize();
        static final Path DATA_DIR  = ROOT.resolve("data");
        static final Path KEYS_DIR  = ROOT.resolve("keys");
        static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        static void ensureDirs() {
            try { Files.createDirectories(DATA_DIR); Files.createDirectories(KEYS_DIR); }
            catch (IOException e){ throw new UncheckedIOException(e); }
        }
        static String nowIso(){ return LocalDateTime.now().format(ISO); }
    }
    static class Hash {
        static String hexSha256(byte[] data){
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] d = md.digest(data);
                StringBuilder sb=new StringBuilder();
                for(byte b:d) sb.append(String.format("%02x", b));
                return sb.toString();
            } catch(Exception e){ throw new RuntimeException(e); }
        }
        static String hexSha256OfFile(Path p){
            try { return hexSha256(Files.readAllBytes(p)); }
            catch(IOException e){ throw new UncheckedIOException(e); }
        }
    }

    /* ============================== METRICS ============================== */
    static class MetricsSink {
        private final Map<String, Double> gauges = new ConcurrentHashMap<>();
        private final Map<String, Long> counters = new ConcurrentHashMap<>();
        void gauge(String name, double val){ gauges.put(name, val); }
        void add(String name, double delta){ gauges.merge(name, delta, Double::sum); }
        void count(String name){ counters.merge(name, 1L, Long::sum); }
        String asPrometheus() {
            StringBuilder sb=new StringBuilder();
            for (var e : counters.entrySet()) {
                sb.append("# TYPE ").append(e.getKey()).append(" counter\n")
                  .append(e.getKey()).append(" ").append(e.getValue()).append("\n");
            }
            for (var e : gauges.entrySet()) {
                sb.append("# TYPE ").append(e.getKey()).append(" gauge\n")
                  .append(e.getKey()).append(" ").append(String.format(Locale.US,"%.3f", e.getValue())).append("\n");
            }
            return sb.toString();
        }
    }

    /* ============================== SECURITY (OFFLINE) ============================== */
    static class UserRegistry {
        static class Entry { final String keyHash; final String markerHash;
            Entry(String k, String m){ keyHash=k==null?"":k.toLowerCase(); markerHash=m==null?"":m.toLowerCase(); } }
        private final Map<String, Entry> map = new ConcurrentHashMap<>();
        void register(String user, String keyHash, String markerHash){ map.put(user, new Entry(keyHash, markerHash)); }
        boolean check(String user, String provided){
            if (provided==null || user==null) return false;
            Entry e = map.get(user);
            if (e==null) return false;
            String p = provided.toLowerCase();
            return (!e.keyHash.isEmpty() && p.equals(e.keyHash)) || (!e.markerHash.isEmpty() && p.equals(e.markerHash));
        }
    }

    /* ============================== WALLET / CONTACTS / NOTES ============================== */
    static class Wallet {
        private double bal=0;
        private final Path ledger = Env.DATA_DIR.resolve("wallet_ledger.csv");
        synchronized void credit(double amt, String src){ if (amt<=0) return; bal+=amt; log("CREDIT",amt,src); }
        synchronized boolean debit(double amt, String why){ if (amt<=0 || bal<amt) return false; bal-=amt; log("DEBIT",amt,why); return true; }
        synchronized double balance(){ return bal; }
        private void log(String t, double a, String m){
            try {
                Files.writeString(ledger, Env.nowIso()+";"+t+";"+String.format(Locale.US,"%.2f", a)+";"+m+"\n",
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ignored){}
        }
        synchronized String asJson(){
            return "{\"balance\":"+String.format(Locale.US,"%.2f", bal)+"}";
        }
    }
    static class ContactsRepo {
        String name="", email="", phone="";
        synchronized void set(String n,String e,String p){ name=n==null?"":n; email=e==null?"" : e; phone=p==null?"":p; }
        synchronized String dump(){
            return "{\"name\":\""+esc(name)+"\",\"email\":\""+esc(email)+"\",\"phone\":\""+esc(phone)+"\"}";
        }
        private String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }
    }
    static class NotesRepo {
        private final Path notes = Env.DATA_DIR.resolve("notes.txt");
        synchronized void add(String who, String text){
            try {
                Files.writeString(notes, Env.nowIso()+" ["+who+"]: "+text+"\n",
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ignored){}
        }
    }

    /* ============================== DREAM GLOBE / HOLOGRAM ============================== */
    static class DreamGlobe {
        private final Set<String> apps = new LinkedHashSet<>(List.of(
                "StarMap","Wallet","Notes","Contacts","CO2-Scanner","Hologram-Painter","Energy-Monitor"
        ));
        boolean open=false;
        synchronized boolean openIfAllowed(boolean blessed, double charge){
            if (blessed && charge>=100.0) { open=true; return true; }
            return false;
        }
        synchronized String listAppsJson(){
            StringBuilder sb=new StringBuilder("[");
            boolean first=true;
            for (String a:apps){ if(!first) sb.append(","); first=false; sb.append("\"").append(a).append("\""); }
            sb.append("]");
            return sb.toString();
        }
        synchronized boolean isOpen(){ return open; }
    }

    /* ============================== RITUAL + ENERGY PACK (SIMULATION) ============================== */
    static class EnergyPack {
        double solar=0, plasma=0, levitation=0; // 0..100
        void harvestSolar(double f){ solar = clamp(solar + 3.5*f, 0, 100); }
        void pulsePlasma(double f){ plasma = clamp(plasma + 5.0*f, 0, 100); }
        void liftTick(double f){ levitation = clamp(levitation + 2.5*f, 0, 100); }
        static double clamp(double v,double a,double b){ return Math.max(a, Math.min(b, v)); }
        String json(){ return "{\"solar\":"+round(solar)+",\"plasma\":"+round(plasma)+",\"levitation\":"+round(levitation)+"}"; }
        private String round(double v){ return String.format(Locale.US,"%.1f", v); }
    }
    static class RitualEngine {
        double charge=0;         // 0..100
        boolean blessed=false;   // Inschallah gesprochen
        String last="";
        final MetricsSink metrics;
        final EnergyPack energy;
        RitualEngine(MetricsSink m, EnergyPack e){ metrics=m; energy=e; }
        synchronized double wind(int cycles){
            // wickelnde Kreisbewegung: Solar wird gekoppelt, Ladung steigt
            cycles = Math.max(1, Math.min(1000, cycles));
            for(int i=0;i<cycles;i++){
                energy.harvestSolar(1.0);
                charge = EnergyPack.clamp(charge + 0.8, 0, 100);
            }
            last="wind:"+cycles;
            metrics.count("ritual_wind_calls_total");
            metrics.gauge("ritual_charge", charge);
            return charge;
        }
        synchronized void inschallah(){
            blessed=true;
            energy.pulsePlasma(1.0);
            last="inschallah";
            metrics.count("ritual_inschallah_calls_total");
        }
        synchronized boolean openGlobe(DreamGlobe globe){
            boolean ok = globe.openIfAllowed(blessed, charge);
            last = ok? "globe_opened" : "globe_denied";
            if (ok) metrics.count("globe_open_total"); else metrics.count("globe_denied_total");
            return ok;
        }
        synchronized String json(){
            return "{\"charge\":"+String.format(Locale.US,"%.1f",charge)+",\"blessed\":"+blessed+",\"last\":\""+last+"\"}";
        }
    }

    /* ============================== AIRPODS BRIDGE (GESTURES) ============================== */
    enum Gesture { doubleTap, tripleTap, longPress, tilt }
    static class AirPodsBridge {
        final RitualEngine ritual;
        final EnergyPack energy;
        final MetricsSink metrics;
        AirPodsBridge(RitualEngine r, EnergyPack e, MetricsSink m){ ritual=r; energy=e; metrics=m; }
        synchronized String handle(Gesture g){
            switch (g){
                case doubleTap -> {
                    double c = ritual.wind(12);
                    metrics.count("airpods_doubletap_total");
                    return "wind:charge="+String.format(Locale.US,"%.1f", c);
                }
                case longPress -> {
                    ritual.inschallah();
                    metrics.count("airpods_longpress_total");
                    return "inschallah:ok";
                }
                case tripleTap -> {
                    metrics.count("airpods_tripletap_total");
                    return "tripleTap:ready";
                }
                case tilt -> {
                    energy.liftTick(1.0);
                    metrics.count("airpods_tilt_total");
                    return "levitation:"+String.format(Locale.US,"%.1f", energy.levitation);
                }
            }
            return "noop";
        }
    }

    /* ============================== SESSION / STATE ============================== */
    static class Session {
        final String id;
        final EnergyPack energy = new EnergyPack();
        final MetricsSink metrics;
        final RitualEngine ritual;
        final AirPodsBridge air;
        final DreamGlobe globe = new DreamGlobe();
        String lastGesture="";
        final Deque<String> log = new ArrayDeque<>();

        Session(String id, MetricsSink m){
            this.id=id; this.metrics=m;
            this.ritual = new RitualEngine(m, energy);
            this.air    = new AirPodsBridge(ritual, energy, m);
            note("session:init");
        }
        void note(String s){ if (log.size()>30) log.pollFirst(); log.addLast(Env.nowIso()+" "+s); }
        String json(){
            StringBuilder sb=new StringBuilder();
            sb.append("{\"id\":\"").append(id).append("\",");
            sb.append("\"energy\":").append(energy.json()).append(",");
            sb.append("\"ritual\":").append(ritual.json()).append(",");
            sb.append("\"globeOpen\":").append(globe.isOpen()).append(",");
            sb.append("\"apps\":").append(globe.listAppsJson()).append(",");
            sb.append("\"lastGesture\":\"").append(lastGesture).append("\",");
            sb.append("\"log\":[");
            boolean first=true; for(String l:log){ if(!first) sb.append(","); first=false; sb.append("\"").append(l.replace("\"","\\\"")).append("\""); }
            sb.append("]}");
            return sb.toString();
        }
    }

    /* ============================== APP ============================== */
    private final MetricsSink metrics = new MetricsSink();
    private final UserRegistry users = new UserRegistry();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Wallet wallet = new Wallet();
    private final ContactsRepo contacts = new ContactsRepo();
    private final NotesRepo notes = new NotesRepo();

    public HoloAirPodsSuite(){
        Env.ensureDirs();
        // Beispiel-User vorkonfigurieren:
        // Marker-Hash = dein "NurSeal v3R — Finger-Bundle" (falls du den nutzt)
        String markerHash = "b8d0d1ff1e0d0a207ed2f3eb0f1f2af33138997934796c6850c650f8d4339526".toLowerCase();
        users.register("Sanel Crnkic", "", markerHash);
        // Optional: weitere Nutzer via Datei-Hash (keys/<user>.key) – Anleitung siehe unten
    }

    /* ============================== HTTP HELPERS ============================== */
    private static Map<String,String> query(URI uri){
        Map<String,String> m=new HashMap<>();
        String q=uri.getRawQuery(); if(q==null) return m;
        for(String p:q.split("&")){
            if(p.isEmpty()) continue;
            String[] kv=p.split("=",2);
            String k = urlDecode(kv[0]);
            String v = kv.length>1 ? urlDecode(kv[1]) : "";
            m.put(k, v);
        }
        return m;
    }
    private static String urlDecode(String s){
        try { return URLDecoder.decode(s, StandardCharsets.UTF_8); }
        catch (Exception e){ return s; }
    }
    private static String body(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()){
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    private boolean requireAuth(HttpExchange ex){
        // Für schreibende Endpunkte nötig (POST). GET /metrics ist offen.
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) return true;
        String user = ex.getRequestHeaders().getFirst("X-User");
        String auth = ex.getRequestHeaders().getFirst("X-Auth");
        boolean ok = users.check(user, auth);
        if(!ok){
            reply(ex, 401, "{\"error\":\"unauthorized\"}");
            return false;
        }
        return true;
    }
    private static void reply(HttpExchange ex, int code, String json){
        try {
            byte[] b = json.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            ex.sendResponseHeaders(code, b.length);
            try(OutputStream os = ex.getResponseBody()){ os.write(b); }
        } catch (IOException ignored){}
    }
    private static void replyText(HttpExchange ex, int code, String text, String ctype){
        try {
            byte[] b = text.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", ctype);
            ex.sendResponseHeaders(code, b.length);
            try(OutputStream os = ex.getResponseBody()){ os.write(b); }
        } catch (IOException ignored){}
    }
    private Session getOrCreateSession(String id){
        return sessions.computeIfAbsent(id==null||id.isBlank()? "vk-02": id, sid -> {
            Session s = new Session(sid, metrics);
            metrics.count("sessions_created_total");
            return s;
        });
    }

    /* ============================== HTTP ROUTER ============================== */
    private void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if ("/metrics".equals(path)) { replyText(ex, 200, metrics.asPrometheus(), "text/plain; version=0.0.4"); return; }
        if ("/healthz".equals(path)) { replyText(ex, 200, "ok", "text/plain"); return; }

        Map<String,String> q = query(ex.getRequestURI());
        switch (path){
            case "/api/session/start" -> {
                if (!requireAuth(ex)) return;
                Session s = getOrCreateSession(q.get("session"));
                s.note("session:start device="+q.getOrDefault("device","?"));
                reply(ex, 200, s.json());
            }
            case "/api/state" -> {
                Session s = getOrCreateSession(q.get("session"));
                reply(ex, 200, s.json());
            }
            case "/api/ritual/wind" -> {
                if (!requireAuth(ex)) return;
                Session s = getOrCreateSession(q.get("session"));
                int cycles = Integer.parseInt(q.getOrDefault("cycles","12"));
                double c = s.ritual.wind(cycles);
                s.note("ritual:wind cycles="+cycles);
                metrics.gauge("energy_solar", s.energy.solar);
                reply(ex, 200, "{\"charge\":"+String.format(Locale.US,"%.1f", c)+"}");
            }
            case "/api/ritual/inschallah" -> {
                if (!requireAuth(ex)) return;
                Session s = getOrCreateSession(q.get("session"));
                s.ritual.inschallah();
                s.note("ritual:inschallah");
                metrics.gauge("energy_plasma", s.energy.plasma);
                reply(ex, 200, "{\"blessed\":true}");
            }
            case "/api/ritual/openGlobe" -> {
                if (!requireAuth(ex)) return;
                Session s = getOrCreateSession(q.get("session"));
                boolean ok = s.ritual.openGlobe(s.globe);
                s.note("ritual:openGlobe ok="+ok);
                reply(ex, ok?200:409, "{\"open\":"+ok+",\"apps\":"+s.globe.listAppsJson()+"}");
            }
            case "/api/airpods/gesture" -> {
                if (!requireAuth(ex)) return;
                Session s = getOrCreateSession(q.get("session"));
                String g = q.getOrDefault("g","doubleTap");
                Gesture gg = Gesture.valueOf(g);
                String res = s.air.handle(gg);
                s.lastGesture=g; s.note("airpods:"+g);
                // tripleTap → automatischer Versuch Globe öffnen
                if (gg==Gesture.tripleTap){
                    boolean ok = s.ritual.openGlobe(s.globe);
                    if (ok) s.note("globe:auto-open");
                }
                metrics.gauge("energy_levitation", s.energy.levitation);
                reply(ex, 200, "{\"result\":\""+res+"\",\"state\":"+s.json()+"}");
            }
            case "/api/contacts/get" -> {
                reply(ex, 200, contacts.dump());
            }
            case "/api/contacts/set" -> {
                if (!requireAuth(ex)) return;
                contacts.set(q.get("name"), q.get("email"), q.get("phone"));
                reply(ex, 200, contacts.dump());
            }
            case "/api/wallet/balance" -> {
                reply(ex, 200, wallet.asJson());
            }
            case "/api/wallet/credit" -> {
                if (!requireAuth(ex)) return;
                double amount = Double.parseDouble(q.getOrDefault("amount","0"));
                wallet.credit(amount, "api");
                reply(ex, 200, wallet.asJson());
            }
            case "/api/wallet/debit" -> {
                if (!requireAuth(ex)) return;
                double amount = Double.parseDouble(q.getOrDefault("amount","0"));
                boolean ok = wallet.debit(amount, "api");
                reply(ex, ok?200:409, "{\"ok\":"+ok+",\"balance\":"+String.format(Locale.US,"%.2f", wallet.balance())+"}");
            }
            case "/api/notes/add" -> {
                if (!requireAuth(ex)) return;
                String who = ex.getRequestHeaders().getFirst("X-User");
                String text = q.getOrDefault("text", body(ex));
                notes.add(who==null?"anon":who, text==null?"":text);
                reply(ex, 200, "{\"ok\":true}");
            }
            case "/api/users/register" -> {
                // POST /api/users/register?user=Name  (Body = Rohdaten/Key, z.B. Datei-Inhalt)
                if (!requireAuth(ex)) return; // nur bestehender Admin kann neue hinzufügen – simpel
                String user = q.get("user");
                if (user==null || user.isBlank()){ reply(ex, 400, "{\"error\":\"user required\"}"); return; }
                String b = body(ex);
                String keyHash = Hash.hexSha256(b.getBytes(StandardCharsets.UTF_8));
                users.register(user, keyHash, "");
                reply(ex, 200, "{\"user\":\""+user+"\",\"keyHash\":\""+keyHash+"\"}");
            }
            default -> reply(ex, 404, "{\"error\":\"not found\"}");
        }
    }

    /* ============================== SERVER ============================== */
    public void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("HoloAirPodsSuite läuft auf http://localhost:"+port);
    }

    /* ============================== MAIN ============================== */
    public static void main(String[] args) throws Exception {
        int port = (args.length>0? Integer.parseInt(args[0]) : 9090);
        HoloAirPodsSuite app = new HoloAirPodsSuite();
        app.startServer(port);
    }
}