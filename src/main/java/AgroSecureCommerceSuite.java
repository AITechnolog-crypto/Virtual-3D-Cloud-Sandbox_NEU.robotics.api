import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;
import javax.imageio.ImageIO;

/**
 * AgroSecureCommerceSuite (offline, single file)
 * ----------------------------------------------
 * Fusion: SecureSwarmStandalone  ×  AgroKommerzOS_Pro  ×  GreenBoots
 * - Biometrie-Gate (SHA-256, keine echte DNA)
 * - Finance/Commerce: Marketplace, Kunden/CRM, Orders, KYC (sim)
 * - BalancedPaymentProcessor: Reserve → Quoten (Wallet/Orbit/Tech/Aid), nie < 0
 * - QR/SEPA: BTC/ETH/SEPA-Payload, PNG via ZXing (optional)
 * - Social Equity + Aid (Wasser)
 * - Feature-Flags: cloud/marketing/kopplung
 * - Entrepreneur-Verkaufsloop (Dauerschleife)
 * - Snapshots/Ledger in ./data
 *
 * Build:
 *   javac AgroSecureCommerceSuite.java
 *
 * Enroll:
 *   java AgroSecureCommerceSuite enroll <SHA256_HEX>
 *
 * Run (Beispiel):
 *   java AgroSecureCommerceSuite run <SHA256_HEX> zone=FARMLAND units=10000000000 \
 *     flags=cloud:off,marketing:on,kopplung:on reserve=500 split=0.40,0.20,0.30,0.10 qrs=on \
 *     phone="+43-000-0000000" callback=true
 */
public class AgroSecureCommerceSuite {

    /* ============== ENV/UTIL ============== */
    static final Logger LOG = Logger.getLogger("AgroSecureCommerceSuite");
    static final Path   DATA = Paths.get("data");
    static void ensure(){ try{ Files.createDirectories(DATA);}catch(Exception e){ throw new RuntimeException(e);} }
    static String now(){ return LocalDateTime.now().toString(); }
    static String money(double v){ return String.format(Locale.US,"%.2f", v); }
    static String round(double v){ return String.format(Locale.US,"%.1f", v); }
    static Map<String,String> parseArgs(String[] a){ Map<String,String> m=new HashMap<>();
        for(String s:a){ if(s.contains("=")){ int i=s.indexOf('='); m.put(s.substring(0,i), s.substring(i+1)); } else { m.put("mode", s);} } return m; }
    static long parseLong(String s,long d){ try{ return Long.parseLong(s);}catch(Exception e){ return d;} }
    static double parseDouble(String s,double d){ try{ return Double.parseDouble(s);}catch(Exception e){ return d;} }
    static double clamp(double v,double lo,double hi){ return Math.max(lo, Math.min(hi,v)); }

    static String sha256(String s){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb=new StringBuilder(); for(byte b:d) sb.append(String.format("%02x", b)); return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }

    /* ============== BIOMETRIE ============== */
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

    /* ============== Feature Flags / Config ============== */
    static final class FeatureFlags {
        boolean cloud=false, marketing=true, kopplung=true;
        static FeatureFlags from(String s){
            FeatureFlags f=new FeatureFlags(); if(s==null) return f;
            for(String p:s.split(",")){
                String[] kv=p.split(":"); if(kv.length!=2) continue;
                boolean on = kv[1].equalsIgnoreCase("on")||kv[1].equalsIgnoreCase("true")||kv[1].equals("1");
                switch(kv[0].toLowerCase(Locale.ROOT)){
                    case "cloud" -> f.cloud=on;
                    case "marketing" -> f.marketing=on;
                    case "kopplung" -> f.kopplung=on;
                }
            }
            return f;
        }
    }

