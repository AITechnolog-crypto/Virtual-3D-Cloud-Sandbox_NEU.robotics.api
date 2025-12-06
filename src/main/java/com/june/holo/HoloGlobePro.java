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

/* =========================================================================
   HoloGlobePro – Perso-Holo-OS (offline)
   - Personalisierter Hologramm-Globus mit Desktop-/Fenster-Logik (Windows-like)
   - AirPods-Gesten -> Rituale (Wind/Inschallah/Levitation) -> Energie
   - VCPU-Cluster ("brutale CPU"): Cores, GHz, Turbo, Thermik
   - Biometrie (Hash-basiert: Marker/Finger-Bundle, Key, Voice) – KEINE echte DNA!
   - Wallet, Kontakte (mit gpt99@iCloud.com), Notizen
   - Start-Orbit (Launcher) + REST-API + /metrics (Prometheus-Format)
   - Offline by design. Alles lokal unter ./data
   ========================================================================= */
public class HoloGlobePro {

    /* ======================= ENV + UTILS ======================= */
    static class Env {
        static final Path ROOT = Paths.get(".").toAbsolutePath().normalize();
        static final Path DATA = ROOT.resolve("data");
        static final Path KEYS = ROOT.resolve("keys");
        static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        static void ensure(){ try{ Files.createDirectories(DATA); Files.createDirectories(KEYS);}catch(IOException e){throw new UncheckedIOException(e);} }
        static String now(){ return LocalDateTime.now().format(ISO); }
    }
    static class Hash {
        static String sha256(byte[] buf){
            try{
                MessageDigest md=MessageDigest.getInstance("SHA-256");
                byte[] d=md.digest(buf);
                StringBuilder sb=new StringBuilder();
                for(byte b:d) sb.append(String.format("%02x", b));
                return sb.toString();
            }catch(Exception e){throw new RuntimeException(e);}    }
    }

    /* ======================= METRICS ======================= */
    static class Metrics {
        private final Map<String, Long> ctr = new ConcurrentHashMap<>();
        private final Map<String, Double> g = new ConcurrentHashMap<>();
        void inc(String k){ ctr.merge(k,1L,Long::sum); }
        void gauge(String k, double v){ g.put(k,v); }
        String prom(){
            StringBuilder sb=new StringBuilder();
            for(var e:ctr.entrySet()){ sb.append("# TYPE ").append(e.getKey()).append(" counter\n").append(e.getKey()).append(" ").append(e.getValue()).append("\n");}
            for(var e:g.entrySet()){ sb.append("# TYPE ").append(e.getKey()).append(" gauge\n").append(e.getKey()).append(" ").append(String.format(Locale.US,"%.3f",e.getValue())).append("\n");}
            return sb.toString();
        }
    }

    /* ======================= SECURITY / BIOMETRY (offline) ======================= */
    static class UserRegistry {
        static class Entry { final String keyHash, markerHash, voiceHash;
            Entry(String k,String m,String v){ keyHash=nz(k); markerHash=nz(m); voiceHash=nz(v);}    
            private static String nz(String s){ return s==null?"":s.toLowerCase(); }
        }
        private final Map<String, Entry> users = new ConcurrentHashMap<>();
        void register(String user, String keyHash, String markerHash, String voiceHash){ users.put(user, new Entry(keyHash, markerHash, voiceHash)); }
        boolean check(String user, String auth, String voice){
            Entry e = users.get(user);
            if (e==null) return false;
            String a = auth==null?"":auth.toLowerCase();
            String v = voice==null?"":voice.toLowerCase();
            boolean keyOk   = !e.keyHash.isEmpty()    && a.equals(e.keyHash);
            boolean markOk  = !e.markerHash.isEmpty() && a.equals(e.markerHash);
            boolean voiceOk = e.voiceHash.isEmpty() || v.equals(e.voiceHash); // Voice optional
            return (keyOk || markOk) && voiceOk;
        }
    }

