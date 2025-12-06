import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.lang.reflect.Method;

/**
 * RoboCop Special Legend – Enterprise Suite (strict defensive, read-only sim)
 * --------------------------------------------------------------------------
 * Zielkunden: staatliche & private Institutionen (Sicherheits-/Compliance-Fokus)
 *
 * Features (rein defensiv):
 *  - De-Eskalationscoach (Hinweise, Sprache, Distanz, Priorisierung von Hilfe)
 *  - BodycamRecorder + EvidenceLocker (Hash-Kette, Export, Redaction-Hinweise)
 *  - Humanitärrouter (Wasser/Schutz, Fairness – gratis für Bedürftige)
 *  - Engineering (BOM → Beschaffung → Fertigung → QA → Compliance → Distribution)
 *  - Marketing: Produkte, Sales-Heartbeat, ICS-Termin, Landing-Page-Export
 *  - WalletSuite: IBAN/BTC/ETH, PayPal-Link, QR-Payload (PNG mit ZXing via Reflection)
 *
 * Build: javac RoboCop_SpecialLegend_Enterprise.java
 * Run:
 *   java RoboCop_SpecialLegend_Enterprise demo
 *   java RoboCop_SpecialLegend_Enterprise sales
 *   java RoboCop_SpecialLegend_Enterprise aid
 */
public class RoboCop_SpecialLegend_Enterprise {

    /* ======================== KONTAKT / ACCOUNTS ======================== */
    static final class Contact {
        static final String BRAND         = "RoboCop Special Legend (Defensive Suite)";
        static final String ORG           = "ShariaBoots SecureSwarm";
        static final String PHONE_E164    = "+387671052799";           // <- vom Nutzer geliefert
        static final String PHONE_DISPLAY = "+387 67 105 2799";
        static final String ADDRESS       = "Jezerski bb, 77241 Bosanska Krupa, BA";
        static final String EMAIL         = "contact@secureswarm.example"; // Platzhalter
        // Wallet / Payment
        static final String IBAN          = "AT921400057010099023";    // <- Nutzer IBAN
        static final String PAYPAL_HANDLE = "SanelCrnkic";             // <- aus deinem Material
        static final String BTC           = "bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd";
        static final String ETH           = "0x40dEa729f32481A707917AcBBD9eaA84AcB66367";
    }

    /* ============================= MAIN ============================= */
    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "demo";
        System.out.println("== "+Contact.BRAND+" ==");
        System.out.println("[Kontakt] "+Contact.ORG+" • "+Contact.PHONE_DISPLAY+" • "+Contact.ADDRESS);
        System.out.println("[Wallet]  IBAN "+Contact.IBAN+"  | PayPal.me/"+Contact.PAYPAL_HANDLE+"\n");

        // Kern-Stacks
        SafetyPolicy safety = SafetyPolicy.strict();
        WalletSuite wallets = new WalletSuite();
        MarketingSuite marketing = new MarketingSuite(wallets);
        EngineeringStack engineering = new EngineeringStack(safety);
        HumanitarianStack humanitarian = new HumanitarianStack();
        EvidenceSuite evidence = new EvidenceSuite();

