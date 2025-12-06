import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Sharo SecureOps 2-in-1 (Humanity + Security) – Demo/Simulation (defensiv)
 * -------------------------------------------------------------------------
 * - Wallet: IBAN/BTC/ETH + PayPal, QR (ZXing optional via Reflection)
 * - Humanity: Aid-Router (Wasser/Schutz) mit Fairness, Budget-Routing
 * - Security: Evidence/Bodycam (Hash-Kette), Shield (defensiv), Threat Absorber (sim)
 * - Predictive: Sharo Predictive Risk (EMA + Trends), Frühwarn-Level
 * - Operations: Dienstpläne (ICS-Export), Einsatz-Statistik (SLA/MTTR), Sales-Herzschlag
 * - DermaCareProPlus: sichere, ethische Stub-Integration (ohne heikle Details)
 *
 * Build: javac Sharo_SecureOps_2in1.java
 * Run:   java Sharo_SecureOps_2in1
 */
public class Sharo_SecureOps_2in1 {

    /* ========================== KONTAKT / WALLET ========================== */
    static final class Contact {
        static final String BRAND         = "RoboCop Special Legend – SecureOps 2-in-1";
        static final String ORG           = "ShariaBoots SecureSwarm";
        static final String PHONE_E164    = "+387671052799";           // <- deine Nummer
        static final String PHONE_DISPLAY = "+387 67 105 2799";
        static final String ADDRESS       = "Jezerski bb, 77241 Bosanska Krupa, BA";
        static final String EMAIL         = "contact@secureswarm.example"; // Platzhalter

        // Wallet / Payment
        static final String IBAN          = "AT921400057010099023";
        static final String PAYPAL_HANDLE = "SanelCrnkic";
        static final String BTC           = "bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd";
        static final String ETH           = "0x40dEa729f32481A707917AcBBD9eaA84AcB66367";
    }

    public static void main(String[] args) {
        System.out.println("== "+Contact.BRAND+" ==");
        System.out.println("[Kontakt] "+Contact.ORG+" • "+Contact.PHONE_DISPLAY+" • "+Contact.ADDRESS);
        System.out.println("[Wallet]  IBAN "+Contact.IBAN+"  | PayPal.me/"+Contact.PAYPAL_HANDLE+"\n");

        // Suites
        WalletSuite wallet = new WalletSuite();
        wallet.card();
        wallet.generateQRCodes(0.001, 25.00, "SecureOps Support");

        AppointmentScheduler.create(
                LocalDateTime.now().plusDays(2).withHour(14).withMinute(0),
                "Persönliches Gespräch: SecureOps 2-in-1",
                "Vorstellung, Due Diligence, Hotline: "+Contact.PHONE_DISPLAY+" – Adresse: "+Contact.ADDRESS);

        MarketingSuite marketing = new MarketingSuite(wallet);
        marketing.salesHeartbeat();

        // Predictive (Sharo Predictive)
        PredictiveRiskEngine predictive = new PredictiveRiskEngine();
        predictive.feedCO2(840); predictive.feedCO2(990); predictive.feedCO2(1160);
        System.out.println("[Predictive] Level: "+predictive.currentLevel()+" • Trend="+String.format(Locale.US,"%.1f", predictive.trend())+"\n");

        // Security 2-in-1 (Evidence + Shield) & Threat Absorber (sim)
        EvidenceSuite evidence = new EvidenceSuite();
        SafeGuardSystem guard  = new SafeGuardSystem();
        ThreatEnergyAbsorber absorber = new ThreatEnergyAbsorber(guard.shield);

        evidence.demoBodycam("Stadtpark Nord", "Team A-12");
        absorber.absorb("Rakete", 520);
        absorber.absorb("Laser",  340);
        absorber.absorb("Atombedrohung (sim)", 1600); // rein simulativer Tag
        System.out.println();

        // Humanity Stack + Budget-Routing (Balanced)
        BalancedPaymentProcessor payments = BalancedPaymentProcessor.defaultFor(Contact.IBAN);
        HumanitarianStack humanitarian = new HumanitarianStack();
        humanitarian.runAidScenario(payments);
        humanitarian.ledger.status();
        System.out.println();

        // Dienstpläne + ICS
        DutyRoster roster = new DutyRoster();
        roster.addMember(new Staff("Sanel",  "Lead Ops"));
        roster.addMember(new Staff("Amira",  "Dispatch"));
        roster.addMember(new Staff("Faruk",  "Field Medic"));
        roster.scheduleWeek(LocalDate.now().plusDays(1)); // Start morgen
        File ics = roster.exportICS("roster_next_week.ics");
        System.out.println("[Dienstplan] ICS: "+ics.getAbsolutePath()+"\n");

        // Einsatz-Statistik
        OpsStats stats = new OpsStats();
        stats.markIncident("HUM_WATER", 18);
        stats.markIncident("HUM_PROTECT", 27);
        stats.markIncident("SEC_BODYCAM", 12);
        stats.printSummary();

        // DermaCareProPlus – sichere Stub-Integration (Read-Only)
        DermaCareProPlusIntegration derma = new DermaCareProPlusIntegration();
        derma.safeInstallDemo();

        System.out.println("\n[END] SecureOps 2-in-1 Demo abgeschlossen.");
    }

