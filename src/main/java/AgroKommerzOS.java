import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;

// =============================================================
// AgroKommerzOS (offline, single file)
//  - Biometrie (Hash-basiert) als Start-Gate (keine echte DNA)
//  - Sensoren → KI-Entscheider → Bewässerung/Energie/Handel/Finanzen
//  - Social Equity (NEEDY/STANDARD/WEALTHY) + AidRouter (Wasser)
//  - Wallet + Silos (Wasser/Energie/Getreide), dynamische Preise
//  - Periodische Ticks, sauberes Shutdown, Snapshots ./data/state.json
// =============================================================
public class AgroKommerzOS {
    // ---------- ENV ----------
    static final Logger LOG = Logger.getLogger("AgroKommerzOS");
    static final Path DATA = Paths.get("data");
    static void ensure() { try { Files.createDirectories(DATA); } catch (Exception e) { throw new RuntimeException(e); } }
    static String now() { return LocalDateTime.now().toString(); }
    static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }

    // ---------- BIOMETRIE (reiner Hash) ----------
    static class Biometry {
        final Path store = DATA.resolve("biometry.marker");
        String enrolled = "";
        void enroll(String hex64){
            if (!hex64.matches("^[0-9a-f]{64}$")) throw new IllegalArgumentException("marker muss 64 hex sein");
            enrolled = hex64;
            try { Files.writeString(store, enrolled, StandardCharsets.UTF_8); } catch (Exception ignored) {}
        }
        boolean load(){
            try { if (Files.exists(store)) enrolled = Files.readString(store, StandardCharsets.UTF_8).trim(); } catch(Exception ignored){}
            return !enrolled.isBlank();
        }
        boolean check(String providedHex){ return providedHex != null && !enrolled.isBlank() && providedHex.equalsIgnoreCase(enrolled); }
    }

    // ---------- WALLET / SILOS ----------
    static class Wallet {
        double balance;
        final Path ledger = DATA.resolve("wallet.csv");
        synchronized void credit(double x, String why){ if (x>0){ balance+=x; log("CREDIT", x, why);} }
        synchronized boolean debit(double x, String why){ if(x<=0||x>balance) return false; balance-=x; log("DEBIT", x, why); return true; }
        synchronized double bal(){ return balance; }
        void log(String t,double a,String m){ try{ Files.writeString(ledger, now()+";"+t+";"+String.format(Locale.US,"%.2f",a)+";"+m+"\n",StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception ignored){} }
    }
    static class Silos {
        double waterLiters = 2500;     // Zisterne
        double energyUnits = 1200;     // pseudo Einheiten
        double grainKg     = 800;      // Lager
        synchronized void addWater(double l){ waterLiters += Math.max(0,l); }
        synchronized boolean useWater(double l){ if(l<=0 || l>waterLiters) return false; waterLiters -= l; return true; }
        synchronized void addEnergy(double e){ energyUnits += Math.max(0,e); }
        synchronized boolean useEnergy(double e){ if(e<=0 || e>energyUnits) return false; energyUnits -= e; return true; }
        synchronized void addGrain(double kg){ grainKg += Math.max(0,kg); }
        synchronized boolean useGrain(double kg){ if(kg<=0 || kg>grainKg) return false; grainKg -= kg; return true; }
    }

    // ---------- SENSORIK ----------
    static class SensorData {
        final String type; final double value; SensorData(String t,double v){ type=t; value=v; }
        public String toString(){ return type+"="+String.format(Locale.US,"%.2f",value); }
    }
    static class Sensors {
        final Random r = new Random();
        List<SensorData> read(){
            // Bodenfeuchte (%), Lufttemp (°C), Sonnenintensität (0..1), Tankstand (L), Regenprognose (mm)
            return List.of(
              new SensorData("soil_moisture",   30 + r.nextGaussian()*7),    // kann <20% fallen
              new SensorData("air_temp_c",      20 + r.nextGaussian()*5),
              new SensorData("sun_intensity",   Math.min(1, Math.max(0, 0.7 + r.nextGaussian()*0.15))),
              new SensorData("cistern_liters",  2000 + r.nextGaussian()*400),
              new SensorData("rain_forecast_mm",Math.max(0, r.nextGaussian()*3 + 1))
            );
        }
    }

    // ---------- PLÄNE ----------
    static class IrrigationPlan { final double liters; final String field; IrrigationPlan(String field,double liters){ this.field=field; this.liters=liters; } }
    static class EnergyPlan { final double demandUnits; final boolean turboSolar; EnergyPlan(double d, boolean t){ demandUnits=d; turboSolar=t; } }
    static class TradePlan { final boolean sellGrain; final double kg; final String product; TradePlan(boolean s,double kg,String p){ sellGrain=s; this.kg=kg; product=p; } }
    static class FinancePlan { final double pumpCost; final double maintenance; FinancePlan(double p,double m){ pumpCost=p; maintenance=m; } }
    static class Decision {
        final IrrigationPlan irrigation; final EnergyPlan energy; final TradePlan trade; final FinancePlan finance;
        Decision(IrrigationPlan i, EnergyPlan e, TradePlan t, FinancePlan f){ irrigation=i; energy=e; trade=t; finance=f; }
    }

    // ---------- SOCIAL EQUITY ----------
    enum EquityTier { NEEDY, STANDARD, WEALTHY }
    static class SocialEquityEngine {
        EquityTier classifyBuyer(double incomeIdx){
            if (incomeIdx >= 0.75) return EquityTier.WEALTHY;
            if (incomeIdx <= 0.35) return EquityTier.NEEDY;
            return EquityTier.STANDARD;
        }
        double buyerPrice(EquityTier t, double p){
            return switch(t){
                case NEEDY -> Math.max(0, p*0.9);
                case WEALTHY -> p*1.2;
                default -> p;
            };
        }
        double waterAidPrice(EquityTier t, double liters){
            double base = liters*0.02; // Energie/OpEx-Äquivalent
            return (t==EquityTier.NEEDY) ? 0 : (t==EquityTier.WEALTHY? base*1.6 : base);
        }
    }

    // ---------- MARKT ----------
    static class Offer {
        static final AtomicLong SEQ = new AtomicLong(1);
        final long id = SEQ.getAndIncrement();
        final String name; double basePricePerKg; int stockKg;
        Offer(String name,double base,int stockKg){ this.name=name; this.basePricePerKg=base; this.stockKg=stockKg; }
    }
    static class Marketplace {
        final Map<Long,Offer> offers = new LinkedHashMap<>();
        final Map<Long,Integer> demand = new HashMap<>();
        final Random r = new Random();
        void add(Offer o){ offers.put(o.id,o); }
        double demandIndex(long id){ int d=demand.getOrDefault(id,0)+1; demand.put(id,d); return 1.0 + Math.min(1.2, d*0.04); }
        static double clamp(double v,double lo,double hi){ return Math.max(lo, Math.min(hi,v)); }

        // simuliert Verkauf an Abnehmer (mit Equity)
        double trySell(String product, double kg, SocialEquityEngine eq, Wallet wallet){
            Offer o = offers.values().stream().filter(x->x.name.equals(product) && x.stockKg>0).findFirst().orElse(null);
            if(o==null) return 0;
            int sell = (int)Math.min(kg, o.stockKg);
            if (sell<=0) return 0;
            double incomeIdx = 0.3 + r.nextDouble()*0.7; // zufälliger Käufer
            EquityTier tier = eq.classifyBuyer(incomeIdx);
            double dyn = o.basePricePerKg * demandIndex(o.id);
            double price = eq.buyerPrice(tier, dyn);
            price = clamp(price, o.basePricePerKg*0.7, o.basePricePerKg*1.9);
            double revenue = sell * price;
            o.stockKg -= sell;
            wallet.credit(revenue, "SALE "+product+" "+sell+"kg @"+String.format(Locale.US,"%.2f",price)+"/kg tier="+tier);
            return revenue;
        }
    }

    // ---------- AID (Wasser für Bedürftige) ----------
    static class AidRouter {
        final SocialEquityEngine eq;
        final Silos silos;
        AidRouter(SocialEquityEngine e, Silos s){ this.eq=e; this.silos=s; }
        double handleWaterAid(double liters, double incomeIdx, Wallet wallet){
            EquityTier t = eq.classifyBuyer(incomeIdx);
            double charge = eq.waterAidPrice(t, liters);
            boolean ok = silos.useWater(liters);
            if(!ok){ LOG.info("Aid: nicht genug Wasser"); return 0; }
            if (charge>0) wallet.credit(charge, "AID water "+(int)liters+"L tier="+t);
            LOG.info("Aid: "+(int)liters+"L geliefert tier="+t+" charge="+String.format(Locale.US,"%.2f",charge));
            return charge;
        }
    }

    // ---------- AKTOREN ----------
    static class IrrigationSystem {
        final Silos silos;
        IrrigationSystem(Silos s){ this.silos=s; }
        void execute(IrrigationPlan p){
            if(p==null) return;
            boolean ok = silos.useWater(p.liters);
            if(ok) LOG.info("Bewässerung "+p.field+" "+(int)p.liters+"L");
            else LOG.warning("Zisterne leer für Bewässerung ("+(int)p.liters+"L)");
        }
    }
    static class SolarSystem {
        final Silos silos; SolarSystem(Silos s){ this.silos=s; }
        void execute(EnergyPlan e){
            if(e==null) return;
            // demand units aus Energie-Silo ziehen, Solar füllt leicht nach
            boolean ok = silos.useEnergy(Math.max(0,e.demandUnits-12)); // 12 units deckt Solar
            if(!ok) LOG.warning("Energie knapp – Drosselung aktiv");
            silos.addEnergy(10); // Solar-Recharge
        }
    }

    // ---------- KI ----------
    static class KI {
        Decision decide(List<SensorData> d, Silos silos){
            double soil = val(d,"soil_moisture");
            double sun  = val(d,"sun_intensity");
            double tank = val(d,"cistern_liters");
            double rain = val(d,"rain_forecast_mm");

            // Bewässerung: wenn trocken (<35), mehr; wenn Regen > 3mm, weniger / none
            double needLiters = 0;
            if (soil < 28 && rain < 2) needLiters = 600;
            else if (soil < 35 && rain < 3) needLiters = 320;

            if (tank < 800) needLiters *= 0.5; // sparen
            if (sun > 0.8) needLiters *= 1.1;  // mehr Verdunstung → etwas mehr

            IrrigationPlan ip = (needLiters>0) ? new IrrigationPlan("Feld A", Math.round(needLiters)) : null;

            // Energie: Last abhängig von Sonne/Temperatur -> Pumpen & IT
            double demand = 40 + (sun*30); // 40..70 units
            EnergyPlan ep = new EnergyPlan(demand, sun>0.75);

            // Handel: wenn Kornlager > 600 kg → schiebe 150..300 kg in Markt
            boolean sell = silos.grainKg > 600;
            double sellKg = sell ? Math.min(300, silos.grainKg-500) : 0;
            TradePlan tp = new TradePlan(sell, sellKg, "Wheat");

            // Finanzen: Pumpen- und Instandhaltung
            FinancePlan fp = new FinancePlan(needLiters>0? Math.max(2, needLiters*0.01): 0, 3);

            return new Decision(ip, ep, tp, fp);
        }
        double val(List<SensorData> d, String key){
            return d.stream().filter(x->x.type.equals(key)).mapToDouble(x->x.value).findFirst().orElse(0);
        }
    }

    // ---------- PAYMENT ----------
    static class PaymentProcessor {
        final Wallet wallet; final Silos silos;
        PaymentProcessor(Wallet w, Silos s){ this.wallet=w; this.silos=s; }
        void applyFinance(FinancePlan f){
            if(f==null) return;
            double tot = f.pumpCost + f.maintenance;
            if (tot<=0) return;
            boolean ok = wallet.debit(tot, "Ops");
            if(!ok) LOG.warning("Wallet leer für Ops ("+String.format(Locale.US,"%.2f",tot)+")");
        }
        void routeRevenue(double revenue){
            if (revenue<=0) return;
            // 70% Wallet, 30% reinvest (Energie oder Wasseraufbereitung)
            double toFund = revenue*0.30;
            silos.addEnergy(toFund*0.5);
            silos.addWater(toFund*5);
            LOG.info("Reinvest: +Energy "+String.format(Locale.US,"%.1f",toFund*0.5)+" +Water "+String.format(Locale.US,"%.1f",toFund*5));
        }
    }

    // ---------- SNAPSHOT ----------
    static class Snapshot {
        static void write(Silos s, Wallet w, List<SensorData> d, Decision dec) {
            String json = "{"
              + "\"ts\":\""+now()+"\","
              + "\"wallet\":"+String.format(Locale.US,"%.2f", w.bal())+"," 
              + "\"silos\":{"
                + "\"water\":"+String.format(Locale.US,"%.1f", s.waterLiters)+"," 
                + "\"energy\":"+String.format(Locale.US,"%.1f", s.energyUnits)+"," 
                + "\"grain\":"+String.format(Locale.US,"%.1f", s.grainKg)
              + "},"
              + "\"sensors\":"+toJsonSensors(d)+"," 
              + "\"decision\":"+toJsonDecision(dec)
            + "}";
            try { Files.writeString(DATA.resolve("state.json"), json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); } catch(Exception ignored){}
        }
        static String toJsonSensors(List<SensorData> d){
            StringBuilder sb=new StringBuilder("["); boolean f=true;
            for(var s:d){ if(!f) sb.append(","); f=false; sb.append("{\"t\":\"").append(s.type).append("\",\"v\":").append(String.format(Locale.US,"%.2f",s.value)).append("}"); }
            return sb.append("]").toString();
        }
        static String toJsonDecision(Decision dc){
            if(dc==null) return "null";
            String ip = (dc.irrigation==null)?"null":("{\"field\":\""+dc.irrigation.field+"\",\"liters\":"+String.format(Locale.US,"%.0f",dc.irrigation.liters)+"}");
            String ep = (dc.energy==null)?"null":("{\"demand\":"+String.format(Locale.US,"%.1f",dc.energy.demandUnits)+",\"turbo\":"+dc.energy.turboSolar+"}");
            String tp = (dc.trade==null)?"null":("{\"sell\":"+dc.trade.sellGrain+",\"kg\":"+String.format(Locale.US,"%.0f",dc.trade.kg)+",\"product\":\""+dc.trade.product+"\"}");
            String fp = (dc.finance==null)?"null":("{\"pump\":"+String.format(Locale.US,"%.2f",dc.finance.pumpCost)+",\"maint\":"+String.format(Locale.US,"%.2f",dc.finance.maintenance)+"}");
            return "{\"irrigation\":"+ip+",\"energy\":"+ep+",\"trade\":"+tp+",\"finance\":"+fp+"}";
        }
    }

    // ---------- ENGINE ----------
    static class Engine implements AutoCloseable {
        final Sensors sensors = new Sensors();
        final KI ki = new KI();
        final Silos silos = new Silos();
        final Wallet wallet = new Wallet();
        final SocialEquityEngine equity = new SocialEquityEngine();
        final Marketplace market = new Marketplace();
        final AidRouter aid = new AidRouter(equity, silos);
        final IrrigationSystem irrigation = new IrrigationSystem(silos);
        final SolarSystem solar = new SolarSystem(silos);
        final PaymentProcessor pay = new PaymentProcessor(wallet, silos);
        final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        volatile boolean running = true;

        Engine(){
            market.add(new Offer("Wheat", 0.85, 1000));
            market.add(new Offer("Corn",  0.65, 800));
            wallet.credit(120, "bootstrap");
        }

        void start(){
            ses.scheduleAtFixedRate(this::tick, 0, 1200, TimeUnit.MILLISECONDS);
        }

        void tick(){
            try{
                List<SensorData> d = sensors.read();
                Decision dec = ki.decide(d, silos);

                // Aktoren
                irrigation.execute(dec.irrigation);
                solar.execute(dec.energy);

                // Handel
                if (dec.trade.sellGrain && dec.trade.kg>0){
                    boolean ok = silos.useGrain(dec.trade.kg);
                    if (ok){
                        double rev = market.trySell(dec.trade.product, dec.trade.kg, equity, wallet);
                        pay.routeRevenue(rev);
                    }else{
                        LOG.info("Nicht genug Korn für Verkauf");
                    }
                }

                // Aid (Beispiel: jede 5. Runde ~ bedürftiger Haushalt)
                if (System.currentTimeMillis()/1000 % 5 == 0){
                    double incomeIdx = 0.25; // needy
                    aid.handleWaterAid(180, incomeIdx, wallet);
                }

                // Finanzen
                pay.applyFinance(dec.finance);

                // Snapshot
                Snapshot.write(silos, wallet, d, dec);

                // Log Status
                LOG.info(String.format(Locale.US,"Silos water=%.0fL energy=%.0f grain=%.0fkg wallet=%.2f",
                        silos.waterLiters, silos.energyUnits, silos.grainKg, wallet.bal()));
            }catch(Exception e){
                LOG.log(Level.SEVERE, "Tick error", e);
            }
        }

        @Override public void close(){ running=false; ses.shutdownNow(); }
    }

    // ---------- MAIN ----------
    public static void main(String[] args) throws Exception {
        ensure();
        LOG.info("AgroKommerzOS booting…");

        // --- Biometrie-Gate ---
        Biometry bio = new Biometry();
        bio.load();
        String cmd = (args.length>0)? args[0] : "";
        if ("enroll".equalsIgnoreCase(cmd)){
            if (args.length<2) {
                System.out.println("Usage: java AgroKommerzOS enroll <SHA256_HEX>");
                return;
            }
            bio.enroll(args[1].trim().toLowerCase());
            System.out.println("Enrolled marker = "+args[1]);
            return;
        }
        if (bio.enrolled.isBlank()){
            System.out.println("Kein Marker registriert. Bitte zuerst:");
            System.out.println("  java AgroKommerzOS enroll <dein_SHA256_hex>");
            return;
        }
        String provided = (args.length>1)? args[1].trim().toLowerCase() : "";
        if (!bio.check(provided)){
            System.out.println("Biometrie: Zugriff verweigert (falscher Marker-Hash).");
            return;
        }

        // --- Engine starten ---
        try(Engine engine = new Engine()){
            engine.start();
            LOG.info("Engine läuft. Ctrl+C zum Beenden. Snapshots in ./data/state.json");
            // Block main-thread
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try{ engine.close(); }catch(Exception ignored){}
                System.out.println("\nShutdown ok.");
            }));
            // park
            Thread.currentThread().join();
        }
    }
}