    /* ======================= WALLET / CONTACTS / NOTES ======================= */
    static class Wallet {
        private double bal=0;
        private final Path ledger=Env.DATA.resolve("wallet.csv");
        synchronized void credit(double x,String src){ if(x>0){ bal+=x; log("CREDIT",x,src);} }
        synchronized boolean debit(double x,String why){ if(x<=0||x>bal) return false; bal-=x; log("DEBIT",x,why); return true; }
        synchronized double balance(){ return bal; }
        private void log(String t,double a,String m){
            try{ Files.writeString(ledger, Env.now()+";"+t+";"+String.format(Locale.US,"%.2f",a)+";"+m+"\n",StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(IOException ignored){}
        }
        String json(){ return "{\"balance\":"+String.format(Locale.US,"%.2f",bal)+"}"; }
    }
    static class Contacts {
        String name="Sanel Crnkic", email="gpt99@iCloud.com", phone="";
        synchronized void set(String n,String e,String p){ if(n!=null)name=n; if(e!=null)email=e; if(p!=null)phone=p; }
        synchronized String json(){ return "{\"name\":\""+esc(name)+"\",\"email\":\""+esc(email)+"\",\"phone\":\""+esc(phone)+"\"}"; }
        private String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }
    }
    static class Notes {
        private final Path f = Env.DATA.resolve("notes.txt");
        synchronized void add(String who,String text){
            try{ Files.writeString(f, Env.now()+" ["+(who==null?"anon":who)+"]: "+(text==null?"":text)+"\n",StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(IOException ignored){}
        }
    }

    /* ======================= ENERGY / RITUAL / AIRPODS ======================= */
    static class Energy {
        double solar=0, plasma=0, lev=0;  // 0..100
        static double clamp(double v){ return Math.max(0, Math.min(100, v)); }
        void addSolar(double f){ solar=clamp(solar+3.5*f); }
        void addPlasma(double f){ plasma=clamp(plasma+5.0*f); }
        void addLev(double f){ lev=clamp(lev+2.5*f); }
        boolean spend(double need){ // nimmt anteilig aus solar/plasma
            double pool = solar + plasma;
            if (pool < need) return false;
            double ratioSolar = solar / pool;
            double useSolar = need * ratioSolar, usePlasma = need - useSolar;
            solar = clamp(solar - useSolar); plasma = clamp(plasma - usePlasma);
            return true;
        }
        String json(){ return "{\"solar\":"+fmt(solar)+",\"plasma\":"+fmt(plasma)+",\"levitation\":"+fmt(lev)+"}"; }
        private String fmt(double v){ return String.format(Locale.US,"%.1f",v); }
    }
    static class Ritual {
        double charge=0; boolean blessed=false; String last="";
        final Metrics m; final Energy e;
        Ritual(Metrics m, Energy e){ this.m=m; this.e=e; }
        synchronized double wind(int loops){ loops=Math.max(1,Math.min(1000,loops)); for(int i=0;i<loops;i++){ e.addSolar(1.0); charge=Math.min(100, charge+0.8);} m.inc("ritual_wind_total"); last="wind:"+loops; m.gauge("ritual_charge",charge); return charge; }
        synchronized void inschallah(){ blessed=true; e.addPlasma(1.0); last="inschallah"; m.inc("ritual_inschallah_total"); }
        synchronized String state(){ return "{\"charge\":"+String.format(Locale.US,"%.1f",charge)+",\"blessed\":"+blessed+",\"last\":\""+last+"\"}"; }
    }
    enum Gesture { doubleTap, tripleTap, longPress, tilt }
    static class AirPods {
        final Ritual r; final Energy e; final Metrics m;
        final Map<Gesture,String> map = new EnumMap<>(Gesture.class);
        AirPods(Ritual r, Energy e, Metrics m){
            this.r=r; this.e=e; this.m=m;
            map.put(Gesture.doubleTap, "wind");
            map.put(Gesture.longPress, "inschallah");
            map.put(Gesture.tripleTap, "openGlobe");
            map.put(Gesture.tilt, "lev");
        }
        synchronized String handle(Gesture g){
            String action = map.getOrDefault(g,"wind");
            switch(action){
                case "wind" -> { m.inc("airpods_doubletap_total"); double c=r.wind(12); return "wind charge="+String.format(Locale.US,"%.1f",c); }
                case "inschallah" -> { m.inc("airpods_longpress_total"); r.inschallah(); return "blessed"; }
                case "lev" -> { m.inc("airpods_tilt_total"); e.addLev(1.0); return "lev="+String.format(Locale.US,"%.1f",e.lev); }
                case "openGlobe" -> { m.inc("airpods_tripletap_total"); return "try-open"; }
                case "openStart" -> { m.inc("airpods_openstart_total"); return "open-start"; }
                default -> { return "noop"; }
            }
        }
    }

    /* ======================= VCPU ("brutale CPU") ======================= */
    static class VCPU {
        int cores=64; double baseGHz=3.2, turboGHz=6.4; boolean turbo=false; double tempC=38.0; double load=0.0;
        long ticks=0; double lastOps=0; // MOp/s
        final Metrics m; final Energy e;
        VCPU(Metrics m, Energy e){ this.m=m; this.e=e; }
        synchronized void setTurbo(boolean on){ turbo=on; }
        synchronized void setLoad(double l){ load=Math.max(0,Math.min(1,l)); }
        synchronized void tick(long ms){
            double ghz = turbo? turboGHz : baseGHz;
            double watts = (20 + cores*ghz*0.9) * (0.4 + 0.6*load);
            double need = (watts * ms/1000.0) / 25.0;
            boolean ok = e.spend(need);
            if(!ok){ turbo=false; ghz = baseGHz*0.7; load=Math.min(load,0.6); need=need*0.5; e.spend(need); }
            tempC = Math.max(28.0, tempC + (watts*0.02) - (e.lev*0.01));
            if (tempC>82.0){ turbo=false; load=Math.min(load,0.7); }
            double ops = cores*ghz*1e3*load; // (Pseudo) MOp/s
            lastOps = ops;
            ticks += ms;
            m.gauge("vcpu_temp_c", tempC);
            m.gauge("vcpu_ops_mops", ops);
            m.gauge("vcpu_load", load);
            m.gauge("vcpu_turbo", turbo?1:0);
        }
        synchronized String json(){
            return "{\"cores\":"+cores+",\"baseGHz\":"+fmt(baseGHz)+",\"turboGHz\":"+fmt(turboGHz)+",\"turbo\":"+turbo+
                   ",\"tempC\":"+fmt(tempC)+",\"load\":"+fmt(load)+",\"opsMops\":"+fmt(lastOps)+"}";
        }
        private String fmt(double v){ return String.format(Locale.US,"%.2f", v); }
    }

    /* ======================= HOLOGRAM GLOBE: DESKTOP/WINDOWS ======================= */
    static class HoloWindow {
        final String id = UUID.randomUUID().toString();
        String title; String app; int orbit=1; int x=0,y=0,w=640,h=420; boolean focused=true;
        HoloWindow(String title,String app){ this.title=title; this.app=app; }
        String json(){ return "{\"id\":\""+id+"\",\"title\":\""+esc(title)+"\",\"app\":\""+esc(app)+"\",\"orbit\":"+orbit+",\"x\":"+x+",\"y\":"+y+",\"w\":"+w+",\"h\":"+h+",\"focused\":"+focused+"}"; }
        private String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }

        void setPos(int nx, int ny){ this.x = clamp(nx, -4000, 4000); this.y = clamp(ny, -4000, 4000); }
        void setSize(int nw, int nh){ this.w = clamp(nw, 180, 3840); this.h = clamp(nh, 140, 2160); }
        void setOrbit(int ring){ this.orbit = clamp(ring, 1, 3); }
        void setFocused(){ this.focused=true; }
        private int clamp(int v,int lo,int hi){ return Math.max(lo, Math.min(hi, v)); }
    }
    static class HoloDesktop {
        boolean globusOpen=false; final Map<String,HoloWindow> wins=new LinkedHashMap<>();