    /* ============================== PREDICTIVE ============================= */
    static final class PredictiveRiskEngine {
        // Einfache EMA + Trend; rein informativ (Sharo Predictive)
        private Double ema = null;
        private final double alpha = 0.35;
        private final Deque<Double> last = new ArrayDeque<>();

        public void feedCO2(double ppm){
            if (ema == null) ema = ppm;
            else ema = alpha*ppm + (1-alpha)*ema;
            last.add(ema);
            if (last.size() > 6) last.removeFirst();
        }
        public double trend(){ // einfacher Trend: letzte minus mittlere
            if (last.isEmpty()) return 0;
            double avg = last.stream().mapToDouble(d->d).average().orElse(0);
            return last.getLast() - avg;
        }
        public String currentLevel(){
            double t = trend();
            if (t > 60) return "HIGH";
            if (t > 20) return "ELEVATED";
            return "NORMAL";
        }
    }

    /* ============================== SECURITY =============================== */
    static final class SafeGuardSystem {
        final EnergyManager energy = new EnergyManager();
        final PlasmaShield  shield = new PlasmaShield(energy);
        final EvidenceSuite evidence = new EvidenceSuite();
    }

    static final class ThreatEnergyAbsorber {
        private final PlasmaShield shield;
        private double totalAbsorbed = 0;
        ThreatEnergyAbsorber(PlasmaShield shield){ this.shield = shield; }
        public void absorb(String label, double energyUnits){
            // rein defensiv/sim: Energie wird "abgeleitet", keine Angriffslogik
            totalAbsorbed += Math.max(0, energyUnits);
            System.out.println("[Absorber] "+label+" absorbiert +" + (int)energyUnits + " (sim)");
            shield.boostKurz(Math.min(energyUnits*0.25, 120));
        }
        public double total(){ return totalAbsorbed; }
    }

    /* ========================== EVIDENCE / BODYCAM ========================= */
    static final class EvidenceSuite {
        final EvidenceLocker locker = new EvidenceLocker();

