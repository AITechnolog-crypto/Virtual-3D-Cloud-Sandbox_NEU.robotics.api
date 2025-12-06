import java.util.*;
import java.text.NumberFormat;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * GreenBoots_BioModules_Pro – Swarmbots "Tarngrillen" + Commerce/Aid (offline)
 * ---------------------------------------------------------------------------
 * - Beinhaltet: deine Bio-Module (Smart-Bins, Crop-Guardian, Reef-Microlab, …)
 * - NEU: Biometrie-Gate (SHA-256 Hash), Wallet, Marketplace, Social Equity,
 *        Aid-Router (Wasser), Snapshots -> ./data/green_state.json
 * - Edge-Privacy (On-Device), Policy-Gates (No-Fly/Conflict), O(1) Aggregation
 * - Simuliertes Harvesting: Pflanze/Erde, Bienen-Vibration, Kinetik, Solar, Welle
 *
 * Start (offline):
 *   javac GreenBoots_BioModules_Pro.java
 *   java GreenBoots_BioModules_Pro enroll <DEIN_SHA256_HEX>
 *   java GreenBoots_BioModules_Pro run <DEIN_SHA256_HEX> zone=FARMLAND units=10000000000
 *
 * Sicherheit/Etik: Simulation/Read-Only. Realbetrieb nur mit Einwilligung, Recht,
 * Behördenkoordination & Mensch-im-Loop. Kein Offense-Code.
 */
public class GreenBoots_BioModules_Pro {