    /* ============== PAYMENT ADDRESSES / DETECTOR / QR ============== */
    static final class PaymentAddressBook {
        static final String ETH = "0x40dEa729f32481A707917AcBBD9eaA84AcB66367";
        static final String BTC = "bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd";
        static final String IBAN= "AT921400057010099023";
        static final String RECEIVER = "AgroSecure Suite";
    }
    enum PaymentKind { IBAN_EUR, BITCOIN, ETHEREUM, UNKNOWN }
    static final class PaymentDetector {
        private static final String IBAN_RE="^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$";
        private static final String ETH_RE ="^0x[0-9a-fA-F]{40}$";
        private static final String BTC_RE ="^(bc1|[13])[a-zA-HJ-NP-Z0-9]{25,90}$";
        static PaymentKind detect(String s){
            if(s==null) return PaymentKind.UNKNOWN; String x=s.replace(" ","");
            if(x.matches(ETH_RE)) return PaymentKind.ETHEREUM;
            if(x.matches(BTC_RE)) return PaymentKind.BITCOIN;
            if(x.matches(IBAN_RE)) return PaymentKind.IBAN_EUR;
            return PaymentKind.UNKNOWN;
        }
    }
    static final class QrPayloadFactory {
        static String bitcoinUri(String address, Double amount){
            if(amount==null) return "bitcoin:"+address;
            NumberFormat nf=NumberFormat.getInstance(Locale.US); nf.setGroupingUsed(false); nf.setMaximumFractionDigits(8);
            return "bitcoin:"+address+"?amount="+nf.format(amount);
        }
        static String ethereumUri(String address){ return "ethereum:"+address; }
        static String epcSepaPayload(String name,String iban,Double amountEUR,String rem){
            String version="001", encoding="1", service="SCT", bic="";
            String amount=(amountEUR==null)?"":"EUR"+String.format(Locale.US,"%.2f", Math.max(0.01, amountEUR));
            String r=(rem==null)?"":rem;
            return String.join("\n", Arrays.asList("BCD",version,encoding,service,bic,name,iban.replace(" ",""),amount,"",r,"","") );
        }
    }
    static final class QrPngWriter {
        @SuppressWarnings("unchecked")
        static boolean writePngIfZXing(String payload, File out, int size){
            try{
                Class<?> bfClass=Class.forName("com.google.zxing.BarcodeFormat");
                Object qrFormat=Enum.valueOf((Class<Enum>)bfClass.asSubclass(Enum.class), "QR_CODE");
                Class<?> writerCls=Class.forName("com.google.zxing.qrcode.QRCodeWriter");
                Object writer=writerCls.getDeclaredConstructor().newInstance();
                Method encode=writerCls.getMethod("encode", String.class, bfClass, int.class, int.class, Map.class);
                Object bitMatrix=encode.invoke(writer, payload, qrFormat, size, size, null);
                Class<?> bmClass=Class.forName("com.google.zxing.common.BitMatrix");
                int w=(int)bmClass.getMethod("getWidth").invoke(bitMatrix);
                int h=(int)bmClass.getMethod("getHeight").invoke(bitMatrix);
                BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
                Method get=bmClass.getMethod("get", int.class, int.class);
                for(int y=0;y<h;y++) for(int x=0;x<w;x++){ boolean on=(boolean)get.invoke(bitMatrix,x,y); img.setRGB(x,y, on?0x000000:0xFFFFFF);} 
                ImageIO.write(img, "png", out);
                System.out.println("[QR] PNG → "+out.getAbsolutePath());
                return true;
            }catch(ClassNotFoundException e){
                System.out.println("[QR] ZXing nicht im Classpath – zeige Payload als Text.");
                return false;
            }catch(Throwable t){
                System.out.println("[QR] Fehler: "+t.getMessage());
                return false;
            }
        }
        static void printFallback(String title,String payload){ System.out.println("["+title+"]\n"+payload+"\n"); }
    }
    static final class CryptoArtRegistry {
        static void print(){
            System.out.println("[CRYPTO-ART]");
            System.out.println(" - BTC ••••"+PaymentAddressBook.BTC.substring(PaymentAddressBook.BTC.length()-6)+" (Bitcoin)");
            System.out.println(" - ETH ••••"+PaymentAddressBook.ETH.substring(PaymentAddressBook.ETH.length()-6)+" (Ethereum)\n");
        }
    }
    static final class PaymentsAddon {
        static void generateAllQrs(boolean on){
            if(!on) return;
            String btc = QrPayloadFactory.bitcoinUri(PaymentAddressBook.BTC, 0.001);
            if(!QrPngWriter.writePngIfZXing(btc, new File("qr_btc.png"), 640)) QrPngWriter.printFallback("BTC (BIP-21)", btc);
            String eth = QrPayloadFactory.ethereumUri(PaymentAddressBook.ETH);
            if(!QrPngWriter.writePngIfZXing(eth, new File("qr_eth.png"), 640)) QrPngWriter.printFallback("ETH (ethereum:)", eth);
            String epc = QrPayloadFactory.epcSepaPayload(PaymentAddressBook.RECEIVER, PaymentAddressBook.IBAN, 25.00, "AgroSecure Support");
            if(!QrPngWriter.writePngIfZXing(epc, new File("qr_sepa.png"), 640)) QrPngWriter.printFallback("SEPA/EPC (BCD)", epc);
        }
    }