        void demoBodycam(String ort, String unit){
            BodycamRecorder cam = new BodycamRecorder(locker);
            cam.start(ort, unit);
            cam.note("Eintreffen, Lage sondiert, De-Eskalationsansprache");
            cam.note("Abstand 2.5m, offene Hände, klare Sprache");
            cam.stop();
            File pkg = locker.exportPackage("evidence_pkg.txt");
            System.out.println("[Evidence] Export: "+pkg.getAbsolutePath());
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
                List<String> out = new ArrayList<>();
                String prev = "GENESIS";
                for (String l : lines) {
                    String h = sha256(prev + "|" + l);
                    out.add(h + "  " + l);
                    prev = h;
                }
                out.add("CHAIN_HEAD="+prev);
                try (Writer w = new OutputStreamWriter(new FileOutputStream(name), StandardCharsets.UTF_8)){
                    w.write(String.join("\n", out));
                }
            } catch (Exception e) {
                System.out.println("Evidence export fehlgeschlagen: "+e.getMessage());
            }
            return new File(name);
        }
        private static String sha256(String s) throws Exception{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        }
    }

    /* ============================== HUMANITY =============================== */
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
        void initialisiere(){ System.out.println("Wasser init"); }
        double allocateWaterLiters(double requested){ double provided=Math.min(requested,500); System.out.println("Wasserhilfe "+(int)provided+"L"); return provided; }
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
        final SocialEquityEngine equity = new SocialEquityEngine();

        void runAidScenario(BalancedPaymentProcessor payments){
            System.out.println("== HUMANITÄR ==");
            wasser.initialisiere();
            for (CitizenProfile c : CitizenProfile.sample(3)){
                boolean w = new Random().nextBoolean();
                boolean p = new Random().nextBoolean() || c.needScore>0.6;
                double litersReq = w ? (50 + new Random().nextInt(200)) : 0;
                AidOutcome o = handle(c, w, p, litersReq, payments);
                ledger.record(o);
            }
        }
        AidOutcome handle(CitizenProfile c, boolean water, boolean protection, double liters, BalancedPaymentProcessor payments){
            EquityTier t = equity.classifyCitizen(c);
            double litersDelivered = 0, charge=0;
            if (water){ litersDelivered = wasser.allocateWaterLiters(liters); charge += serviceWaterPrice(t, litersDelivered); }
            if (protection){ bodyguard.protectCitizen(c.id); charge += serviceProtectionPrice(t); }
            if (charge>0 && t!=EquityTier.NEEDY) payments.route(charge, Math.max(0, charge*0.15));
            else System.out.println("Aid gratis für "+c.id+" ("+t+")");
            return new AidOutcome(litersDelivered, protection, charge);
        }
        private static double serviceWaterPrice(EquityTier t, double liters){ double base = liters*0.5; if (t==EquityTier.NEEDY) return 0; if (t==EquityTier.WEALTHY) return base*1.5; return base; }
        private static double serviceProtectionPrice(EquityTier t){ double base=200; if (t==EquityTier.NEEDY) return 0; if (t==EquityTier.WEALTHY) return base*2; return base; }
    }

    /* ============================== WALLET / PAYMENTS ===================== */
    static final class WalletSuite {
        void card(){
            System.out.println("[Wallets]");
            System.out.println(" - IBAN: "+Contact.IBAN);
            System.out.println(" - BTC ••••"+Contact.BTC.substring(Contact.BTC.length()-6));
            System.out.println(" - ETH ••••"+Contact.ETH.substring(Contact.ETH.length()-6)+"\n");
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
            return String.join("\n", Arrays.asList("BCD",version,encoding,service,bic,name,iban.replace(" ",""),amount,"",r,"",""));
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
                BufferedImage img = new BufferedImage(w,h, BufferedImage.TYPE_INT_RGB);
                Method get = bmClass.getMethod("get", int.class, int.class);
                for (int y=0; y<h; y++) for (int x=0; x<w; x++) {
                    boolean on = (boolean) get.invoke(bitMatrix, x, y);
                    img.setRGB(x, y, on ? 0x000000 : 0xFFFFFF);
                }
                ImageIO.write(img, "png", out);
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
    static final class WalletRueckspeisung { final String iban; double saldo=0; WalletRueckspeisung(String i){iban=i;} void gutschreiben(double e){ saldo+=e; System.out.println("Rückspeisung an IBAN "+iban+" +"+euro(e)); } }
    static final class CloudBackfeed { void feed(double e){ System.out.println("Cloud-Rückspeisung +"+euro(e)); } }
    static final class SiloManager { void cloud(double e){ System.out.println("Cloud-Silo +"+euro(e)); } void wallet(double e){ System.out.println("Wallet-Silo +"+euro(e)); } }
    static final class FinancePolicy {
        double reserveTarget=500.0, wWallet=0.45, wCloud=0.00, wTech=0.35, wAid=0.20;
        static FinancePolicy def(){ return new FinancePolicy(); }
    }
    static final class BalancedPaymentProcessor {
        final WalletRueckspeisung wallet; final CloudBackfeed cloud; final SiloManager silos; final FinancePolicy p; double reserve=0;
        BalancedPaymentProcessor(WalletRueckspeisung w, CloudBackfeed c, SiloManager s, FinancePolicy p){ this.wallet=w; this.cloud=c; this.silos=s; this.p=p; }
        static BalancedPaymentProcessor defaultFor(String iban){ return new BalancedPaymentProcessor(new WalletRueckspeisung(iban), new CloudBackfeed(), new SiloManager(), FinancePolicy.def()); }
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

    /* ============================== DIENSTPLÄNE ============================ */
    static final class Staff { final String name, role; Staff(String n,String r){name=n;role=r;} }
    static final class Shift { final LocalDate date; final String slot; final Staff staff; Shift(LocalDate d,String s,Staff st){date=d;slot=s;staff=st;} }
    static final class DutyRoster {
        final List<Staff> members = new ArrayList<>();
        final List<Shift> shifts  = new ArrayList<>();
        void addMember(Staff s){ members.add(s); }
        void scheduleWeek(LocalDate start){
            String[] slots = {"08:00-16:00","16:00-00:00","00:00-08:00"};
            int idx = 0;
            for (int d=0; d<7; d++){
                LocalDate day = start.plusDays(d);
                for (String slot : slots){
                    Staff s = members.get(idx % members.size());
                    shifts.add(new Shift(day, slot, s));
                    idx++;
                }
            }
            System.out.println("[Dienstplan] Woche geplant ab "+start);
        }
        File exportICS(String filename){
            StringBuilder sb = new StringBuilder();
            sb.append("BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//SecureOpsRoster//EN\n");
            for (Shift sh : shifts){
                LocalDateTime start = parseTime(sh.date, sh.slot.split("-")[0]);
                LocalDateTime end   = parseTime(sh.date, sh.slot.split("-")[1]);
                if (end.isBefore(start)) end = end.plusDays(1); // Nacht
                String uid=UUID.randomUUID()+"@secureops";
                sb.append("BEGIN:VEVENT\nUID=").append(uid).append("\n")
                  .append("DTSTAMP:").append(fmt(LocalDateTime.now())).append("\n")
                  .append("DTSTART:").append(fmt(start)).append("\n")
                  .append("DTEND:").append(fmt(end)).append("\n")
                  .append("SUMMARY:").append(escape(sh.staff.role+" – "+sh.staff.name)).append("\n")
                  .append("DESCRIPTION:Dienst • Tel: ").append(Contact.PHONE_DISPLAY).append("\n")
                  .append("LOCATION:").append(escape(Contact.ADDRESS)).append("\nEND:VEVENT\n");
            }
            sb.append("END:VCALENDAR\n");
            return writeFile(filename, sb.toString());
        }
        private static LocalDateTime parseTime(LocalDate d,String hhmm){ String[] p=hhmm.split(":"); return d.atTime(Integer.parseInt(p[0]), Integer.parseInt(p[1])); }
        private static String fmt(LocalDateTime t){ return t.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")); }
        private static String escape(String s){ return s.replace("\\","\\\\").replace("\n","\\n").replace(",","\\,").replace(";","\\;"); }
    }

    /* ============================== OPS-STATS =============================== */
    static final class OpsStats {
        private int total = 0;
        private final Map<String,Integer> byType = new HashMap<>();
        private final List<Integer> responseMin = new ArrayList<>();

        void markIncident(String type, int minutes){
            total++; byType.put(type, byType.getOrDefault(type,0)+1); responseMin.add(minutes);
        }
        void printSummary(){
            double avg = responseMin.stream().mapToInt(i->i).average().orElse(0);
            int p90 = percentile(responseMin, 90);
            System.out.println("== OPS-STATISTIK ==");
            System.out.println("Total: "+total+" • Avg Resp: "+avg+" min • P90: "+p90+" min");
            for (Map.Entry<String,Integer> e : byType.entrySet()) System.out.println(" - "+e.getKey()+": "+e.getValue());
        }
        private static int percentile(List<Integer> data, int p){
            if (data.isEmpty()) return 0;
            List<Integer> s = new ArrayList<>(data); Collections.sort(s);
            int idx = (int)Math.ceil(p/100.0*s.size())-1; idx=Math.max(0,Math.min(idx,s.size()-1));
            return s.get(idx);
        }
    }

    /* ============================== ENERGY / SHIELD ======================== */
    static final class EnergyManager { private double total=0; void add(double e){ total+=Math.max(0,e);} void consume(double e){ total-=Math.max(0,e); if(total<0) total=0;} double getTotal(){return total;} }
    static final class PlasmaShield {
        private final EnergyManager em; PlasmaShield(EnergyManager em){this.em=em;}
        public void activate(String cause){ em.consume(100); System.out.println("Plasma-Schild aktiv (defensiv) – "+cause); }
        public void boostKurz(double e){ em.consume(Math.min(e*0.1, em.getTotal())); System.out.println("Plasma-Schild kurz verstärkt"); }
    }

    /* ============================== MARKETING ============================== */
    static final class Offer { final long id; final String title; final double price; final int stock; Offer(long id,String t,double p,int s){ this.id=id; this.title=t; this.price=p; this.stock=s; } }
    static final class MarketingSuite {
        final WalletSuite wallet;
        MarketingSuite(WalletSuite w){ this.wallet=w; }
        List<Offer> catalog(){
            return Arrays.asList(
                    new Offer(1,"SecureOps 2-in-1 (Simulation, defensiv)", 980, 100),
                    new Offer(2,"Bodycam & EvidenceLocker (Hash-Kette)",   1250, 60),
                    new Offer(3,"Humanitarian Water & Shield Bundle",      1500, 40)
            );
        }
        void salesHeartbeat(){
            System.out.println("== SALES ==");
            for (Offer o : catalog()) System.out.println(" - "+o.title+" • ab "+euro(o.price));
            System.out.println("Termin: "+Contact.PHONE_DISPLAY+" • PayPal: paypal.me/"+Contact.PAYPAL_HANDLE+"\n");
            walletDetectDemo();
        }
        void walletDetectDemo(){
            System.out.println("[Detect] IBAN → "+PaymentDetector.detect(Contact.IBAN));
            System.out.println("[Detect] BTC  → "+PaymentDetector.detect(Contact.BTC));
            System.out.println("[Detect] ETH  → "+PaymentDetector.detect(Contact.ETH));
        }
    }

    /* ====================== DERMA CARE PRO PLUS (SAFE STUB) ================= */
    // Sichere, ethische Integration: keine Bioprint-Anleitungen, nur orchestrierende Prüfschritte.
    static final class DermaCareProPlusIntegration {
        void safeInstallDemo(){
            System.out.println("== DermaCare Pro Plus – sichere Installation (Stub) ==");
            HardwareKomponenten hw = new HardwareKomponenten();
            SoftwarePakete sw     = new SoftwarePakete();
            DermaCareProPlusInstallation installer = new DermaCareProPlusInstallation();
            installer.installiereSystem(hw, sw);
        }
    }
    static final class DermaCareProPlusInstallation {
        public void installiereSystem(HardwareKomponenten hardware, SoftwarePakete software) {
            if (!hardware.pruefeKomponenten()) {
                System.out.println("Fehler: Hardware-Komponenten unvollständig oder defekt."); return;
            }
            hardware.assembliereGewebeDrucker();           // Stub: keine sensiblen Details
            hardware.installiereScannerKomponenten();
            hardware.richteSteuerungseinheitEin();
            software.konfiguriereNetzwerk();
            software.installiereAnalyseSoftware();
            software.integriereEthikUndDatenschutz();
            if (!software.fuehreSystemtestsDurch()) { System.out.println("Fehler: Systemtests fehlgeschlagen."); return; }
            System.out.println("Installation & Konfiguration erfolgreich (Demo/Stub).");
        }
    }
    static final class HardwareKomponenten {
        public boolean pruefeKomponenten(){ System.out.println("HW-Prüfung OK (Stub)"); return true; }
        public void assembliereGewebeDrucker(){ System.out.println("Einheit zusammengeführt (ohne Details)"); }
        public void installiereScannerKomponenten(){ System.out.println("Scanner montiert (CE/Datenschutz)"); }
        public void richteSteuerungseinheitEin(){ System.out.println("Steuerung initialisiert (Audit-Logs an)"); }
    }
    static final class SoftwarePakete {
        public void konfiguriereNetzwerk(){ System.out.println("Netzwerk konfiguriert (TLS, RBAC)"); }
        public void installiereAnalyseSoftware(){ System.out.println("Analyse-Module installiert (Read-Only)"); }
        public void integriereEthikUndDatenschutz(){ System.out.println("Ethik/Privacy Filter aktiv (PII-Minimierung)"); }
        public boolean fuehreSystemtestsDurch(){ System.out.println("Systemtests PASS (Stub)"); return true; }
    }

    /* ============================== HELPERS ================================ */
    static String euro(double v){ return "€"+String.format(Locale.US,"%.2f",v); }
    static File writeFile(String name, String content){
        try (Writer w = new OutputStreamWriter(new FileOutputStream(name), StandardCharsets.UTF_8)){ w.write(content); }
        catch(Exception e){ System.out.println("Datei-Fehler: "+e.getMessage()); }
        return new File(name);
    }
    static final class AppointmentScheduler {
        static File create(LocalDateTime start, String title, String description){
            LocalDateTime end = start.plusMinutes(45);
            String uid = UUID.randomUUID()+"@secureops";
            String ics = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//SecureOps//EN\nBEGIN:VEVENT\n"
                    + "UID:"+uid+"\nDTSTAMP:"+fmt(LocalDateTime.now())+"\n"
                    + "DTSTART:"+fmt(start)+"\nDTEND:"+fmt(end)+"\n"
                    + "SUMMARY:"+esc(title)+"\nDESCRIPTION:"+esc(description)+"\\nTel: "+Contact.PHONE_DISPLAY+"\n"
                    + "LOCATION:"+esc(Contact.ADDRESS)+"\nEND:VEVENT\nEND:VCALENDAR\n";
            return writeFile("secureops_meeting.ics", ics);
        }
        private static String fmt(LocalDateTime t){ return t.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")); }
        private static String esc(String s){ return s.replace("\\","\\\\").replace("\n","\\n").replace(",","\\,").replace(";","\\;"); }
    }
}
