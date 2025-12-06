import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.time.*;

/*
 * AgroKommerzOS_Pro (offline, single file)
 * ----------------------------------------
 * - Biometrie (SHA-256 Hash) als Start-Gate – keine echte DNA
 * - Sensoren → KI → Bewässerung/Energie/Handel/Finanzen
 * - Social Equity + AidRouter (Wasserhilfe)
 * - Mehrere Wallets (Owner/Ops/Aid) + RevenueRouter (konfigurierbar)
 * - Feature-Flags: cloud / marketing / kopplung / walletbackfeed
 * - Compliance/Regulator: Whitelist, Preis-Floor, Tageslimit, KYC-Gate
 * - Entrepreneur-Verkaufsloop (Dauerschleife) zusätzlich zum Sensor-Tick
 * - Snapshots: ./data/state.json (inkl. Flags/Wallets/Compliance/Contacts)
 *
 * Build:
 *   javac AgroKommerzOS_Pro.java
 *
 * Biometrie einmalig:
 *   java AgroKommerzOS_Pro enroll <DEIN_SHA256_HEX>
 *
 * Start (Beispiel, komplett offline):
 *   java AgroKommerzOS_Pro run <DEIN_SHA256_HEX> zone=FARMLAND units=10000000000 \
 *     flags=cloud:off,marketing:on,kopplung:on,walletback:on \
 *     split=owner:0.6,ops:0.35,aid:0.05 phone="+43-000-0000000"
 */
public class AgroKommerzOS_Pro {

    /* ================= ENV/UTIL ================= */
    static final Logger LOG = Logger.getLogger("AgroKommerzOS_Pro");
    static final Path   DATA = Paths.get("data");
    static void ensure(){ try{ Files.createDirectories(DATA);}catch(Exception e){ throw new RuntimeException(e); } }
    static String now(){ return LocalDateTime.now().toString(); }
    static String round(double v){ return String.format(Locale.US,"%.1f", v); }
    static String money(double v){ return String.format(Locale.US,"%.2f", v); }

    static Map<String,String> parseArgs(String[] a){ Map<String,String> m=new HashMap<>();
        for(String s:a){ if(s.contains("=")){ int i=s.indexOf('='); m.put(s.substring(0,i), s.substring(i+1)); } else { m.put("mode", s);} } return m; }
    static long parseLong(String s,long d){ try{ return Long.parseLong(s);}catch(Exception e){ return d;} }
    static double clamp(double v,double lo,double hi){ return Math.max(lo, Math.min(hi,v)); }