        synchronized boolean open(boolean blessed, double charge, boolean creator){ // Creator Mode senkt Hürde
            double need = creator? 50.0 : 100.0;
            globusOpen = blessed && charge>=need;
            return globusOpen;
        }
        synchronized HoloWindow newWindow(String title, String app){
            HoloWindow w = new HoloWindow(title, app);
            w.orbit = 1 + (wins.size()%3);
            wins.put(w.id, w); return w;
        }
        synchronized boolean close(String id){ return wins.remove(id)!=null; }
        synchronized String listJson(){
            StringBuilder sb=new StringBuilder("["); boolean f=true;
            for(HoloWindow w:wins.values()){ if(!f) sb.append(","); f=false; sb.append(w.json()); }
            return sb.append("]").toString();
        }
        synchronized HoloWindow get(String id){ return wins.get(id); }
        private void bringToFront(String id){
            HoloWindow w = wins.remove(id);
            if (w!=null) wins.put(id, w); // LinkedHashMap: reinsert -> oben
        }
        synchronized boolean move(String id, int x, int y){
            HoloWindow w = wins.get(id); if(w==null) return false;
            w.setPos(x,y); bringToFront(id); return true;
        }
        synchronized boolean resize(String id, int w, int h){
            HoloWindow win = wins.get(id); if(win==null) return false;
            win.setSize(w,h); bringToFront(id); return true;
        }
        synchronized boolean focus(String id){
            HoloWindow win=wins.get(id); if(win==null) return false;
            for(HoloWindow o:wins.values()) o.focused=false;
            win.setFocused(); bringToFront(id); return true;
        }
        synchronized boolean setOrbit(String id, int ring){
            HoloWindow win=wins.get(id); if(win==null) return false;
            win.setOrbit(ring); bringToFront(id); return true;
        }
        synchronized int windowCount(){ return wins.size(); }
    }