    /*=========================== ENV/UTIL ===========================*/
    static final Path DATA = Paths.get("data");
    static void ensure(){ try{ Files.createDirectories(DATA);}catch(Exception e){ throw new RuntimeException(e); } }
    static String round(double v){ return String.format(Locale.US,"%.1f",v); }
    static Map<String,String> parseArgs(String[] a){ Map<String,String> m=new HashMap<>(); for(String s:a){ if(s.contains("=")){int i=s.indexOf('='); m.put(s.substring(0,i), s.substring(i+1));} else { m.put("mode",s);} } return m; }
    static long parseLong(String s,long d){ try{ return Long.parseLong(s);}catch(Exception e){ return d; } }
    static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(); for(byte b:d) sb.append(String.format("%02x", b)); return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }

    /*=========================== BIOMETRY ===========================*/
    static final class Biometry {
        final Path store = DATA.resolve("marker.sha256");
        String enrolled="";
        void enroll(String hex){ if(!hex.matches("^[0-9a-f]{64}$")) throw new IllegalArgumentException("Marker muss 64 hex sein"); enrolled=hex; try{ Files.writeString(store, enrolled); }catch(Exception ignored){} }
        boolean load(){ try{ if(Files.exists(store)) enrolled=Files.readString(store).trim(); }catch(Exception ignored){} return !enrolled.isBlank(); }
        boolean check(String provided){ return provided!=null && provided.equalsIgnoreCase(enrolled); }
    }

    /*=========================== CORE STATE ===========================*/
    static final class EnergyManager { private double kJ=0; void add(double x){ kJ+=Math.max(0,x);} void consume(double x){ kJ=Math.max(0,kJ-Math.max(0,x));} double total(){ return kJ; } }
    static final class CoolingUnit { private double t=25.0; void cool(){ t=Math.max(15.0,t-3.0);} double temp(){ return t; } }
    static final class SwarmField {
        private final long units; private double stability=0.0;
        SwarmField(long u){ units=Math.max(100,u);} 
        void deploy(){ System.out.println("[SWARM] Deploy "+NumberFormat.getInstance(Locale.US).format(units)+" Units"); }
        void ensurePlatform(){ double step=Math.min(0.08+Math.log10(units)*0.005,0.2); stability=Math.min(1.0, stability+step); System.out.println("[SWARM] Plattform "+(int)(stability*100)+"%"); }
    }
    static final class Audit {
        private final Deque<String> last=new ArrayDeque<>();
        synchronized void log(String who,String what){ String row=new Date()+" | "+who+" | "+what; last.addLast(row); if(last.size()>80) last.removeFirst(); System.out.println("[AUDIT] "+row); }
        synchronized List<String> tail(){ return new ArrayList<>(last); }
    }
    static final class Metrics {
        private final Map<String,Long> m=new LinkedHashMap<>();
        void inc(String k){ m.put(k, m.getOrDefault(k,0L)+1); }
        void add(String k,long v){ m.put(k, m.getOrDefault(k,0L)+v); }
        Map<String,Long> all(){ return m; }
        void print(){ for(var e:m.entrySet()) System.out.println(e.getKey()+": "+e.getValue()); }
    }
    static final class EdgePrivacy { boolean detectPattern(String kind,double sens){ return Math.random()<sens; } }

    /*=========================== ZONES/POLICY ===========================*/
    static final class Position { final double lat,lon; Position(double la,double lo){lat=la;lon=lo;} public String toString(){ return "lat="+lat+", lon="+lon; } }
    static final class ZoneResolver {
        String zoneTagFor(Position p){
            double lat=p.lat, lon=p.lon;
            if (near(lat,47.5,13.0)) return "FOREST";
            if (near(lat,53.55,9.99)) return "COAST";
            if (near(lat,47.95,12.30))return "WETLAND";
            if (near(lat,50.00,8.80)) return "FARMLAND";
            if (near(lat,48.135,11.582)) return "CITY";
            if (lat>48.20 && lat<48.25 && lon>16.50 && lon<16.70) return "NOFLY_TFR";
            return "CITY";
        }
        boolean near(double a,double b,double lonRef){ return Math.abs(a-b)<0.3; }
    }
    static final class RecoveryPolicy {
        boolean allowStart(String zone){ if(zone.startsWith("NOFLY")) return false; if(zone.startsWith("CONFLICT")) return false; return true; }
    }

    /*=========================== HARVESTERS ===========================*/
    static final class GreenHarvesters {
        private final Random r; GreenHarvesters(Random r){ this.r=r; }
        double harvestPlantMicrobialKJ(String zone){ double wh = zone.equals("FARMLAND")? (0.2+0.2*r.nextDouble()) : (0.05+0.15*r.nextDouble()); return wh*3.6; }
        double harvestHiveVibrationKJ(int intensity){ double mWh=(0.6+0.4*r.nextDouble())*Math.max(1,intensity); return (mWh/1000.0)*3.6; }
        double harvestKineticBinKJ(){ double mWh=1.5+1.0*r.nextDouble(); return (mWh/1000.0)*3.6; }
        double harvestSolarKJ(String zone){ double wh = switch(zone){ case "FARMLAND"->0.8+r.nextDouble()*0.8; case "COAST"->0.6+r.nextDouble()*0.7; case "FOREST"->0.2+r.nextDouble()*0.4; default->0.3+r.nextDouble()*0.5; }; return wh*3.6; }
        double harvestWaveKJ(){ double wh=0.4+r.nextDouble()*0.6; return wh*3.6; }
    }

    /*=========================== MODULE HUB ===========================*/
    interface BioModule { String id(); String[] preferZones(); void run(Context c, Position here, String zone); }
    static final class Context {
        final EnergyManager energy; final SwarmField swarm; final CoolingUnit cooling; final Audit audit; final Metrics metrics; final EdgePrivacy edge;
        final ZoneResolver zones; final RecoveryPolicy policy; final GreenHarvesters harvest;
        // PRO: Commerce/Aid/State
        final Wallet wallet; final Marketplace market; final SocialEquity equity; final AidRouter aid;
        final FarmStore store;
        Context(EnergyManager e, SwarmField s, CoolingUnit c, Audit a, Metrics m, EdgePrivacy ed, ZoneResolver z, RecoveryPolicy p, GreenHarvesters h,
                Wallet w, Marketplace mk, SocialEquity eq, AidRouter ar, FarmStore fs){
            this.energy=e; this.swarm=s; this.cooling=c; this.audit=a; this.metrics=m; this.edge=ed; this.zones=z; this.policy=p; this.harvest=h;
            this.wallet=w; this.market=mk; this.equity=eq; this.aid=ar; this.store=fs;
        }
    }
    static final class ExtensionHub {
        private final Context ctx; private final List<BioModule> mods=new ArrayList<>();
        ExtensionHub(Context ctx){ this.ctx=ctx; }
        void register(BioModule m){ mods.add(m); ctx.audit.log("ext","reg: "+m.id()+" @"+Arrays.toString(m.preferZones())); }
        void autorun(Position here, String zone){ for(BioModule m:mods) if(matches(m.preferZones(),zone)) m.run(ctx, here, zone); }
        boolean matches(String[] zs,String z){ if(zs==null||zs.length==0) return true; for(String s:zs) if(s.equalsIgnoreCase(z)) return true; return false; }
    }

    /*=========================== EXISTING MODULES (kurz) ===========================*/
    static final class PollinatorGuardianModule implements BioModule {
        public String id(){ return "POLLINATOR_GUARDIAN"; }
        public String[] preferZones(){ return new String[]{"FARMLAND","CITY"}; }
        public void run(Context c, Position here, String zone){
            boolean varroa = c.edge.detectPattern("hive_audio", 0.08);
            if(varroa){ c.metrics.inc("varroa_flags"); c.audit.log(id(),"Varroa Frühwarnung @"+here); }
            int gaps = zone.equals("CITY")?2:1;
            if(gaps>0){ c.metrics.add("bloom_gap_spots", gaps); c.audit.log(id(),"Blühdefizit="+gaps); }
            double kJ = c.harvest.harvestHiveVibrationKJ(gaps); c.energy.add(kJ);
            // PRO: minimaler Bonus-Ertrag bei guter Bestäuber-Lage
            if(zone.equals("FARMLAND")) c.store.grainKg += 0.8*gaps;
        }
    }
    static final class ForestGuardianModule implements BioModule {
        public String id(){ return "FOREST_GUARDIAN"; }
        public String[] preferZones(){ return new String[]{"FOREST"}; }
        public void run(Context c, Position here, String zone){
            if(c.edge.detectPattern("chainsaw_audio", 0.12)){ c.metrics.inc("chainsaw_alerts"); c.audit.log(id(),"Kettensägen-Akustik → Ranger-Alarm (sim)"); }
        }
    }
    static final class BirdAndBatSafeModule implements BioModule {
        public String id(){ return "BIRD_BAT_SAFE"; }
        public String[] preferZones(){ return new String[]{"CITY","FOREST"}; }
        public void run(Context c, Position here, String zone){
            if(Math.random()<0.10){ c.metrics.inc("bat_activity_events"); c.audit.log(id(),"Fledermaus → Rotor-Reduktion (sim)"); }
        }
    }
    static final class BlueCorridorModule implements BioModule {
        public String id(){ return "BLUE_CORRIDOR"; }
        public String[] preferZones(){ return new String[]{"COAST"}; }
        public void run(Context c, Position here, String zone){
            if(c.edge.detectPattern("whale_acoustic", 0.07)){ c.metrics.inc("whale_detections"); c.audit.log(id(),"Walgesang → AIS-Hinweis (sim)"); }
        }
    }
    static final class TurtleAndGhostNetModule implements BioModule {
        public String id(){ return "TURTLE_GHOSTNET"; }
        public String[] preferZones(){ return new String[]{"COAST"}; }
        public void run(Context c, Position here, String zone){
            if(Math.random()<0.06){ c.metrics.inc("ghost_nets_spotted"); }
        }
    }
    static final class AmphibianHelperModule implements BioModule {
        public String id(){ return "AMPHIBIAN_HELPER"; }
        public String[] preferZones(){ return new String[]{"WETLAND"}; }
        public void run(Context c, Position here, String zone){
            if(Math.random()<0.05){ c.metrics.inc("edna_alerts"); }
        }
    }
    static final class PalletSentinelModule implements BioModule {
        public String id(){ return "PALLET_SENTINEL"; }
        public String[] preferZones(){ return new String[]{"CITY","FARMLAND","COAST"}; }
        public void run(Context c, Position here, String zone){ if(Math.random()<0.07){ c.metrics.inc("pallet_quarantines"); } }
    }

    /*=========================== DEINE NEUEN MODULE ===========================*/
    static final class SmartBinsModule implements BioModule {
        public String id(){ return "SMART_BINS"; }
        public String[] preferZones(){ return new String[]{"CITY","FOREST"}; }
        public void run(Context c, Position here, String zone){
            if(c.edge.detectPattern("ursid_presence",0.10)){
                c.metrics.inc("bear_contacts_prevented");
                c.audit.log(id(),"Bärensichere Verriegelung + Geruchsreduzierung (sim)");
                c.energy.add(c.harvest.harvestKineticBinKJ());
            }
        }
    }
    static final class CropGuardianModule implements BioModule {
        public String id(){ return "CROP_GUARDIAN"; }
        public String[] preferZones(){ return new String[]{"FARMLAND"}; }
        public void run(Context c, Position here, String zone){
            boolean pathActive = c.edge.detectPattern("wild_path_activity", 0.18);
            if(pathActive){ c.metrics.inc("wildlife_corridors_guided"); c.audit.log(id(),"Leit-Cues → Korridor um Feld (sim)"); }
            if(Math.random()<0.20){ c.metrics.inc("buffer_strip_recos"); c.audit.log(id(),"Empfehlung: Blüh-/Pufferstreifen (sim)"); }
            c.energy.add(c.harvest.harvestPlantMicrobialKJ(zone));
            // PRO: etwas Feld-Ertrag je Tick (Demo)
            c.store.grainKg += 3.5; // ~3.5 kg pro Schritt (Demo)
        }
    }
    static final class ReefMicrolabModule implements BioModule {
        public String id(){ return "REEF_MICROLAB"; }
        public String[] preferZones(){ return new String[]{"COAST"}; }
        public void run(Context c, Position here, String zone){
            if(Math.random()<0.16){ c.metrics.inc("reef_heat_flags"); c.audit.log(id(),"Thermal-Flag → Mikro-Schatten (sim)"); }
            if(Math.random()<0.10){ c.metrics.inc("reef_ph_flags");   c.audit.log(id(),"pH-Flag → Meldung (sim)"); }
            c.energy.add(c.harvest.harvestWaveKJ());
        }
    }

    /*=========================== COMMERCE / AID ===========================*/
    static final class Wallet {
        double balance=0; final Path ledger = DATA.resolve("wallet.csv");
        synchronized void credit(double a,String why){ if(a>0){ balance+=a; log("CREDIT",a,why);} }
        synchronized boolean debit(double a,String why){ if(a<=0||a>balance) return false; balance-=a; log("DEBIT",a,why); return true; }
        private void log(String t,double a,String m){ try{ Files.writeString(ledger, new Date()+";"+t+";"+String.format(Locale.US,"%.2f",a)+";"+m+"\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);}catch(Exception ignored){} }
    }
    enum EquityTier { NEEDY, STANDARD, WEALTHY }
    static final class SocialEquity {
        EquityTier classify(double incomeIdx){ if(incomeIdx>=0.75) return EquityTier.WEALTHY; if(incomeIdx<=0.35) return EquityTier.NEEDY; return EquityTier.STANDARD; }
        double buyerPrice(EquityTier t,double p){ return switch(t){ case NEEDY->Math.max(0,p*0.9); case WEALTHY->p*1.2; default->p; }; }
        double waterAidPrice(EquityTier t,double liters){ double base=liters*0.02; return (t==EquityTier.NEEDY)?0 : (t==EquityTier.WEALTHY? base*1.6 : base); }
    }
    static final class Marketplace {
        static final class Offer { final String name; double basePerKg; int stockKg; Offer(String n,double b,int s){name=n;basePerKg=b;stockKg=s;} }
        final List<Offer> offers=new ArrayList<>(); final Random r=new Random(); final Map<String,Integer> demand=new HashMap<>();
        void add(Offer o){ offers.add(o); }
        double demandIndex(String name){ int d=demand.getOrDefault(name,0)+1; demand.put(name,d); return 1.0+Math.min(1.2, d*0.04); }
        double sell(String product, double kg, SocialEquity eq, Wallet wallet){
            Offer o = offers.stream().filter(x->x.name.equals(product)&&x.stockKg>0).findFirst().orElse(null); if(o==null) return 0;
            int sell=(int)Math.min(kg,o.stockKg); if(sell<=0) return 0;
            double incomeIdx = 0.3 + r.nextDouble()*0.7; EquityTier tier = eq.classify(incomeIdx);
            double dyn = o.basePerKg * demandIndex(o.name);
            double price = eq.buyerPrice(tier, dyn); price = Math.max(o.basePerKg*0.7, Math.min(price, o.basePerKg*1.9));
            double revenue = sell*price; o.stockKg-=sell; wallet.credit(revenue, "SALE "+product+" "+sell+"kg @"+String.format(Locale.US,"%.2f",price)+"/kg tier="+tier);
            return revenue;
        }
    }
    static final class AidRouter {
        final SocialEquity eq; final FarmStore store; final Wallet wallet;
        AidRouter(SocialEquity e, FarmStore s, Wallet w){ this.eq=e; this.store=s; this.wallet=w; }
        void waterAid(double liters, double incomeIdx){
            EquityTier t = eq.classify(incomeIdx);
            if(store.waterLiters < liters){ System.out.println("[AID] nicht genug Wasser"); return; }
            store.waterLiters -= liters;
            double charge = eq.waterAidPrice(t, liters);
            if(charge>0) wallet.credit(charge, "AID water "+(int)liters+"L tier="+t);
            System.out.println("[AID] "+(int)liters+"L geliefert tier="+t+" charge="+String.format(Locale.US,"%.2f",charge));
        }
    }
    static final class FarmStore {
        double grainKg=800, waterLiters=2500, energyUnits=1200;
        void reinvest(double revenue){ if(revenue<=0) return; double fund=revenue*0.30; energyUnits+=fund*0.5; waterLiters+=fund*5; }
    }

    /*=========================== SNAPSHOT ===========================*/
    static void snapshot(Context c, String zone, int tick){
        String json = "{" 
          + "\"tick\":"+tick+"," 
          + "\"zone\":\""+zone+"\"," 
          + "\"energy_kJ\":"+round(c.energy.total())+"," 
          + "\"temp_C\":"+round(c.cooling.temp())+"," 
          + "\"store\":{\"grainKg\":"+round(c.store.grainKg)+",\"waterL\":"+round(c.store.waterLiters)+",\"energyU\":"+round(c.store.energyUnits)+"}," 
          + "\"wallet\":"+String.format(Locale.US,"%.2f", c.wallet.balance)+"," 
          + "\"metrics\":{"+ c.metrics.all().entrySet().stream()
                  .map(e->"\""+e.getKey()+"\":"+e.getValue()).reduce((a,b)->a+","+b).orElse("") +"}"
        + "}";
        try{ Files.writeString(DATA.resolve("green_state.json"), json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);}catch(Exception ignored){}
    }

    /*=========================== RUN LOOP ===========================*/
    public static void main(String[] args) {
        ensure();
        Map<String,String> cli = parseArgs(args);
        String mode = cli.getOrDefault("mode","run");

        // --- Biometrie ---
        Biometry bio = new Biometry();
        if("enroll".equalsIgnoreCase(mode)){
            if(args.length<2){ System.out.println("Usage: java GreenBoots_BioModules_Pro enroll <SHA256_HEX>"); return; }
            bio.enroll(args[1].trim().toLowerCase());
            System.out.println("Enrolled marker: "+args[1]); return;
        }
        if(!bio.load()){ System.out.println("Kein Marker. Bitte zuerst: java GreenBoots_BioModules_Pro enroll <SHA256_HEX>"); return; }
        if(args.length<2 || !bio.check(args[1].trim().toLowerCase())){ System.out.println("Biometrie: Zugriff verweigert."); return; }

        // --- CLI ---
        String zoneArg = cli.get("zone");
        long units = parseLong(cli.getOrDefault("units","10000000000"), 10_000_000_000L);

        // --- Core/Context ---
        EnergyManager energy = new EnergyManager();
        SwarmField swarm = new SwarmField(units);
        CoolingUnit cooling = new CoolingUnit();
        Audit audit = new Audit();
        Metrics metrics = new Metrics();
        EdgePrivacy edge = new EdgePrivacy();
        ZoneResolver zones = new ZoneResolver();
        RecoveryPolicy policy = new RecoveryPolicy();
        GreenHarvesters harvest = new GreenHarvesters(new Random(7));

        Wallet wallet = new Wallet();
        Marketplace market = new Marketplace();
        market.add(new Marketplace.Offer("Wheat", 0.85, 1000));
        market.add(new Marketplace.Offer("Corn",  0.65, 800));
        SocialEquity equity = new SocialEquity();
        FarmStore store = new FarmStore();
        AidRouter aid = new AidRouter(equity, store, wallet);

        Context ctx = new Context(energy, swarm, cooling, audit, metrics, edge, zones, policy, harvest,
                                  wallet, market, equity, aid, store);

        // --- Modules ---
        ExtensionHub hub = new ExtensionHub(ctx);
        hub.register(new PollinatorGuardianModule());
        hub.register(new ForestGuardianModule());
        hub.register(new BirdAndBatSafeModule());
        hub.register(new BlueCorridorModule());
        hub.register(new TurtleAndGhostNetModule());
        hub.register(new AmphibianHelperModule());
        hub.register(new PalletSentinelModule());
        hub.register(new SmartBinsModule());
        hub.register(new CropGuardianModule());
        hub.register(new ReefMicrolabModule());

        // --- Deploy ---
        swarm.deploy();
        audit.log("system", "GreenBoots PRO bereit: " + NumberFormat.getInstance(Locale.US).format(units) + " Units");
        wallet.credit(120,"bootstrap");

        // --- Route ---
        List<Position> route = (zoneArg==null) ? List.of(
            new Position(48.135,11.582),  // CITY
            new Position(50.000, 8.800),  // FARMLAND
            new Position(47.500,13.000),  // FOREST
            new Position(53.550, 9.993),  // COAST
            new Position(47.950,12.300)   // WETLAND
        ) : List.of(exampleFor(zoneArg));

        int tick=0;
        for(Position p : route){
            tick++;
            String zone = zones.zoneTagFor(p);
            if(!policy.allowStart(zone)){ audit.log("policy","skip zone="+zone+" @"+p); continue; }

            swarm.ensurePlatform();

            // Passives Harvest
            double kJ=0; kJ+=harvest.harvestPlantMicrobialKJ(zone); kJ+=harvest.harvestSolarKJ(zone); energy.add(kJ);
            audit.log("energy","harvest +"+round(kJ)+"kJ ("+zone+")");

            // Bio-Module
            hub.autorun(p, zone);

            // Cooling
            cooling.cool();

            // Commerce: Wenn genug Korn, verkaufe Batch
            if(store.grainKg>650){
                double sellKg = Math.min(300, store.grainKg-500);
                store.grainKg -= sellKg;
                double rev = market.sell("Wheat", sellKg, equity, wallet);
                store.reinvest(rev);
            }

            // Humanitäre Wasserhilfe: alle 4 Ticks ein bedürftiger Haushalt (Demo)
            if(tick%4==0){
                double incomeIdx=0.28; aid.waterAid(160, incomeIdx);
            }

            // Snapshot
            snapshot(ctx, zone, tick);

            audit.log("status", "tick="+tick+" zone="+zone+" E="+round(energy.total())+"kJ T="+round(cooling.temp())+"C store(grain="+round(store.grainKg)+"kg, water="+round(store.waterLiters)+"L) wallet="+String.format(Locale.US,"%.2f",wallet.balance));

            // kleine Pause (Demo)
            try{ Thread.sleep(350);}catch(InterruptedException ignored){}
        }

        System.out.println("\n=== KPIs ===");
        metrics.print();
        System.out.println("\n=== Audit (Tail) ===");
        for(String r: audit.tail()) System.out.println("• "+r);
        System.out.println("\n[END] Gesamtenergie: "+round(energy.total())+" kJ | Wallet: "+String.format(Locale.US,"%.2f",wallet.balance));
    }

    /*------- helpers -------*/
    static Position exampleFor(String zone){
        return switch(zone.toUpperCase(Locale.ROOT)){
            case "FOREST"   -> new Position(47.500,13.000);
            case "CITY"     -> new Position(48.135,11.582);
            case "COAST"    -> new Position(53.550, 9.993);
            case "FARMLAND" -> new Position(50.000, 8.800);
            case "WETLAND"  -> new Position(47.950,12.300);
            default         -> new Position(48.135,11.582);
        };
    }
}