    /* ============== Wallets / Donations / Finance Policy ============== */
    enum Fund { HUMANITAER, RND_TECH }
    interface PaymentGateway { String createCheckout(Fund fund,long amountCents,String currency); }
    static final class PayPalGateway implements PaymentGateway {
        public String rndLink="https://www.paypal.me/SanelCrnkic";
        public String ngoLink="https://www.paypal.me/SanelCrnkic";
        public String createCheckout(Fund fund,long cents,String cur){
            String base = (fund==Fund.RND_TECH? rndLink : ngoLink);
            return base+"?amount="+String.format(Locale.US,"%.2f",cents/100.0)+"&currency_code="+cur;
        }
    }
    static final class BankGateway implements PaymentGateway {
        private final String ibanRND=PaymentAddressBook.IBAN, ibanNGO="LT723250010548966150";
        public String createCheckout(Fund fund,long cents,String cur){
            String iban=(fund==Fund.RND_TECH?ibanRND:ibanNGO);
            String purpose="DON-"+fund+"-"+UUID.randomUUID().toString().substring(0,8);
            return "bank://SEPA?iban="+iban.replace(" ","")+"&amount="+(cents/100.0)+"&currency="+cur+"&purpose="+purpose;
        }
    }
    static final class DonationLedger {
        private final List<String> rows=new ArrayList<>();
        DonationLedger(){ rows.add("time,provider,ref,fund,amount,currency"); }
        synchronized void record(String provider,String ref,Fund fund,long cents,String cur){
            rows.add(new Date()+","+provider+","+ref+","+fund+","+(cents/100.0)+","+cur);
        }
        String csv(){ return String.join("\n", rows); }
    }
    static final class Wallet { public final String label; public final String target; private double credited=0;
        Wallet(String label,String target){ this.label=label; this.target=target; }
        public void credit(double amount){ credited+=amount; System.out.println("[WALLET] "+label+" +"+money(amount)+" -> "+target); }
        public double getTotal(){ return credited; }
    }
    static final class DonationRouter {
        private final Wallet tech, aid; private final DonationLedger ledger;
        private final PayPalGateway pp=new PayPalGateway(); private final BankGateway bank=new BankGateway();
        private final AtomicLong seq=new AtomicLong(1);
        DonationRouter(Wallet tech, Wallet aid, DonationLedger ledger){ this.tech=tech; this.aid=aid; this.ledger=ledger; }
        String checkoutLink(String provider,Fund fund,long cents,String cur){
            if("paypal".equalsIgnoreCase(provider)) return pp.createCheckout(fund,cents,cur);
            if("bank".equalsIgnoreCase(provider))   return bank.createCheckout(fund,cents,cur);
            throw new IllegalArgumentException("Provider unbekannt: "+provider);
        }
        void capture(String provider,Fund fund,long cents,String cur){
            String ref=provider.toUpperCase(Locale.ROOT)+"-"+seq.getAndIncrement();
            if(fund==Fund.RND_TECH) tech.credit(cents/100.0); else aid.credit(cents/100.0);
            ledger.record(provider,ref,fund,cents,cur);
        }
    }

    static final class EnergyManager { private double total=0; public void add(double e){ total+=Math.max(0,e);} public void consume(double e){ total-=Math.max(0,e); if(total<0) total=0; } public double getTotal(){return total;} }
    static final class SatellitenResonanzVerbindung { public void aktiviere(){ System.out.println("Satelliten-Resonanz aktiv"); } public void rueckspeisenOrbit(double e){ System.out.println("Orbit-Rückspeisung +"+money(e)); } }
    static final class SiloManager { private double cloud=0,wallet=0; public void speichereInCloud(double e){ cloud+=e; System.out.println("Cloud-Silo +"+money(e)); } public void speichereInWallet(double e){ wallet+=e; System.out.println("Wallet-Silo +"+money(e)); } }
    static final class WalletRueckspeisung { private final String iban; private double saldo=0; WalletRueckspeisung(String iban){ this.iban=iban; } public void gutschreiben(double e){ saldo+=e; System.out.println("Rückspeisung an IBAN "+iban+" +"+money(e)); } }