    /* ======================= START-ORBIT (Launcher) ======================= */
    static class AppEntry {
        final String key; final String title; final String app;
        AppEntry(String key, String title, String app){ this.key=key; this.title=title; this.app=app; }
        String json(){ return "{\"key\":\""+key+"\",\"title\":\""+esc(title)+"\",\"app\":\""+esc(app)+"\"}"; }
        private String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }
    }
    static class AppRegistry {
        private final Map<String,AppEntry> all = new LinkedHashMap<>();
        AppRegistry(){
            register(new AppEntry("wallet","Wallet","Wallet"));
            register(new AppEntry("notes","Notizen","Notes"));
            register(new AppEntry("cpu","CPU Monitor","CPU"));
            register(new AppEntry("contacts","Kontakte","Contacts"));
            register(new AppEntry("starmap","Sternenhimmel","StarMap")); // Offline-App-Stub
            register(new AppEntry("silo","Energie-Silos","Silo"));
        }
        void register(AppEntry e){ all.put(e.key, e); }
        AppEntry get(String key){ return all.get(key); }
        Collection<AppEntry> all(){ return all.values(); }
    }
    static class StartOrbit {
        private final AppRegistry reg;
        private final LinkedHashMap<String,AppEntry> pinned = new LinkedHashMap<>();
        StartOrbit(AppRegistry reg){
            this.reg=reg;
            pin("wallet"); pin("notes"); pin("cpu"); pin("starmap");
        }
        boolean pin(String key){ AppEntry e=reg.get(key); if(e==null) return false; pinned.put(key,e); return true; }
        boolean unpin(String key){ return pinned.remove(key)!=null; }
        String listJson(){
            StringBuilder sb=new StringBuilder("["); boolean f=true;
            for(AppEntry e:pinned.values()){ if(!f) sb.append(","); f=false; sb.append(e.json()); }
            sb.append("]"); return sb.toString();
        }
        HoloWindow launch(String key, HoloDesktop desk){
            AppEntry e = pinned.getOrDefault(key, reg.get(key));
            if (e==null) return null;
            return desk.newWindow(e.title, e.app);
        }
    }