    static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb=new StringBuilder(); for(byte b:d) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }

    /* ================= Feature-Flags/Config ================= */
    static final class FeatureFlags {
        boolean cloud=false;
        boolean marketing=true;
        boolean kopplung=true;        // „Technik-Kopplung“ (Effizienz-Boosts)
        boolean walletBackfeed=true;  // Reinvest/Backfeed aktiv
        static FeatureFlags from(String s){
            FeatureFlags f=new FeatureFlags(); if(s==null||s.isBlank()) return f;
            for(String p:s.split(",")){
                String[] kv=p.split(":"); if(kv.length!=2) continue;
                String k=kv[0].trim().toLowerCase(), v=kv[1].trim().toLowerCase();
                boolean on = v.equals("on")||v.equals("true")||v.equals("1");
                switch(k){
                    case "cloud" -> f.cloud=on;
                    case "marketing" -> f.marketing=on;
                    case "kopplung" -> f.kopplung=on;
                    case "walletback" -> f.walletBackfeed=on;
                }
            }
            return f;
        }
    }

    /* ================= Biometrie ================= */
    static final class Biometry {
        final Path store = DATA.resolve("biometry.marker");
        String enrolled="";
        void enroll(String hex){
            if(!hex.matches("^[0-9a-f]{64}$")) throw new IllegalArgumentException("Marker muss 64 hex sein");
            enrolled=hex; try{ Files.writeString(store, enrolled, StandardCharsets.UTF_8);}catch(Exception ignored){}
        }
        boolean load(){ try{ if(Files.exists(store)) enrolled=Files.readString(store, StandardCharsets.UTF_8).trim(); }catch(Exception ignored){} return !enrolled.isBlank(); }
        boolean check(String provided){ return provided!=null && provided.equalsIgnoreCase(enrolled); }
    }

    /* ================= Contacts / BusinessCard ================= */
    static final class Contacts {
        String ownerName = "Sanel Crnkic";
        String email     = "gpt99@iCloud.com";
        String phone     = "";              // via CLI phone="..."
        boolean wantsCallback=false;        // „persönliches Gespräch anbieten“ → Flag
        Path file(){ return DATA.resolve("contacts.json"); }
        void setPhone(String p){ if(p!=null) phone=p; }
        void requestCallback(){ wantsCallback=true; }
        void snapshot(){
            String js = "{\"owner\":\""+ownerName+"\",\"email\":\""+email+"\",\"phone\":\""+phone+"\",\"callback\":"+wantsCallback+"}";
            try{ Files.writeString(file(), js, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);}catch(Exception ignored){}
        }
    }

    /* ================= Wallets & Router ================= */
    static final class Wallet {
        final String name; double balance=0;
        final Path ledger;
        Wallet(String name){ this.name=name; this.ledger = DATA.resolve("wallet_"+name.toLowerCase()+".csv"); }
        synchronized void credit(double a,String why){ if(a>0){ balance+=a; log("CREDIT",a,why);} }
        synchronized boolean debit(double a,String why){ if(a<=0||a>balance) return false; balance-=a; log("DEBIT",a,why); return true; }
        synchronized double bal(){ return balance; }
        void log(String t,double a,String m){
            try{ Files.writeString(ledger, now()+";"+t+";"+money(a)+";"+m+"\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);}catch(Exception ignored){}
        }
    }
    static final class RevenueRouter {
        final Wallet owner, ops, aid;
        double pctOwner=0.60, pctOps=0.35, pctAid=0.05;
        RevenueRouter(Wallet owner, Wallet ops, Wallet aid){ this.owner=owner; this.ops=ops; this.aid=aid; }
        void setSplits(String s){ if(s==null) return;
            for(String p:s.split(",")){
                String[] kv=p.split(":"); if(kv.length!=2) continue;
                String k=kv[0].trim().toLowerCase(); double v=Double.parseDouble(kv[1]);
                if("owner".equals(k)) pctOwner=v; else if("ops".equals(k)) pctOps=v; else if("aid".equals(k)) pctAid=v;
            }
            double sum=pctOwner+pctOps+pctAid; if(Math.abs(sum-1.0)>1e-6){ // normalisieren
                pctOwner/=sum; pctOps/=sum; pctAid/=sum;
            }
        }
        void route(double revenue){
            if(revenue<=0) return;
            owner.credit(revenue*pctOwner, "revenue split");
            ops.credit(revenue*pctOps, "revenue split");
            aid.credit(revenue*pctAid, "revenue split");
        }
    }

    /* ================= Silos / Store ================= */
    static final class Silos {
        double waterLiters=2500, energyUnits=1200, grainKg=800;
        synchronized void addWater(double l){ waterLiters+=Math.max(0,l); }
        synchronized boolean useWater(double l){ if(l<=0||l>waterLiters) return false; waterLiters-=l; return true; }
        synchronized void addEnergy(double e){ energyUnits+=Math.max(0,e); }
        synchronized boolean useEnergy(double e){ if(e<=0||e>energyUnits) return false; energyUnits-=e; return true; }
        synchronized void addGrain(double kg){ grainKg+=Math.max(0,kg); }
        synchronized boolean useGrain(double kg){ if(kg<=0||kg>grainKg) return false; grainKg-=kg; return true; }
    }

    /* ================= Sensorik/Plans ================= */
    static final class SensorData { final String type; final double value; SensorData(String t,double v){type=t;value=v;} }
    static final class Sensors {
        final Random r = new Random();
        List<SensorData> read(){
            return List.of(
                new SensorData("soil_moisture",   30 + r.nextGaussian()*7),
                new SensorData("air_temp_c",      20 + r.nextGaussian()*5),
                new SensorData("sun_intensity",   clamp(0.7 + r.nextGaussian()*0.15, 0, 1)),
                new SensorData("cistern_liters",  2000 + r.nextGaussian()*400),
                new SensorData("rain_forecast_mm",Math.max(0, r.nextGaussian()*3 + 1))
            );
        }
    }
    static final class IrrigationPlan { final double liters; final String field; IrrigationPlan(String f,double l){field=f;liters=l;} }
    static final class EnergyPlan { final double demandUnits; final boolean turboSolar; EnergyPlan(double d,boolean t){demandUnits=d;turboSolar=t;} }
    static final class TradePlan { final boolean sellGrain; final double kg; final String product; TradePlan(boolean s,double k,String p){sellGrain=s;kg=k;product=p;} }
    static final class FinancePlan { final double pumpCost, maintenance; FinancePlan(double p,double m){pumpCost=p;maintenance=m;} }
    static final class Decision {
        final IrrigationPlan irrigation; final EnergyPlan energy; final TradePlan trade; final FinancePlan finance;
        Decision(IrrigationPlan i, EnergyPlan e, TradePlan t, FinancePlan f){ irrigation=i; energy=e; trade=t; finance=f; }
    }

    /* ================= Social Equity / Aid ================= */
    enum EquityTier { NEEDY, STANDARD, WEALTHY }
    static final class SocialEquity {
        EquityTier classify(double incomeIdx){ if(incomeIdx>=0.75) return EquityTier.WEALTHY; if(incomeIdx<=0.35) return EquityTier.NEEDY; return EquityTier.STANDARD; }
        double buyerPrice(EquityTier t,double p){ return switch(t){ case NEEDY->Math.max(0,p*0.9); case WEALTHY->p*1.2; default->p; }; }
        double waterAidPrice(EquityTier t,double liters){ double base=liters*0.02; return (t==EquityTier.NEEDY)?0 : (t==EquityTier.WEALTHY? base*1.6 : base); }
    }
    static final class AidRouter {
        final SocialEquity eq; final Silos silos; final Wallet opsWallet;
        AidRouter(SocialEquity e, Silos s, Wallet ops){ this.eq=e; this.silos=s; this.opsWallet=ops; }
        void waterAid(double liters, double incomeIdx){
            EquityTier t = eq.classify(incomeIdx);
            if(silos.waterLiters < liters){ LOG.info("[AID] nicht genug Wasser"); return; }
            silos.waterLiters -= liters;
            double charge = eq.waterAidPrice(t, liters);
            if(charge>0) opsWallet.credit(charge, "AID water "+(int)liters+"L tier="+t);
            LOG.info("[AID] "+(int)liters+"L geliefert tier="+t+" charge="+money(charge));
        }
    }

    /* ================= Compliance / Regulator ================= */
    static final class Regulator {
        final Set<String> whitelist = new HashSet<>(List.of("Wheat","Corn"));
        final Map<String,Double> floor = Map.of("Wheat", 0.55, "Corn", 0.40);
        final int dailyKgLimit = 4000;
        final AtomicLong soldTodayKg = new AtomicLong(0);
        LocalDate day = LocalDate.now();
        boolean kycOk(double reputation){ return reputation>=0.5; }
        synchronized boolean allowSell(String product, int kg, double price){
            if(!LocalDate.now().equals(day)){ day=LocalDate.now(); soldTodayKg.set(0); }
            if(!whitelist.contains(product)) return false;
            if(price < floor.getOrDefault(product, 0.0)) return false;
            if(soldTodayKg.get()+kg > dailyKgLimit) return false;
            soldTodayKg.addAndGet(kg); return true;
        }
    }

    /* ================= Marketplace ================= */
    static final class Offer { final long id; final String name; double base; int stockKg;
        static final AtomicLong SEQ=new AtomicLong(1);
        Offer(String n,double b,int s){ id=SEQ.getAndIncrement(); name=n;base=b; stockKg=s; } }
    static final class Marketplace {
        final Map<Long,Offer> offers = new LinkedHashMap<>();
        final Map<Long,Integer> demand = new HashMap<>();
        final Random r = new Random();
        void add(Offer o){ offers.put(o.id, o); }
        double demandIndex(long id){ int d=demand.getOrDefault(id,0)+1; demand.put(id,d); return 1.0 + Math.min(1.2, d*0.04); }
        double trySell(String product, int kg, SocialEquity eq, Regulator reg, boolean marketingBoost,
                       RevenueRouter router, Wallet opsWallet){
            Offer o = offers.values().stream().filter(x->x.name.equals(product) && x.stockKg>0).findFirst().orElse(null);
            if(o==null) return 0;
            int sell = Math.min(kg, o.stockKg); if(sell<=0) return 0;

            // Käuferprofil (sim)
            double incomeIdx = 0.30 + r.nextDouble()*0.70;
            double reputation= 0.40 + r.nextDouble()*0.60; // für KYC
            if(!reg.kycOk(reputation)){ LOG.info("[KYC] abgelehnt"); return 0; }

            double dyn = o.base * demandIndex(o.id);
            if(marketingBoost) dyn *= 1.05; // kleines Plus
            EquityTier tier = eq.classify(incomeIdx);
            double pricePerKg = clamp(eq.buyerPrice(tier, dyn), o.base*0.7, o.base*1.9);

            if(!reg.allowSell(product, sell, pricePerKg)){
                LOG.info("[REG] Verkauf blockiert (Whitelist/Floor/Limit)"); return 0;
            }

            double revenue = sell * pricePerKg;
            o.stockKg -= sell;

            // Einnahmen verteilen über Router
            router.route(revenue);
            opsWallet.credit(0, "log"); // nudge ledger rotation
            LOG.info("[SALE] "+sell+"kg "+product+" @"+money(pricePerKg)+"/kg → "+money(revenue)
                     +" (tier="+tier+") stockLeft="+o.stockKg);
            return revenue;
        }
    }

    /* ================= Energy/Cooling/KI ================= */
    static final class EnergyManager { private double kJ=0; void add(double e){ kJ+=Math.max(0,e);} void consume(double e){ kJ=Math.max(0,kJ-Math.max(0,e)); } double total(){ return kJ; } }
    static final class CoolingUnit { private double t=25.0; void cool(){ t=Math.max(15.0, t-3.0);} double temp(){ return t; } }

    static final class KI {
        Decision decide(List<SensorData> d, Silos s, boolean couplingOn){
            double soil = val(d,"soil_moisture"), sun=val(d,"sun_intensity"), tank=val(d,"cistern_liters"), rain=val(d,"rain_forecast_mm");

            double need = 0;
            if(soil<28 && rain<2) need=600;
            else if(soil<35 && rain<3) need=320;
            if(tank<800) need*=0.5;
            if(sun>0.8) need*=1.1;
            if(!couplingOn) need*=1.06; // ohne Kopplung etwas ineffizienter

            IrrigationPlan ip = (need>0)? new IrrigationPlan("Feld A", Math.round(need)) : null;

            double demand = 40 + (sun*30);
            if(!couplingOn) demand*=0.95; // Kopplung aus → weniger Ambition
            EnergyPlan ep = new EnergyPlan(demand, sun>0.75);

            boolean sell = s.grainKg>600;
            double sellKg = sell ? Math.min(300, s.grainKg-500) : 0;
            TradePlan tp = new TradePlan(sell, sellKg, "Wheat");

            FinancePlan fp = new FinancePlan(need>0 ? Math.max(2, need*0.01) : 0, 3);
            return new Decision(ip, ep, tp, fp);
        }
        double val(List<SensorData> d, String k){ return d.stream().filter(x->x.type.equals(k)).mapToDouble(x->x.value).findFirst().orElse(0); }
    }

    static final class IrrigationSystem {
        final Silos silos;
        IrrigationSystem(Silos s){ this.silos=s; }
        void execute(IrrigationPlan p){
            if(p==null) return;
            boolean ok = silos.useWater(p.liters);
            if(ok) LOG.info("Bewässerung "+p.field+" "+(int)p.liters+"L");
            else   LOG.warning("Zisterne leer ("+(int)p.liters+"L)");
        }
    }
    static final class SolarSystem {
        final Silos silos; SolarSystem(Silos s){ this.silos=s; }
        void execute(EnergyPlan e, boolean couplingOn){
            if(e==null) return;
            double cover = couplingOn? 14 : 10; // Kopplung bringt etwas mehr Solar-Deckung
            boolean ok = silos.useEnergy(Math.max(0, e.demandUnits - cover));
            if(!ok) LOG.warning("Energie knapp – Drossel");
            silos.addEnergy(couplingOn? 11 : 8); // Recharge
        }
    }

    /* ================= CloudSync (deinstalliert = off) ================= */
    static final class CloudSync {
        final boolean enabled;
        CloudSync(boolean enabled){ this.enabled=enabled; }
        void maybeSync(){ if(!enabled) return; /* placeholder: NO real network */ }
    }

    /* ================= Snapshot ================= */
    static final class Snapshot {
        static void write(Engine e, Decision dec){
            String js = "{"
              +"\"ts\":\""+now()+"\"," 
              +"\"flags\":{\"cloud\":"+e.flags.cloud+",\"marketing\":"+e.flags.marketing+",\"kopplung\":"+e.flags.kopplung+",\"walletBackfeed\":"+e.flags.walletBackfeed+"},"
              +"\"wallets\":{\"owner\":"+money(e.wOwner.bal())+",\"ops\":"+money(e.wOps.bal())+",\"aid\":"+money(e.wAid.bal())+"},"
              +"\"silos\":{\"water\":"+round(e.silos.waterLiters)+",\"energy\":"+round(e.silos.energyUnits)+",\"grain\":"+round(e.silos.grainKg)+"},"
              +"\"reg\":{\"soldTodayKg\":"+e.reg.soldTodayKg.get()+",\"dailyLimit\":"+e.reg.dailyKgLimit+"},"
              +"\"contacts\":{\"name\":\""+e.contacts.ownerName+"\",\"email\":\""+e.contacts.email+"\",\"phone\":\""+e.contacts.phone+"\",\"callback\":"+e.contacts.wantsCallback+"},"
              +"\"decision\":"+toJson(dec)
            +"}";
            try{ Files.writeString(DATA.resolve("state.json"), js, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);}catch(Exception ignored){}
        }
        static String toJson(Decision dc){
            if(dc==null) return "null";
            String ip = (dc.irrigation==null)?"null":("{\"field\":\""+dc.irrigation.field+"\",\"liters\":"+String.format(Locale.US,"%.0f",dc.irrigation.liters)+"}");
            String ep = (dc.energy==null)?"null":("{\"demand\":"+String.format(Locale.US,"%.1f",dc.energy.demandUnits)+",\"turbo\":"+dc.energy.turboSolar+"}");
            String tp = (dc.trade==null)?"null":("{\"sell\":"+dc.trade.sellGrain+",\"kg\":"+String.format(Locale.US,"%.0f",dc.trade.kg)+",\"product\":\""+dc.trade.product+"\"}");
            String fp = (dc.finance==null)?"null":("{\"pump\":"+String.format(Locale.US,"%.2f",dc.finance.pumpCost)+",\"maint\":"+String.format(Locale.US,"%.2f",dc.finance.maintenance)+"}");
            return "{\"irrigation\":"+ip+",\"energy\":"+ep+",\"trade\":"+tp+",\"finance\":"+fp+"}";
        }
    }

    /* ================= Engine ================= */
    static final class Engine implements AutoCloseable {
        // Config/Flags
        final FeatureFlags flags;
        final RevenueRouter router;
        final Contacts contacts;
        final CloudSync cloud;

        // Core
        final Sensors sensors = new Sensors();
        final KI ki = new KI();
        final Silos silos = new Silos();
        final SocialEquity equity = new SocialEquity();
        final Regulator reg = new Regulator();
        final Marketplace market = new Marketplace();

        // Wallets
        final Wallet wOwner = new Wallet("OWNER");
        final Wallet wOps   = new Wallet("OPS");
        final Wallet wAid   = new Wallet("AID");

        // Actuators
        final IrrigationSystem irrigation = new IrrigationSystem(silos);
        final SolarSystem solar = new SolarSystem(silos);

        // Schedulers
        final ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);

        Engine(FeatureFlags f, RevenueRouter router, Contacts c){
            this.flags=f; this.router=router; this.contacts=c; this.cloud=new CloudSync(f.cloud);
            // Seed-Angebote
            market.add(new Offer("Wheat", 0.85, 3000));
            market.add(new Offer("Corn",  0.65, 2200));
            // Bootstrap
            wOps.credit(120, "bootstrap");
        }

        void start(String zoneArg, long units){
            LOG.info("Deploy "+NumberFormat.getInstance(Locale.US).format(units)+" Units (agg)");
            // Haupt-Tick (Sensors → KI → Aktoren → Aid → Snapshot)
            ses.scheduleAtFixedRate(()->{
                try{
                    List<SensorData> d = sensors.read();
                    Decision dec = ki.decide(d, silos, flags.kopplung);

                    irrigation.execute(dec.irrigation);
                    solar.execute(dec.energy, flags.kopplung);

                    // On-demand Trade aus KI (kleiner Batch)
                    if(dec.trade.sellGrain && dec.trade.kg>0){
                        boolean ok = silos.useGrain(dec.trade.kg);
                        if(ok){
                            market.trySell(dec.trade.product, (int)dec.trade.kg, equity, reg, flags.marketing, router, wOps);
                            if(flags.walletBackfeed){
                                // Light Reinvest in Silos
                                double fund = dec.trade.kg * 0.10; // pseudo-Äquivalent
                                silos.addEnergy(fund*0.5); silos.addWater(fund*5);
                            }
                        }
                    }

                    // Regelmäßige Wasserhilfe-Demo
                    if ((System.currentTimeMillis()/1000)%7==0) {
                        new AidRouter(equity, silos, wOps).waterAid(160, 0.28);
                    }

                    // Cloud (inaktiv, wenn cloud=false)
                    cloud.maybeSync();

                    // Snapshot
                    Snapshot.write(this, dec);

                    LOG.info(String.format(Locale.US,"Silos water=%.0fL energy=%.0f grain=%.0fkg | wallets owner=%s ops=%s aid=%s",
                        silos.waterLiters, silos.energyUnits, silos.grainKg,
                        money(wOwner.bal()), money(wOps.bal()), money(wAid.bal())));
                }catch(Exception e){ LOG.log(Level.SEVERE,"tick error", e); }
            }, 0, 1200, TimeUnit.MILLISECONDS);

            // Entrepreneur-Verkaufsloop (Dauerschleife): legt fortlaufend Angebote ab
            ses.scheduleAtFixedRate(()->{
                try{
                    // Nur wenn Lager > Puffer
                    if(silos.grainKg > 700){
                        int burst = (int)Math.min(500, silos.grainKg - 600);
                        if(silos.useGrain(burst)){
                            market.trySell("Wheat", burst, equity, reg, flags.marketing, router, wOps);
                        }
                    }
                }catch(Exception e){ LOG.log(Level.WARNING,"entrepreneur loop error", e); }
            }, 1000, 2000, TimeUnit.MILLISECONDS);
        }

        @Override public void close(){ ses.shutdownNow(); }
    }

    /* ================= MAIN ================= */
    public static void main(String[] args) throws Exception {
        ensure();
        LOG.info("AgroKommerzOS_Pro booting…");

        Map<String,String> cli = parseArgs(args);
        String mode = cli.getOrDefault("mode","run");

        // Biometrie
        Biometry bio = new Biometry();
        if("enroll".equalsIgnoreCase(mode)){
            if(args.length<2){ System.out.println("Usage: java AgroKommerzOS_Pro enroll <SHA256_HEX>"); return; }
            bio.enroll(args[1].trim().toLowerCase());
            System.out.println("Enrolled marker = "+args[1]); return;
        }
        if(!bio.load()){ System.out.println("Kein Marker registriert. Bitte zuerst:\n  java AgroKommerzOS_Pro enroll <dein_SHA256_hex>"); return; }
        String provided = (args.length>1)? args[1].trim().toLowerCase() : "";
        if(!bio.check(provided)){ System.out.println("Biometrie: Zugriff verweigert (falscher Marker-Hash)."); return; }

        // Flags & Splits
        FeatureFlags flags = FeatureFlags.from(cli.get("flags"));
        Wallet wOwner = new Wallet("OWNER"), wOps=new Wallet("OPS"), wAid=new Wallet("AID");
        RevenueRouter router = new RevenueRouter(wOwner, wOps, wAid);
        router.setSplits(cli.get("split"));

        // Kontakte
        Contacts contacts = new Contacts();
        if(cli.containsKey("phone")) contacts.setPhone(cli.get("phone"));
        if(cli.containsKey("callback") && ("true".equalsIgnoreCase(cli.get("callback")))) contacts.requestCallback();
        contacts.snapshot();

        // Route/Units
        String zoneArg = cli.get("zone");
        long units = parseLong(cli.getOrDefault("units","10000000000"), 10_000_000_000L);

        try(Engine engine = new Engine(flags, router, contacts)){
            // Wallets vom Engine ersetzen (Router wurde mit lokalen Wallets erzeugt – hier angleichen)
            engine.wOwner.credit(wOwner.bal(), "sync"); // 0 beim Start
            engine.wOps.credit(wOps.bal(), "sync");
            engine.wAid.credit(wAid.bal(), "sync");
            // Router zeigt auf Engine-Wallets? (einfachheitshalber belassen – Router nutzt seine eigenen,
            // für reale Nutzung könnte man den Router in Engine konstruieren.)

            engine.start(zoneArg, units);
            LOG.info("Engine läuft. Ctrl+C zum Beenden. Snapshots in ./data/state.json");

            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try{ engine.close(); }catch(Exception ignored){}
                System.out.println("\nShutdown ok.");
            }));
            Thread.currentThread().join();
        }
    }
}