    static final class FinancePolicy {
        double reserveTarget=300.0;
        double wWallet=0.50, wOrbit=0.20, wTech=0.20, wAid=0.10;
        FinancePolicy reserveTargetEUR(double v){ reserveTarget=Math.max(0,v); return this; }
        FinancePolicy weights(double wallet,double orbit,double tech,double aid){
            double sum=Math.max(0,wallet)+Math.max(0,orbit)+Math.max(0,tech)+Math.max(0,aid);
            if(sum==0){ wWallet=1; wOrbit=wTech=wAid=0; }
            else { wWallet=wallet/sum; wOrbit=orbit/sum; wTech=tech/sum; wAid=aid/sum; }
            return this;
        }
        public String toString(){ return String.format(Locale.US,"Policy{reserve=%.2f, split W/O/T/A=%.0f/%.0f/%.0f/%.0f%%}", reserveTarget,100*wWallet,100*wOrbit,100*wTech,100*wAid); }
    }

    static final class BalancedPaymentProcessor {
        private final WalletRueckspeisung wallet;
        private final SatellitenResonanzVerbindung orbit;
        private final SiloManager silos;
        private final DonationRouter donations;
        private final FinancePolicy policy;
        private double reserve=0.0;

        BalancedPaymentProcessor(WalletRueckspeisung w, SatellitenResonanzVerbindung o, SiloManager s, DonationRouter d, FinancePolicy p){
            this.wallet=w; this.orbit=o; this.silos=s; this.donations=d; this.policy=p;
            System.out.println("[FinancePolicy] "+p);
        }
        /** Einnahmen → Kosten → Reserve → Quoten; nie negativ. */
        void route(double revenue, double opCost){
            revenue=Math.max(0,revenue); opCost=Math.max(0,opCost);
            System.out.println(String.format(Locale.US,"[Route] € in=%.2f  op=%.2f  reserve=%.2f", revenue, opCost, reserve));
            double margin = revenue - opCost;
            if(margin < 0){
                double need=-margin;
                if(reserve>=need){ reserve-=need; System.out.println(String.format(Locale.US,"[Route] Defizit %.2f aus Reserve → %.2f",need,reserve)); margin=0; }
                else { System.out.println(String.format(Locale.US,"[Route] Defizit %.2f nicht gedeckt (Reserve %.2f). Keine Ausschüttung.",need,reserve)); return; }
            }
            double needRes=Math.max(0, policy.reserveTarget - reserve);
            double toRes=Math.min(needRes, margin);
            reserve += toRes; double rest = Math.max(0, margin - toRes);
            if(toRes>0) System.out.println(String.format(Locale.US,"[Route] Reserve +%.2f → %.2f", toRes, reserve));
            if(rest<=0){ System.out.println("[Route] Kein Rest nach Reserve."); return; }

            double toWallet = rest*policy.wWallet;
            double toOrbit  = rest*policy.wOrbit;
            double toTech   = rest*policy.wTech;
            double toAid    = rest*policy.wAid;

            if(toWallet>0){ wallet.gutschreiben(toWallet); silos.speichereInWallet(toWallet); }
            if(toOrbit>0){ orbit.rueckspeisenOrbit(toOrbit); silos.speichereInCloud(toOrbit); }
            if(toTech>0){ donations.capture("bank", Fund.RND_TECH,   Math.round(toTech*100), "EUR"); }
            if(toAid>0){ donations.capture("bank", Fund.HUMANITAER, Math.round(toAid*100), "EUR"); }

            System.out.println(String.format(Locale.US,"[Route] verteilt → Wallet=%.2f, Orbit=%.2f, Tech=%.2f, Aid=%.2f", toWallet,toOrbit,toTech,toAid));
        }
    }