    /* ======================= SESSION / PROFILE ======================= */
    static class Profile {
        final String owner; boolean creatorMode=false; String theme="Hirsch-Crest Dark";
        Profile(String owner){ this.owner=owner; }
        String json(){ return "{\"owner\":\""+owner+"\",\"creatorMode\":"+creatorMode+",\"theme\":\""+theme+"\"}"; }
    }
    static class Session {
        final String id;
        final Energy energy=new Energy();
        final Metrics metrics;
        final Ritual ritual;
        final AirPods air;
        final VCPU vcpu;
        final HoloDesktop desk=new HoloDesktop();
        final Profile profile;
        final Deque<String> log = new ArrayDeque<>();
        final AppRegistry appReg = new AppRegistry();
        final StartOrbit start = new StartOrbit(appReg);

        Session(String id, String owner, Metrics m){
            this.id=id; this.metrics=m;
            this.ritual=new Ritual(m,energy);
            this.air=new AirPods(ritual,energy,m);
            this.vcpu=new VCPU(m,energy);
            this.profile=new Profile(owner);
            note("session:init");
        }
        void note(String s){ if(log.size()>50) log.pollFirst(); log.addLast(Env.now()+" "+s); }
        String json(){
            StringBuilder sb=new StringBuilder();
            sb.append("{\"id\":\"").append(id).append("\",")
              .append("\"energy\":").append(energy.json()).append(",")
              .append("\"ritual\":").append(ritual.state()).append(",")
              .append("\"vcpu\":").append(vcpu.json()).append(",")
              .append("\"globusOpen\":").append(desk.globusOpen).append(",")
              .append("\"windows\":").append(desk.listJson()).append(",")
              .append("\"profile\":").append(profile.json()).append(",")
              .append("\"startPinned\":").append(start.listJson()).append(",")
              .append("\"log\":[");
            boolean f=true; for(String l:log){ if(!f) sb.append(","); f=false; sb.append("\"").append(l.replace("\"","\\\"")).append("\""); }
            sb.append("]}");
            return sb.toString();
        }
    }

    /* ======================= APP ROOT ======================= */
    private final Metrics metrics = new Metrics();
    private final UserRegistry users = new UserRegistry();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Wallet wallet = new Wallet();
    private final Contacts contacts = new Contacts();
    private final Notes notes = new Notes();

    public HoloGlobePro(){
        Env.ensure();
        // Dein Marker-Hash (Finger-Bundle) ist vorregistriert:
        String markerHash = "b8d0d1ff1e0d0a207ed2f3eb0f1f2af33138997934796c6850c650f8d4339526".toLowerCase();
        users.register("Sanel Crnkic", "", markerHash, ""); // Voice optional leer
    }