        // Visitenkarte: QR/Wallet/ICS/LP
        wallets.card();
        wallets.generateQRCodes(0.001, 25.00, "RoboCop Special Legend Support");
        File ics = AppointmentScheduler.create(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0),
                "Persönliches Gespräch: " + Contact.BRAND,
                "Vorstellung & Due Diligence. Hotline: " + Contact.PHONE_DISPLAY + " | Adresse: " + Contact.ADDRESS);
        System.out.println("[Termin] ICS: " + ics.getAbsolutePath());
        File lp = marketing.exportLandingPage();
        System.out.println("[LandingPage] " + lp.getAbsolutePath() + "\n");

        switch (mode) {
            case "demo" -> {
                // 1) Engineering-Batch
                engineering.runOneBatch();
                // 2) De-Eskalation & Bodycam-Demo
                DeEscalationCoach coach = new DeEscalationCoach();
                coach.advise("laute Menschenmenge, Stress, unklare Lage, verletzte Person");
                evidence.demoBodycamScenario();
                // 3) Humanitär
                humanitarian.runAidScenario(BalancedPaymentProcessor.defaultFor(Contact.IBAN));
                System.out.println("\n[Ende] Demo abgeschlossen.");
            }
            case "sales" -> marketing.salesHeartbeat();
            case "aid"   -> humanitarian.runAidScenario(BalancedPaymentProcessor.defaultFor(Contact.IBAN));
            default      -> System.out.println("Modi: demo | sales | aid");
        }
    }

    /* =========================== SAFETY / POLICY ========================== */
    static final class SafetyPolicy {
        final boolean strictDefensiveOnly;
        private SafetyPolicy(boolean v){ this.strictDefensiveOnly = v; }
        static SafetyPolicy strict(){ return new SafetyPolicy(true); }
        void check(String action){ if (strictDefensiveOnly) System.out.println("[Safety] defensive-only • "+action); }
    }

    /* ========================== EVIDENCE / BODYCAM ======================== */
    static final class EvidenceSuite {
        final EvidenceLocker locker = new EvidenceLocker();

        void demoBodycamScenario(){
            System.out.println("== EVIDENCE: Bodycam-Demo ==");
            BodycamRecorder cam = new BodycamRecorder(locker);
            cam.start("Bahnhof-Vorplatz", "Team A-12");
            cam.note("Eintreffen – verletzte Person, Rettung angefordert");
            cam.note("De-Eskalationsansprache, Abstand 2.5m, offene Hände, klare Sprache");
            cam.stop();
            File pkg = locker.exportPackage("evidence_pkg.txt");
            System.out.println("[Evidence] Export: " + pkg.getAbsolutePath() + "\n");
        }
    }
    static final class BodycamRecorder {
        final EvidenceLocker locker; boolean running=false;
        BodycamRecorder(EvidenceLocker l){ this.locker = l; }
        void start(String location, String unit){ running=true; locker.add("START@"+ts()+" loc="+location+" unit="+unit); }
        void note(String text){ if(running) locker.add("NOTE@"+ts()+" "+text); }
        void stop(){ if(running){ locker.add("STOP@"+ts()); running=false; } }
        private static String ts(){ return OffsetDateTime.now().toString(); }
    }
    static final class EvidenceLocker {
        final List<String> lines = new ArrayList<>();
        void add(String line){ lines.add(line); }
        File exportPackage(String name){
            try {
                // Hash-Kette
                List<String> out = new ArrayList<>();
                String prev = "GENESIS";
                for (String l : lines) {
                    String h = sha256(prev + "|" + l);
                    out.add(h + "  " + l);
                    prev = h;
                }
                out.add("CHAIN_HEAD="+prev);
                File f = new File(name);
                try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)){
                    w.write(String.join("\n", out));
                }
                return f;
            } catch (Exception e) {
                System.out.println("Evidence export fehlgeschlagen: "+e.getMessage());
                return new File(name);
            }
        }
        private static String sha256(String s) throws Exception{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        }
    }
    static final class RedactionTool {
        // rudimentär: maskiert Telefonnummern/IBANs in Strings (Demozweck)
        static String mask(String s){
            return s.replaceAll("\\+?\\d[\\d\\s-]{6,}", "****(phone)")
                    .replaceAll("[A-Z]{2}\\d{2}[A-Z0-9]{11,30}", "****(iban)");
        }
    }

    /* =========================== DE-ESKALATION ============================ */
    static final class DeEscalationCoach {
        void advise(String situation){
            System.out.println("== DE-ESKALATIONSRATGEBER ==");
            String low = situation.toLowerCase(Locale.ROOT);
            System.out.println("- Stimme: ruhig, Sätze <10 Wörter, Ich-Botschaften");
            System.out.println("- Distanz: mind. 2m, seitliche Position, freie Hände sichtbar");
            System.out.println("- Priorität: Verletzte zuerst (Erste Hilfe anfordern)");
            if (low.contains("stress")) System.out.println("- Hinweis: 4-7-8-Atmung anleiten (4 ein, 7 halten, 8 aus)");
            if (low.contains("menge"))  System.out.println("- Menge: Raum schaffen, klare Rollen, nur 1 Sprecher/in");
            if (low.contains("unklar")) System.out.println("- Lageklärung: offene Fragen, paraphrasieren, aktiv zuhören");
            System.out.println("- Dokumentation: Bodycam an, respektvolle Sprache, Redaction bei Veröffentlichung\n");
        }
    }

    /* ============================ ENGINEERING ============================= */
    enum MaterialKategorie { TEXTIL, ENERGIE, SENSORIK, PNEUMATIK, THERMIK, GEHÄUSE, ELEKTRONIK, SONSTIGES }
    static final class Material {
        final String name; final MaterialKategorie kat; final String desc; final String zerts; final boolean kritisch;
        Material(String n, MaterialKategorie k, String d, String z, boolean c){ name=n; kat=k; desc=d; zerts=z; kritisch=c; }
    }
    static final class BOMItem {
        final String modul; final Material mat; final int menge; final String einheit; final boolean mustHave;
        BOMItem(String m, Material mat, int menge, String e, boolean must){ this.modul=m; this.mat=mat; this.menge=menge; this.einheit=e; this.mustHave=must; }
    }
    static final class BOMCatalog {
        final List<BOMItem> items = new ArrayList<>();
        static BOMCatalog defensiveVestAirbag(){
            BOMCatalog b = new BOMCatalog();
            b.items.add(new BOMItem("Innenlage", new Material("Merino/Modacryl", MaterialKategorie.TEXTIL, "hautfreundlich/flammhemmend", "REACH", true), 4, "m²", true));
            b.items.add(new BOMItem("Außenhaut", new Material("Hydrophobes Composite", MaterialKategorie.GEHÄUSE, "abriebfest/wasserabweisend", "REACH", true), 3, "m²", true));
            b.items.add(new BOMItem("Airbag-Matrix", new Material("CO₂-Kartusche (UN/CE)", MaterialKategorie.PNEUMATIK, "zertifizierte Einwegkartuschen", "UN/CE", true), 2, "Stk", true));
            b.items.add(new BOMItem("Ventile/Schläuche", new Material("Schnellventile & HD-Schlauch", MaterialKategorie.PNEUMATIK, "kältefest", "CE", true), 6, "Stk", true));
            b.items.add(new BOMItem("Supercaps", new Material("Industrie-Superkondensatoren", MaterialKategorie.ENERGIE, "Kurzzeitleistung", "CE", true), 6, "Stk", true));
            b.items.add(new BOMItem("Reserveakku", new Material("LiFePO₄-Minipack", MaterialKategorie.ENERGIE, "sichere Zellchemie", "CE/UN38.3", true), 1, "Stk", true));
            b.items.add(new BOMItem("Sensorik", new Material("IMU+Baro+Druck", MaterialKategorie.SENSORIK, "Auslöse-Detektion", "CE", true), 1, "Satz", true));
            b.items.add(new BOMItem("Steuerung", new Material("Mikrocontroller+Treiber", MaterialKategorie.ELEKTRONIK, "deterministisch", "CE", true), 1, "Satz", true));
            b.items.add(new BOMItem("Thermik", new Material("Heat-Pipes + Mikrokanäle", MaterialKategorie.THERMIK, "Hitzeableitung", "CE", true), 1, "Satz", true));
            return b;
        }
    }
    static final class ProcurementResult { final Map<BOMItem, Boolean> ok = new HashMap<>(); boolean allesOK(){ return ok.values().stream().allMatch(Boolean::booleanValue); } }
    static final class ProcurementManager {
        ProcurementResult beschaffen(BOMCatalog bom){
            System.out.println("[Beschaffung] Start");
            ProcurementResult r = new ProcurementResult();
            for (BOMItem it : bom.items){
                boolean ok = checkLegalSafe(it.mat) && checkZertifikate(it.mat);
                r.ok.put(it, ok || !it.mustHave);
                System.out.println((ok?"OK   ":"SKIP ")+it.modul+" → "+it.mat.name);
            }
            return r;
        }
        boolean checkLegalSafe(Material m){ return true; }
        boolean checkZertifikate(Material m){ return m.zerts!=null && !m.zerts.isBlank(); }
    }
    enum BuildStatus { FERTIG, FEHLER }
    static final class BuildPlan {
        final List<String> schritte = Arrays.asList(
                "Zuschnitt/Textil (maßhaltig)",
                "Airbag-Matrix (Zellen+Ventile montieren, trocken)",
                "Thermik-Lage (abdichten, Dichtprüfung)",
                "Energie-Puffer (Supercaps+LiFePO₄ absichern)",
                "Sensorik/Steuerung (IMU/Baro/Druck)",
                "Endmontage (Nähte/Dichtungen/Stecker)"
        );
    }
    static final class AssemblyLine {
        BuildStatus fertigen(BuildPlan p, ProcurementResult pr){
            if (!pr.allesOK()) { System.out.println("Bau gestoppt: Must-have fehlt."); return BuildStatus.FEHLER; }
            for (String s : p.schritte) System.out.println("→ Bau: "+s);
            return BuildStatus.FERTIG;
        }
    }
    static final class QAValidator {
        void pruefen(){
            System.out.println("QA: Dichtigkeit OK • Trockenlauf CO₂ OK • Sensorik OK • Energiepfad OK");
        }
    }
    static final class ComplianceGate {
        boolean freigeben(){ System.out.println("Compliance: defensiv, nicht-letal, read-only ✓"); return true; }
    }
    static final class DistributionManager {
        void ausliefern(List<CitizenProfile> wl, double ops){
            SocialEquityEngine eq = new SocialEquityEngine();
            for (CitizenProfile c : wl){
                EquityTier t = eq.classifyCitizen(c);
                double preis = (t==EquityTier.NEEDY)?0.0:(t==EquityTier.WEALTHY?ops*1.8:ops*1.05);
                System.out.println("Auslieferung an "+c.id+" | Tier="+t+" | Preis="+euro(preis));
            }
        }
    }
    static final class EngineeringStack {
        final SafetyPolicy safety; EngineeringStack(SafetyPolicy s){ this.safety=s; }
        void runOneBatch(){
            safety.check("BOM→Beschaffung→Fertigung→QA→Compliance→Distribution");
            BOMCatalog bom = BOMCatalog.defensiveVestAirbag();
            ProcurementManager pm = new ProcurementManager();
            ProcurementResult pr = pm.beschaffen(bom);
            if (new AssemblyLine().fertigen(new BuildPlan(), pr) != BuildStatus.FERTIG) return;
            new QAValidator().pruefen();
            if (!new ComplianceGate().freigeben()) return;
            List<CitizenProfile> wl = CitizenProfile.sample(6);
            double ops = bom.items.stream().filter(i->i.mustHave).count() * 25.0;
            new DistributionManager().ausliefern(wl, ops);
            System.out.println();
        }
    }

    /* ============================== HUMANITÄR ============================== */
    enum EquityTier { NEEDY, STANDARD, WEALTHY }
    static final class SocialEquityEngine {
        EquityTier classifyCitizen(CitizenProfile c){
            if (c.needScore >= 0.7 || c.incomeIndex <= 0.35) return EquityTier.NEEDY;
            if (c.incomeIndex >= 0.75 && c.needScore < 0.5)   return EquityTier.WEALTHY;
            return EquityTier.STANDARD;
        }
    }
    static final class CitizenProfile {
        final String id; final double incomeIndex; final double needScore; final int householdSize;
        CitizenProfile(String id,double income,double need,int hh){ this.id=id; this.incomeIndex=income; this.needScore=need; this.householdSize=hh; }
        static CitizenProfile random(Random r){
            return new CitizenProfile("cit-"+Math.abs(r.nextInt()), 0.1+r.nextDouble()*0.9, 0.1+r.nextDouble()*0.9, 1+r.nextInt(6));
        }
        static List<CitizenProfile> sample(int n){ Random r=new Random(); List<CitizenProfile> l=new ArrayList<>(); for(int i=0;i<n;i++) l.add(random(r)); return l; }
    }
    static final class WasserManagementModul {
        double allocateWaterLiters(double req){ double p = Math.min(req, 500); System.out.println("Wasserhilfe "+(int)p+"L"); return p; }
        void initialisiere(){ System.out.println("Wasser init"); }
    }
    static final class Bodyguard { void protectCitizen(String id){ System.out.println("Schutz (defensiv) für "+id); } }
    static final class AidOutcome { final double liters; final boolean prot; final double charged; AidOutcome(double l, boolean p, double c){ liters=l; prot=p; charged=c; } }
    static final class AidLedger {
        double totalL=0; int protC=0; double charged=0;
        void record(AidOutcome o){ totalL+=o.liters; protC+=o.prot?1:0; charged+=o.charged; }
        void status(){ System.out.println("[AidLedger] Wasser="+(int)totalL+"L Schutz="+protC+" Abrechnung="+euro(charged)); }
    }
    static final class HumanitarianStack {
        final AidLedger ledger = new AidLedger();
        final WasserManagementModul wasser = new WasserManagementModul();
        final Bodyguard bodyguard = new Bodyguard();

        void runAidScenario(BalancedPaymentProcessor payments){
            System.out.println("== HUMANITÄR ==");
            wasser.initialisiere();
            Random r=new Random();
            for (int i=0;i<3;i++){
                CitizenProfile c = CitizenProfile.random(r);
                boolean water = r.nextBoolean(), prot = r.nextBoolean() || c.needScore>0.6;
                double litersReq = water ? (50 + r.nextInt(200)) : 0;
                AidOutcome o = handle(c, water, prot, litersReq, payments);
                ledger.record(o);
            }
            ledger.status();
            System.out.println();
        }
        AidOutcome handle(CitizenProfile c, boolean water, boolean protection, double liters, BalancedPaymentProcessor payments){
            EquityTier t = new SocialEquityEngine().classifyCitizen(c);
            double litersDelivered = 0, charge=0;
            if (water){ litersDelivered = wasser.allocateWaterLiters(liters); charge += serviceWaterPrice(t, litersDelivered); }
            if (protection){ bodyguard.protectCitizen(c.id); charge += serviceProtectionPrice(t); }
            if (charge>0 && t!=EquityTier.NEEDY) payments.route(charge, Math.max(0, charge*0.15)); else System.out.println("Aid gratis für "+c.id+" ("+t+")");
            return new AidOutcome(litersDelivered, protection, charge);
        }
        private static double serviceWaterPrice(EquityTier t, double liters){ double base = liters*0.5; if (t==EquityTier.NEEDY) return 0; if (t==EquityTier.WEALTHY) return base*1.5; return base; }
        private static double serviceProtectionPrice(EquityTier t){ double base=200; if (t==EquityTier.NEEDY) return 0; if (t==EquityTier.WEALTHY) return base*2; return base; }
    }

    /* ============================== MARKETING ============================== */
    static final class Offer {
        final long id; final String title; final double price; final int stock;
        Offer(long id,String t,double p,int s){ this.id=id; this.title=t; this.price=p; this.stock=s; }
    }
    static final class MarketingSuite {
        final WalletSuite wallet;
        MarketingSuite(WalletSuite w){ this.wallet=w; }
        List<Offer> catalog(){
            return Arrays.asList(
                    new Offer(1,"RoboCop Special Legend – Defensive Core (Simulation)", 980, 100),
                    new Offer(2,"Bodycam & EvidenceLocker (Hash-Chain, Redaction-Hints)", 1250, 60),
                    new Offer(3,"Humanitarian Water&Shield Bundle", 1500, 40)
            );
        }
        void salesHeartbeat(){
            System.out.println("== SALES ==");
            for (Offer o : catalog()) System.out.println(" - "+o.title+" • ab "+euro(o.price));
            System.out.println("Termin vereinbaren: "+Contact.PHONE_DISPLAY+" • PayPal.me/"+Contact.PAYPAL_HANDLE);
            wallet.detectDemo();
            System.out.println();
        }
        File exportLandingPage(){
            String html = "<!doctype html><html lang='de'><meta charset='utf-8'>"
                    +"<title="+esc(Contact.BRAND)+"></title>"
                    +"<style>body{font-family:system-ui;margin:40px;max-width:900px} .c{border:1px solid #ddd;padding:16px;margin:12px 0;border-radius:8px}</style>"
                    +"<h1>"+esc(Contact.BRAND)+"</h1>"
                    +"<p>Rein defensiv • De-Eskalation • Bodycam/Evidence • Humanitär</p>"
                    +"<div class='c'><h2>Kontakt</h2><p>"+esc(Contact.ORG)+" • "+esc(Contact.ADDRESS)+" • Tel: <b>"+esc(Contact.PHONE_DISPLAY)+"</b></p>"
                    +"<p>IBAN: <b>"+esc(Contact.IBAN)+"</b> • PayPal: <b>paypal.me/"+esc(Contact.PAYPAL_HANDLE)+"</b></p></div>"
                    +"<div class='c'><h2>Produkte</h2><ul>"
                    +"<li>Defensive Core (Read-Only Simulation)</li>"
                    +"<li>Bodycam & EvidenceLocker (Hash-Kette, Redaction-Hints)</li>"
                    +"<li>Humanitarian Water & Shield Bundle</li></ul></div>"
                    +"</html>";
            return writeFile("landing_robocop_special_legend.html", html);
        }
    }

    /* ============================== WALLET / QR ============================ */
    static final class WalletSuite {
        void card(){
            System.out.println("[Wallets]");
            System.out.println(" - IBAN: " + Contact.IBAN);
            System.out.println(" - BTC ••••" + Contact.BTC.substring(Contact.BTC.length()-6));
            System.out.println(" - ETH ••••" + Contact.ETH.substring(Contact.ETH.length()-6) + "\n");
        }
        void detectDemo(){
            System.out.println("[Detect] IBAN → " + PaymentDetector.detect(Contact.IBAN));
            System.out.println("[Detect] BTC  → " + PaymentDetector.detect(Contact.BTC));
            System.out.println("[Detect] ETH  → " + PaymentDetector.detect(Contact.ETH));
        }
        void generateQRCodes(Double btcAmount, Double sepaAmountEUR, String sepaNote){
            String btc = QrPayloadFactory.bitcoinUri(Contact.BTC, btcAmount);
            if (!QrPngWriter.writePngIfZXing(btc, new File("qr_btc.png"), 640))
                QrPngWriter.printFallback("BTC (BIP-21)", btc);
            String eth = QrPayloadFactory.ethereumUri(Contact.ETH);
            if (!QrPngWriter.writePngIfZXing(eth, new File("qr_eth.png"), 640))
                QrPngWriter.printFallback("ETH (ethereum:)", eth);
            String epc = QrPayloadFactory.epcSepaPayload(Contact.ORG, Contact.IBAN, sepaAmountEUR, sepaNote);
            if (!QrPngWriter.writePngIfZXing(epc, new File("qr_sepa.png"), 640))
                QrPngWriter.printFallback("SEPA/EPC (BCD)", epc);
        }
    }
    enum PaymentKind { IBAN_EUR, BITCOIN, ETHEREUM, UNKNOWN }
    static final class PaymentDetector {
        private static final String IBAN_RE = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$";
        private static final String ETH_RE  = "^0x[0-9a-fA-F]{40}$";
        private static final String BTC_RE  = "^(bc1|[13])[a-zA-HJ-NP-Z0-9]{25,90}$";
        static PaymentKind detect(String s){
            if (s==null) return PaymentKind.UNKNOWN;
            String x = s.replace(" ", "");
            if (x.matches(ETH_RE)) return PaymentKind.ETHEREUM;
            if (x.matches(BTC_RE)) return PaymentKind.BITCOIN;
            if (x.matches(IBAN_RE)) return PaymentKind.IBAN_EUR;
            return PaymentKind.UNKNOWN;
        }
    }
    static final class QrPayloadFactory {
        static String bitcoinUri(String address, Double amountBtc){
            if (amountBtc == null) return "bitcoin:" + address;
            NumberFormat nf = NumberFormat.getInstance(Locale.US); nf.setGroupingUsed(false); nf.setMaximumFractionDigits(8);
            return "bitcoin:" + address + "?amount=" + nf.format(amountBtc);
        }
        static String ethereumUri(String address){ return "ethereum:" + address; }
        static String epcSepaPayload(String name, String iban, Double amountEUR, String rem){
            String version="001", encoding="1", service="SCT", bic="";
            String amount = (amountEUR==null) ? "" : "EUR"+String.format(Locale.US,"%.2f",Math.max(0.01, amountEUR));
            String r = (rem==null) ? "" : rem;
            return String.join("\n", Arrays.asList("BCD",version,encoding,service,bic,name,iban.replace(" ",""),amount,"",r,"","") );
        }
    }
    static final class QrPngWriter {
        @SuppressWarnings("unchecked")
        static boolean writePngIfZXing(String payload, File out, int size){
            try {
                Class<?> bfClass   = Class.forName("com.google.zxing.BarcodeFormat");
                Object qrFormat    = Enum.valueOf((Class<Enum>) bfClass.asSubclass(Enum.class), "QR_CODE");
                Class<?> writerCls = Class.forName("com.google.zxing.qrcode.QRCodeWriter");
                Object writer      = writerCls.getDeclaredConstructor().newInstance();
                Method encode      = writerCls.getMethod("encode", String.class, bfClass, int.class, int.class, Map.class);
                Object bitMatrix   = encode.invoke(writer, payload, qrFormat, size, size, null);
                Class<?> bmClass   = Class.forName("com.google.zxing.common.BitMatrix");
                int w = (int) bmClass.getMethod("getWidth").invoke(bitMatrix);
                int h = (int) bmClass.getMethod("getHeight").invoke(bitMatrix);
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w,h, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Method get = bmClass.getMethod("get", int.class, int.class);
                for (int y=0; y<h; y++) for (int x=0; x<w; x++) {
                    boolean on = (boolean) get.invoke(bitMatrix, x, y);
                    img.setRGB(x, y, on ? 0x000000 : 0xFFFFFF);
                }
                javax.imageio.ImageIO.write(img, "png", out);
                System.out.println("[QR] PNG geschrieben → " + out.getAbsolutePath());
                return true;
            } catch (ClassNotFoundException e) {
                System.out.println("[QR] ZXing nicht gefunden – PNG-Export übersprungen. Payload unten.");
                return false;
            } catch (Throwable t) {
                System.out.println("[QR] Fehler beim PNG-Schreiben: " + t.getMessage());
                return false;
            }
        }
        static void printFallback(String title, String payload){ System.out.println("["+title+"]\n"+payload+"\n"); }
    }

    /* ============================== APPOINTMENTS =========================== */
    static final class AppointmentScheduler {
        static File create(LocalDateTime start, String title, String description){
            LocalDateTime end = start.plusMinutes(45);
            String uid = UUID.randomUUID()+"@robocop-special-legend";
            String ics = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//RoboCop-SpecialLegend//EN\nBEGIN:VEVENT\n"
                    + "UID:"+uid+"\nDTSTAMP:"+fmt(LocalDateTime.now())+"\n"
                    + "DTSTART:"+fmt(start)+"\nDTEND:"+fmt(end)+"\n"
                    + "SUMMARY:"+escape(title)+"\nDESCRIPTION:"+escape(description)+"\\nTel: "+Contact.PHONE_DISPLAY+"\n"
                    + "LOCATION:"+escape(Contact.ADDRESS)+"\nEND:VEVENT\nEND:VCALENDAR\n";
            return writeFile("robocop_special_legend_meeting.ics", ics);
        }
        private static String fmt(LocalDateTime t){ return t.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")); }
        private static String escape(String s){ return s.replace("\\","\\\\").replace("\n","\\n").replace(",","\\,").replace(";","\\;"); }
    }

    /* ============================== FINANCE ================================ */
    static final class FinancePolicy {
        double reserveTarget=500.0, wWallet=0.45, wCloud=0.00, wTech=0.35, wAid=0.20;
        static FinancePolicy def(){ return new FinancePolicy(); }
    }
    static final class WalletRueckspeisung { final String iban; double saldo=0; WalletRueckspeisung(String i){iban=i;} void gutschreiben(double e){ saldo+=e; System.out.println("Rückspeisung an IBAN "+iban+" +"+euro(e)); } }
    static final class CloudBackfeed { void feed(double e){ System.out.println("Cloud-Rückspeisung +"+euro(e)); } }
    static final class SiloManager { void cloud(double e){ System.out.println("Cloud-Silo +"+euro(e)); } void wallet(double e){ System.out.println("Wallet-Silo +"+euro(e)); } }
    static final class BalancedPaymentProcessor {
        final WalletRueckspeisung wallet; final CloudBackfeed cloud; final SiloManager silos; final FinancePolicy p; double reserve=0;
        BalancedPaymentProcessor(WalletRueckspeisung w, CloudBackfeed c, SiloManager s, FinancePolicy p){ this.wallet=w; this.cloud=c; this.silos=s; this.p=p; }
        static BalancedPaymentProcessor defaultFor(String iban){
            return new BalancedPaymentProcessor(new WalletRueckspeisung(iban), new CloudBackfeed(), new SiloManager(), FinancePolicy.def());
        }
        void route(double revenue, double ops){
            revenue = Math.max(0,revenue); ops=Math.max(0,ops);
            System.out.println(String.format(Locale.US,"[Route] Einnahmen=%.2f Kosten=%.2f Reserve=%.2f",revenue,ops,reserve));
            double margin = revenue - ops;
            if (margin < 0){
                double need = -margin;
                if (reserve >= need){ reserve -= need; System.out.println("[Route] Defizit "+euro(need)+" aus Reserve gedeckt → "+euro(reserve)); margin=0; }
                else { System.out.println("[Route] Defizit "+euro(need)+" NICHT gedeckt. Keine Ausschüttung."); return; }
            }
            double fill = Math.min(Math.max(0, p.reserveTarget - reserve), margin);
            reserve += fill; double rest = margin - fill;
            if (fill>0) System.out.println("[Route] Reserve +"+euro(fill)+" → "+euro(reserve));
            if (rest<=0){ System.out.println("[Route] Kein Überschuss."); return; }
            double toWallet = rest*p.wWallet, toTech = rest*p.wTech, toAid = rest*p.wAid, toCloud = rest*p.wCloud;
            if (toWallet>0){ wallet.gutschreiben(toWallet); silos.wallet(toWallet); }
            if (toCloud>0){ cloud.feed(toCloud); silos.cloud(toCloud); }
            if (toTech>0) System.out.println("[R&D] Budget +"+euro(toTech));
            if (toAid>0)  System.out.println("[Aid] Budget +"+euro(toAid));
        }
    }

    /* ============================== HELPERS ================================ */
    static String euro(double v){ return "€"+String.format(Locale.US,"%.2f",v); }
    static File writeFile(String name, String content){
        try (Writer w = new OutputStreamWriter(new FileOutputStream(name), StandardCharsets.UTF_8)){ w.write(content); }
        catch(Exception e){ System.out.println("Datei-Fehler: "+e.getMessage()); }
        return new File(name);
    }
    private static String esc(String s){ return s==null?"":s.replace("<","&lt;").replace(">","&gt;"); }
}