    /* ============== CRM / Customers / Orders ============== */
    enum Tier { NEEDY, STANDARD, WEALTHY }
    static final class Customer {
        final String id;
        String name, email, phone;
        Tier tier; double reputation, incomeIdx;
        int loyalty=0;
        Customer(String id,String name,String email,String phone,Tier tier,double rep,double inc){
            this.id=id; this.name=name; this.email=email; this.phone=phone; this.tier=tier; this.reputation=rep; this.incomeIdx=inc;
        }
    }
    static final class CRM {
        final Map<String,Customer> byId=new HashMap<>();
        final Path file = DATA.resolve("customers.csv");
        CRM(){ try{ if(!Files.exists(file)) Files.writeString(file, "time,id,name,email,phone,tier,reputation,income\n"); }catch(Exception ignored){} }
        Customer upsertRandom(){
            String id="cust-"+UUID.randomUUID().toString().substring(0,8);
            Random r=new Random();
            Tier t = (r.nextDouble()<0.2)?Tier.WEALTHY: (r.nextDouble()<0.5?Tier.STANDARD:Tier.NEEDY);
            double rep=0.4+r.nextDouble()*0.6, inc=0.2+r.nextDouble()*0.8;
            Customer c=new Customer(id,"Buyer "+id,"buyer@ex.ampl","",t,rep,inc);
            byId.put(id,c); append(c); return c;
        }
        void append(Customer c){
            try{ Files.writeString(file, String.join(",", now(), c.id, c.name, c.email, c.phone, String.valueOf(c.tier), String.valueOf(c.reputation), String.valueOf(c.incomeIdx))+"\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);}catch(Exception ignored){}
        }
    }
    static final class Orders {
        final Path file = DATA.resolve("orders.csv");
        Orders(){ try{ if(!Files.exists(file)) Files.writeString(file, "time,orderId,customerId,product,kg,pricePerKg,revenue\n"); }catch(Exception ignored){} }
        void record(String oid,String cid,String product,int kg,double ppk,double rev){
            try{ Files.writeString(file, String.join(",", now(), oid, cid, product, String.valueOf(kg), money(ppk), money(rev))+"\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);}catch(Exception ignored){}
        }
    }

    /* ============== Marketplace / Regulator ============== */
    static final class Offer { final long id; final String name; double base; int stockKg;
        static final AtomicLong SEQ=new AtomicLong(1);
        Offer(String n,double b,int s){ id=SEQ.getAndIncrement(); name=n; base=b; stockKg=s; } }
    static final class Regulator {
        final Set<String> whitelist=new HashSet<>(List.of("Wheat","Corn"));
        final Map<String,Double> floor = Map.of("Wheat",0.55,"Corn",0.40);
        final int dailyKgLimit=4000;
        final AtomicLong soldTodayKg=new AtomicLong(0);
        LocalDate day=LocalDate.now();
        boolean kycOk(double reputation){ return reputation>=0.5; }
        synchronized boolean allowSell(String product,int kg,double price){
            if(!LocalDate.now().equals(day)){ day=LocalDate.now(); soldTodayKg.set(0); }
            if(!whitelist.contains(product)) return false;
            if(price < floor.getOrDefault(product, 0.0)) return false;
            if(soldTodayKg.get()+kg > dailyKgLimit) return false;
            soldTodayKg.addAndGet(kg); return true;
        }
    }
    static final class SocialEquity {
        Tier classify(double incomeIdx){ if(incomeIdx>=0.75) return Tier.WEALTHY; if(incomeIdx<=0.35) return Tier.NEEDY; return Tier.STANDARD; }
        double buyerPrice(Tier t,double p){ return switch(t){ case NEEDY->Math.max(0,p*0.9); case WEALTHY->p*1.2; default->p; }; }
    }
    static final class Marketplace {
        final Map<Long,Offer> offers=new LinkedHashMap<>();
        final Map<Long,Integer> demand=new HashMap<>();
        final Random r=new Random();
        void add(Offer o){ offers.put(o.id,o); }
        double demandIndex(long id){ int d=demand.getOrDefault(id,0)+1; demand.put(id,d); return 1.0+Math.min(1.2, d*0.04); }
        /** Verkauf inkl. CRM/KYC/Regulator – gibt Revenue zurück. */
        double sell(Offer o, int kg, Customer buyer, SocialEquity eq, Regulator reg, boolean marketing, Orders orders){
            if(o==null || kg<=0 || o.stockKg<=0) return 0;
            int sell=Math.min(kg,o.stockKg);
            if(!reg.kycOk(buyer.reputation)){ LOG.info("[KYC] abgelehnt für "+buyer.id); return 0; }

            double dyn=o.base*demandIndex(o.id);
            if(marketing) dyn*=1.05;
            Tier tier = eq.classify(buyer.incomeIdx);
            double ppk = clamp(eq.buyerPrice(tier, dyn), o.base*0.7, o.base*1.9);

            if(!reg.allowSell(o.name, sell, ppk)){ LOG.info("[REG] Verkauf blockiert"); return 0; }

            double revenue = sell*ppk;
            o.stockKg -= sell;
            buyer.loyalty += Math.max(1, (int)(revenue/50.0));
            String oid="ORD-"+UUID.randomUUID().toString().substring(0,8);
            orders.record(oid, buyer.id, o.name, sell, ppk, revenue);
            LOG.info(String.format(Locale.US,"[SALE] %s %dkg @%.2f → %.2f (tier=%s, loyalty=%d, stockLeft=%d)",
                    o.name, sell, ppk, revenue, tier, buyer.loyalty, o.stockKg));
            return revenue;
        }
    }

    /* ============== Silos/Store & Aid ============== */
    static final class Silos {
        double waterLiters=2500, energyUnits=1200, grainKg=800;
        synchronized boolean useGrain(int kg){ if(kg<=0||kg>grainKg) return false; grainKg-=kg; return true; }
        synchronized void addEnergy(double e){ energyUnits+=Math.max(0,e); }
        synchronized void addWater(double l){ waterLiters+=Math.max(0,l); }
    }
    static final class AidRouter {
        final SocialEquity eq; final Silos s; final Wallet ops;
        AidRouter(SocialEquity e,Silos s,Wallet ops){ this.eq=e; this.s=s; this.ops=ops; }
        void waterAid(double liters,double incomeIdx){
            Tier t=eq.classify(incomeIdx); if(s.waterLiters<liters){ LOG.info("[AID] nicht genug Wasser"); return; }
            s.waterLiters -= liters;
            double charge = (t==Tier.NEEDY)?0 : (t==Tier.WEALTHY? liters*0.032 : liters*0.02);
            if(charge>0) ops.credit(charge);
            LOG.info(String.format(Locale.US,"[AID] %.0fL geliefert (tier=%s, charge=%.2f)", liters, t, charge));
        }
    }

    /* ============== Sensors/KI/Actuators (kompakt) ============== */
    static final class SensorData { final String type; final double value; SensorData(String t,double v){type=t;value=v;} }
    static final class Sensors {
        final Random r=new Random();
        List<SensorData> read(){
            return List.of(
                new SensorData("soil_moisture", 30+r.nextGaussian()*7),
                new SensorData("air_temp_c",    20+r.nextGaussian()*5),
                new SensorData("sun_intensity", clamp(0.7+r.nextGaussian()*0.15, 0,1)),
                new SensorData("cistern_liters",2000+r.nextGaussian()*400),
                new SensorData("rain_forecast", Math.max(0, r.nextGaussian()*3+1))
            );
        }
    }
    static final class IrrigationPlan { final int liters; IrrigationPlan(int l){liters=l;} }
    static final class EnergyPlan { final double demand; EnergyPlan(double d){demand=d;} }
    static final class Decision { final IrrigationPlan ip; final EnergyPlan ep; Decision(IrrigationPlan i,EnergyPlan e){ip=i;ep=e;} }

    static final class KI {
        Decision decide(List<SensorData> d, Silos s, boolean koppel){
            double soil=v(d,"soil_moisture"), sun=v(d,"sun_intensity"), tank=v(d,"cistern_liters"), rain=v(d,"rain_forecast");
            double need=0; if(soil<28 && rain<2) need=600; else if(soil<35 && rain<3) need=320;
            if(tank<800) need*=0.5; if(sun>0.8) need*=1.1; if(!koppel) need*=1.06;
            IrrigationPlan ip=(need>0)? new IrrigationPlan((int)Math.round(need)) : null;
            double demand=40+(sun*30); if(!koppel) demand*=0.95;
            return new Decision(ip, new EnergyPlan(demand));
        }
        double v(List<SensorData> d,String key){ return d.stream().filter(x->x.type.equals(key)).mapToDouble(x->x.value).findFirst().orElse(0); }
    }
    static final class IrrigationSystem {
        final Silos s; IrrigationSystem(Silos s){this.s=s;}
        void execute(IrrigationPlan p){ if(p==null) return; if(s.waterLiters>=p.liters){ s.waterLiters-=p.liters; LOG.info("Bewässerung "+p.liters+"L"); } else LOG.warning("Zisterne leer"); }
    }
    static final class SolarSystem {
        final Silos s; SolarSystem(Silos s){this.s=s;}
        void execute(EnergyPlan e, boolean koppel){ double cover=koppel?14:10; if(s.energyUnits < e.demand-cover) LOG.warning("Energie knapp"); s.addEnergy(koppel?11:8); }
    }

    /* ============== Cloud (off) & Snapshot ============== */
    static final class CloudSync { final boolean on; CloudSync(boolean on){this.on=on;} void maybeSync(){ if(!on) return; /* placeholder: no network */ } }
    static final class Snapshot {
        static void write(State st){
            String js="{"+
              "\"ts\":\""+now()+"\","+
              "\"flags\":{\"cloud\":"+st.flags.cloud+",\"marketing\":"+st.flags.marketing+",\"kopplung\":"+st.flags.kopplung+"},"+
              "\"wallets\":{\"owner\":"+money(st.wOwner.getTotal())+",\"ops\":"+money(st.wOps.getTotal())+",\"aid\":"+money(st.wAid.getTotal())+"},"+
              "\"silos\":{\"water\":"+round(st.silos.waterLiters)+",\"energy\":"+round(st.silos.energyUnits)+",\"grain\":"+round(st.silos.grainKg)+"},"+
              "\"reg\":{\"soldTodayKg\":"+st.reg.soldTodayKg.get()+",\"dailyLimit\":"+st.reg.dailyKgLimit+"},"+
              "\"contacts\":{\"name\":\""+st.contacts.ownerName+"\",\"email\":\""+st.contacts.email+"\",\"phone\":\""+st.contacts.phone+"\",\"callback\":"+st.contacts.wantsCallback+"}"
            +"}";
            try{ Files.writeString(DATA.resolve("state.json"), js, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);}catch(Exception ignored){}
        }
    }

    /* ============== Contacts/Business Card ============== */
    static final class Contacts {
        String ownerName="Sanel Crnkic", email="gpt99@iCloud.com", phone=""; boolean wantsCallback=false;
        Path file(){ return DATA.resolve("contacts.json"); }
        void snapshot(){
            String js="{\"owner\":\""+ownerName+"\",\"email\":\""+email+"\",\"phone\":\""+phone+"\",\"callback\":"+wantsCallback+"}";
            try{ Files.writeString(file(), js, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);}catch(Exception ignored){}
        }
    }

    /* ============== Suite State ============== */
    static final class State {
        final FeatureFlags flags;
        final Contacts contacts;
        final CloudSync cloud;
        final Sensors sensors=new Sensors();
        final KI ki=new KI();
        final Silos silos=new Silos();
        final SocialEquity equity=new SocialEquity();
        final Regulator reg=new Regulator();
        final Marketplace market=new Marketplace();
        final Orders orders=new Orders();
        final CRM crm=new CRM();

        final Wallet wOwner=new Wallet("OWNER", PaymentAddressBook.IBAN);
        final Wallet wOps  =new Wallet("OPS",   "OPS Ledger (SIM)");
        final Wallet wAid  =new Wallet("AID",   "Humanitarian (SIM)");

        final DonationLedger dLedger=new DonationLedger();
        final DonationRouter donations=new DonationRouter(new Wallet("TECH","R&D (SIM)"), new Wallet("AID","Humanitarian (SIM)"), dLedger);

        final WalletRueckspeisung bankWallet=new WalletRueckspeisung(PaymentAddressBook.IBAN);
        final SatellitenResonanzVerbindung orbit=new SatellitenResonanzVerbindung();
        final SiloManager siloMgr=new SiloManager();
        final BalancedPaymentProcessor payments;

        final IrrigationSystem irrigation=new IrrigationSystem(silos);
        final SolarSystem solar=new SolarSystem(silos);

        State(FeatureFlags flags, Contacts contacts, FinancePolicy policy){
            this.flags=flags; this.contacts=contacts; this.cloud=new CloudSync(flags.cloud);
            this.payments = new BalancedPaymentProcessor(bankWallet, orbit, siloMgr, donations, policy);
            market.add(new Offer("Wheat",0.85,3000));
            market.add(new Offer("Corn", 0.65,2200));
            wOps.credit(120); // bootstrap
            orbit.aktiviere();
        }
    }

    /* ============== MAIN ENGINE ============== */
    public static void main(String[] args) throws Exception {
        ensure();
        LOG.info("AgroSecureCommerceSuite booting…");

        Map<String,String> cli=parseArgs(args);
        String mode=cli.getOrDefault("mode","run");

        Biometry bio=new Biometry();
        if("enroll".equalsIgnoreCase(mode)){
            if(args.length<2){ System.out.println("Usage: java AgroSecureCommerceSuite enroll <SHA256_HEX>"); return; }
            bio.enroll(args[1].trim().toLowerCase());
            System.out.println("Enrolled marker = "+args[1]); return;
        }
        if(!bio.load()){ System.out.println("Kein Marker registriert. Bitte zuerst:\n  java AgroSecureCommerceSuite enroll <dein_SHA256_hex>"); return; }
        String provided=(args.length>1)? args[1].trim().toLowerCase() : "";
        if(!bio.check(provided)){ System.out.println("Biometrie: Zugriff verweigert (falscher Marker-Hash)."); return; }

        // Flags/Params
        FeatureFlags flags=FeatureFlags.from(cli.get("flags"));
        double reserveTarget=parseDouble(cli.get("reserve"), 300.0);
        String split=cli.getOrDefault("split","0.50,0.20,0.20,0.10");
        String[] sp=split.split(","); double w=0.5,o=0.2,t=0.2,a=0.1;
        if(sp.length==4){ w=parseDouble(sp[0],w); o=parseDouble(sp[1],o); t=parseDouble(sp[2],t); a=parseDouble(sp[3],a); }
        FinancePolicy policy=new FinancePolicy().reserveTargetEUR(reserveTarget).weights(w,o,t,a);

        boolean genQrs = "on".equalsIgnoreCase(cli.getOrDefault("qrs","off"));

        // Contacts
        Contacts contacts=new Contacts();
        if(cli.containsKey("phone")) contacts.phone=cli.get("phone");
        if("true".equalsIgnoreCase(cli.getOrDefault("callback","false"))) contacts.wantsCallback=true;
        contacts.snapshot();

        // Pretty header + QR
        System.out.println("== AgroSecure Commerce Suite ==");
        CryptoArtRegistry.print();
        PaymentsAddon.generateAllQrs(genQrs);

        // Donations quick demo
        DonationLedger tmpLedger=new DonationLedger();
        DonationRouter tmpDonations=new DonationRouter(new Wallet("TECH","R&D (SIM)"), new Wallet("AID","Humanitarian (SIM)"), tmpLedger);
        System.out.println("[Spendenlinks]");
        System.out.println(" - Humanitär/PayPal: "+tmpDonations.checkoutLink("paypal", Fund.HUMANITAER, 50_00,"EUR"));
        System.out.println(" - R&D/Bank:        "+tmpDonations.checkoutLink("bank",   Fund.RND_TECH,   120_00,"EUR"));
        tmpDonations.capture("paypal", Fund.HUMANITAER, 50_00,"EUR");
        tmpDonations.capture("bank",   Fund.RND_TECH,   120_00,"EUR");
        System.out.println("\n[Transparenz-Ledger]\n"+tmpLedger.csv()+"\n");

        // Engine state
        State st=new State(flags, contacts, policy);

        // Schedulers: Sensor/KI-Tick + Entrepreneur-Loop
        ScheduledExecutorService ses=Executors.newScheduledThreadPool(2);

        // Sensor/KI Tick
        ses.scheduleAtFixedRate(()->{
            try{
                List<SensorData> data=st.sensors.read();
                Decision dec = st.ki.decide(data, st.silos, st.flags.kopplung);
                st.irrigation.execute(dec.ip);
                st.solar.execute(dec.ep, st.flags.kopplung);

                // Kleine Reinvest durch simulierte Solar-Überschüsse
                st.silos.addEnergy(5); st.silos.addWater(20);

                // Aid: periodisch
                if((System.currentTimeMillis()/1000)%7==0){
                    new AidRouter(st.equity, st.silos, st.wOps).waterAid(160, 0.28);
                }

                st.cloud.maybeSync();
                Snapshot.write(st);

                LOG.info(String.format(Locale.US,"Silos water=%.0fL energy=%.0f grain=%.0fkg | wallets owner=%s ops=%s aid=%s",
                        st.silos.waterLiters, st.silos.energyUnits, st.silos.grainKg,
                        money(st.wOwner.getTotal()), money(st.wOps.getTotal()), money(st.wAid.getTotal())));
            }catch(Exception e){ LOG.log(Level.SEVERE,"tick error", e); }
        }, 0, 1200, TimeUnit.MILLISECONDS);

        // Entrepreneur-Verkaufsloop (Kundenbindung + Finance Routing)
        ses.scheduleAtFixedRate(()->{
            try{
                // Kunde (bestehender oder neuer)
                Customer buyer = st.crm.upsertRandom();

                // Produkt wählen
                Offer offer = st.market.offers.values().stream().filter(o->o.name.equals("Wheat") && o.stockKg>0).findFirst().orElse(null);
                if(offer==null) return;

                // Menge abhängig vom Lager
                int burst=(int)Math.min(500, Math.max(0, offer.stockKg - 600));
                if(burst<=0) return;

                // Grain aus physischem Silo abziehen (sim Aggregation)
                if(!st.silos.useGrain(burst)) return;

                double revenue = st.market.sell(offer, burst, buyer, st.equity, st.reg, st.flags.marketing, st.orders);
                double opsCost = revenue * 0.15;
                st.payments.route(revenue, opsCost);

            }catch(Exception e){ LOG.log(Level.WARNING,"entrepreneur loop error", e); }
        }, 1000, 2000, TimeUnit.MILLISECONDS);

        LOG.info("Engine läuft. Ctrl+C zum Beenden. Snapshots in ./data/state.json");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try{ ses.shutdownNow(); }catch(Exception ignored){}
            System.out.println("\nShutdown ok.");
        }));
        Thread.currentThread().join();
    }
}