    /* ======================= HTTP HELPERS ======================= */
    private static Map<String,String> query(URI u){
        Map<String,String> m=new HashMap<>(); String q=u.getRawQuery(); if(q==null) return m;
        for(String p:q.split("&")){ if(p.isEmpty()) continue; String[] kv=p.split("=",2);
            String k=url(kv[0]); String v=kv.length>1?url(kv[1]):""; m.put(k,v); }
        return m;
    }
    private static String url(String s){ try{ return URLDecoder.decode(s, StandardCharsets.UTF_8);}catch(Exception e){return s;} }
    private static String readBody(HttpExchange ex) throws IOException{ try(InputStream in=ex.getRequestBody()){ return new String(in.readAllBytes(), StandardCharsets.UTF_8);} }
    private static void json(HttpExchange ex, int code, String j){
        try{ byte[] b=j.getBytes(StandardCharsets.UTF_8); ex.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
            ex.sendResponseHeaders(code,b.length); try(OutputStream os=ex.getResponseBody()){ os.write(b);} }catch(IOException ignored){}
    }
    private static void text(HttpExchange ex, int code, String t, String ct){
        try{ byte[] b=t.getBytes(StandardCharsets.UTF_8); ex.getResponseHeaders().set("Content-Type",ct);
            ex.sendResponseHeaders(code,b.length); try(OutputStream os=ex.getResponseBody()){ os.write(b);} }catch(IOException ignored){}
    }
    private boolean auth(HttpExchange ex){
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) return true; // GET offen (außer sensible)
        String u=ex.getRequestHeaders().getFirst("X-User");
        String a=ex.getRequestHeaders().getFirst("X-Auth"); // Hash
        String v=ex.getRequestHeaders().getFirst("X-Voice"); // Voice-Hash optional
        boolean ok = users.check(u, a, v);
        if(!ok) json(ex,401,"{\"error\":\"unauthorized\"}");
        return ok;
    }
    private Session session(String id, String owner){
        return sessions.computeIfAbsent((id==null||id.isBlank())?"vk-02":id, k->{
            metrics.inc("sessions_total");
            return new Session(k, owner==null?"Sanel Crnkic":owner, metrics);
        });
    }

    /* ======================= ROUTER ======================= */
    private void handle(HttpExchange ex) throws IOException {
        String p = ex.getRequestURI().getPath();
        Map<String,String> q = query(ex.getRequestURI());

        if ("/metrics".equals(p)){ text(ex,200,metrics.prom(),"text/plain; version=0.0.4"); return; }
        if ("/healthz".equals(p)){ text(ex,200,"ok","text/plain"); return; }

        switch (p){
            /* ---- Sessions / State ---- */
            case "/api/session/start" -> {
                if(!auth(ex)) return;
                Session s = session(q.get("session"), ex.getRequestHeaders().getFirst("X-User"));
                s.note("session:start device="+q.getOrDefault("device","?"));
                metrics.gauge("windows_open", s.desk.windowCount());
                json(ex,200,s.json());
            }
            case "/api/state" -> { Session s=session(q.get("session"), null); json(ex,200,s.json()); }
            case "/api/snapshot/save" -> {
                if(!auth(ex)) return;
                Session s = session(q.get("session"), null);
                Path f = Env.DATA.resolve("snapshot_"+s.id+".json");
                Files.writeString(f, s.json(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                json(ex,200,"{\"ok\":true,\"file\":\""+f.toString().replace("\\","/")+"\"}");
            }

            /* ---- Biometry ---- */
            case "/api/biometry/enroll" -> { // POST body = raw secret -> SHA-256
                if(!auth(ex)) return;
                String user = q.getOrDefault("user","Sanel Crnkic");
                String type = q.getOrDefault("type","key"); // key|marker|voice
                String body = readBody(ex);
                String h = Hash.sha256(body.getBytes(StandardCharsets.UTF_8)).toLowerCase();
                // Einfachheit: bestehendes Entry ersetzen (Demo)
                String curMarker = "b8d0d1ff1e0d0a207ed2f3eb0f1f2af33138997934796c6850c650f8d4339526";
                String key="", marker=curMarker, voice="";
                if ("key".equals(type)) key=h; else if("marker".equals(type)) marker=h; else if("voice".equals(type)) voice=h;
                users.register(user, key, marker, voice);
                json(ex,200,"{\"user\":\""+user+"\",\"type\":\""+type+"\",\"hash\":\""+h+"\"}");
            }

            /* ---- Ritual & AirPods ---- */
            case "/api/ritual/wind" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                int loops=Integer.parseInt(q.getOrDefault("loops","36"));
                double c=s.ritual.wind(loops); s.note("wind:"+loops);
                metrics.gauge("energy_solar", s.energy.solar);
                json(ex,200,"{\"charge\":"+String.format(Locale.US,"%.1f",c)+"}");
            }
            case "/api/ritual/inschallah" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                s.ritual.inschallah(); s.note("inschallah");
                metrics.gauge("energy_plasma", s.energy.plasma);
                json(ex,200,"{\"blessed\":true}");
            }
            case "/api/airpods/gesture" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                Gesture g=Gesture.valueOf(q.getOrDefault("g","doubleTap"));
                String r=s.air.handle(g); s.note("airpods:"+g);
                if ("try-open".equals(r) || "open-start".equals(r)){
                    boolean ok=s.desk.open(s.ritual.blessed, s.ritual.charge, s.profile.creatorMode);
                    if(ok) s.note("globus:opened");
                }
                metrics.gauge("energy_lev", s.energy.lev);
                metrics.gauge("windows_open", s.desk.windowCount());
                json(ex,200,"{\"result\":\""+r+"\",\"state\":"+s.json()+"}");
            }
            case "/api/airpods/map" -> { // POST ?gesture=doubleTap&action=wind|inschallah|openGlobe|openStart|lev
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                Gesture g=Gesture.valueOf(q.getOrDefault("gesture","doubleTap"));
                String a=q.getOrDefault("action","wind");
                s.air.map.put(g,a); s.note("airpods:map "+g+"->"+a);
                json(ex,200,"{\"ok\":true}");
            }

            /* ---- Globe / Desktop ---- */
            case "/api/globe/open" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok = s.desk.open(s.ritual.blessed, s.ritual.charge, s.profile.creatorMode);
                s.note("globe:open => "+ok);
                json(ex, ok?200:409, "{\"open\":"+ok+"}");
            }
            case "/api/globe/window/new" -> { // POST ?title=...&app=Wallet
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                HoloWindow w=s.desk.newWindow(q.getOrDefault("title","Fenster"), q.getOrDefault("app","Notes"));
                s.note("win:new "+w.title);
                metrics.gauge("windows_open", s.desk.windowCount());
                json(ex,200,w.json());
            }
            case "/api/globe/window/list" -> { Session s=session(q.get("session"), null); json(ex,200,s.desk.listJson()); }
            case "/api/globe/window/close" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok=s.desk.close(q.get("id")); s.note("win:close "+ok);
                metrics.gauge("windows_open", s.desk.windowCount());
                json(ex, ok?200:404, "{\"ok\":"+ok+"}");
            }
            case "/api/globe/window/move" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok=s.desk.move(q.get("id"),
                        Integer.parseInt(q.getOrDefault("x","0")),
                        Integer.parseInt(q.getOrDefault("y","0")));
                s.note("win:move "+ok);
                json(ex, ok?200:404, "{\"ok\":"+ok+"}");
            }
            case "/api/globe/window/resize" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok=s.desk.resize(q.get("id"),
                        Integer.parseInt(q.getOrDefault("w","800")),
                        Integer.parseInt(q.getOrDefault("h","500")));
                s.note("win:resize "+ok);
                json(ex, ok?200:404, "{\"ok\":"+ok+"}");
            }
            case "/api/globe/window/focus" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok=s.desk.focus(q.get("id")); s.note("win:focus "+ok);
                json(ex, ok?200:404, "{\"ok\":"+ok+"}");
            }
            case "/api/globe/window/orbit" -> {
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok=s.desk.setOrbit(q.get("id"), Integer.parseInt(q.getOrDefault("ring","1")));
                s.note("win:orbit "+ok);
                json(ex, ok?200:404, "{\"ok\":"+ok+"}");
            }

            /* ---- Start-Orbit ---- */
            case "/api/start/list" -> { Session s=session(q.get("session"), null); json(ex,200,s.start.listJson()); }
            case "/api/start/launch" -> { // POST ?app=cpu (nutzt pinned, fällt sonst auf Registry zurück)
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                String app=q.getOrDefault("app","notes");
                HoloWindow w=s.start.launch(app, s.desk);
                if (w==null){ json(ex,404,"{\"error\":\"unknown_app\"}"); break; }
                s.note("start:launch "+app);
                metrics.gauge("windows_open", s.desk.windowCount());
                json(ex,200,w.json());
            }
            case "/api/start/pin" -> { // POST ?app=...
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok=s.start.pin(q.getOrDefault("app",""));
                s.note("start:pin "+ok);
                json(ex, ok?200:404, "{\"ok\":"+ok+",\"pinned\":"+s.start.listJson()+"}");
            }
            case "/api/start/unpin" -> { // POST ?app=...
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                boolean ok=s.start.unpin(q.getOrDefault("app",""));
                s.note("start:unpin "+ok);
                json(ex, ok?200:404, "{\"ok\":"+ok+",\"pinned\":"+s.start.listJson()+"}");
            }

            /* ---- VCPU ---- */
            case "/api/vcpu/state" -> { Session s=session(q.get("session"), null); json(ex,200,s.vcpu.json()); }
            case "/api/vcpu/set" -> { // POST ?load=0..1&turbo=true|false
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                if(q.containsKey("load")) s.vcpu.setLoad(Double.parseDouble(q.get("load")));
                if(q.containsKey("turbo")) s.vcpu.setTurbo(Boolean.parseBoolean(q.get("turbo")));
                s.note("vcpu:set");
                json(ex,200,s.vcpu.json());
            }

            /* ---- Wallet / Contacts / Notes ---- */
            case "/api/wallet/balance" -> { json(ex,200, wallet.json()); }
            case "/api/wallet/credit" -> { if(!auth(ex)) return; double a=Double.parseDouble(q.getOrDefault("amount","0")); wallet.credit(a,"api"); json(ex,200,wallet.json()); }
            case "/api/wallet/debit" -> { if(!auth(ex)) return; double a=Double.parseDouble(q.getOrDefault("amount","0")); boolean ok=wallet.debit(a,"api"); json(ex, ok?200:409, "{\"ok\":"+ok+",\"balance\":"+String.format(Locale.US,"%.2f",wallet.balance())+"}"); }
            case "/api/contacts/get" -> { json(ex,200,contacts.json()); }
            case "/api/contacts/set" -> { if(!auth(ex)) return; contacts.set(q.get("name"), q.get("email"), q.get("phone")); json(ex,200,contacts.json()); }
            case "/api/notes/add" -> { if(!auth(ex)) return; String who=ex.getRequestHeaders().getFirst("X-User"); String body=readBody(ex); notes.add(who, body.isBlank()? q.getOrDefault("text",""): body); json(ex,200,"{\"ok\":true}"); }

            /* ---- Creator Mode (Kun fa yakun) ---- */
            case "/api/creator/enable" -> { // POST Body muss Phrase enthalten
                if(!auth(ex)) return;
                Session s=session(q.get("session"), null);
                String body=readBody(ex);
                boolean ok = body!=null && body.toLowerCase().contains("kun fa ya kun");
                s.profile.creatorMode = ok || s.profile.creatorMode;
                s.note("creatorMode:"+s.profile.creatorMode);
                json(ex, ok?200:409, "{\"creatorMode\":"+s.profile.creatorMode+"}");
            }

            default -> json(ex,404,"{\"error\":\"not_found\"}");
        }
    }

    /* ======================= SERVER LOOP ======================= */
    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        // kleiner VCPU-Scheduler
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(()->{
            for(Session s: sessions.values()) s.vcpu.tick(200);
        }, 200, 200, TimeUnit.MILLISECONDS);
        server.start();
        System.out.println("HoloGlobePro läuft lokal: http://localhost:"+port);
    }

    public static void main(String[] args) throws Exception {
        int port = (args.length>0? Integer.parseInt(args[0]) : 9091);
        new HoloGlobePro().start(port);
    }
}
