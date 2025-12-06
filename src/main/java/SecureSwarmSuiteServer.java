import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SecureSwarmSuiteServer – REST + Metrics + Files
 * ------------------------------------------------
 * - Bauanleitung/Marketing schreiben (Markdown)
 * - Wallet/QR (IBAN/BTC/ETH/SEPA/PayPal) – PNG optional via ZXing
 * - REST: /healthz, /status, /offers, /checkout, /contact (GET/PUT),
 *         /lead (POST), /call (GET), /metrics (Prometheus)
 * - Sicherheit: X-Auth Token (nur für Schreib-Endpunkte erforderlich)
 *
 * Bauen:
 *   javac SecureSwarmSuiteServer.java
 * Starten (ohne Token -> read-only Endpunkte reichen):
 *   java SecureSwarmSuiteServer --rest=8080
 * Starten (mit Token für Schreibendpunkte):
 *   java SecureSwarmSuiteServer --rest=8080 --token=meinGeheimnis \
 *     --name="ShariaBoots SecureSwarm" \
 *     --phone="+387 67 105 2799" --email="contact@secureswarm.local" \
 *     --addr="Jezerski bb, 77241 Bosanska Krupa, BA" \
 *     --site="https://secureswarm.local" \
 *     --iban="AT921400057010099023" \
 *     --btc="bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd" \
 *     --eth="0x40dEa729f32481A707917AcBBD9eaA84AcB66367"
 *
 * HINWEIS: Simulation, rein defensiv/humanitär. Reale Einsätze nur mit Recht,
 * Behördenkoordination, Einwilligung & Mensch-im-Loop.
 */
public class SecureSwarmSuiteServer {

    /* ============================== MAIN ============================== */
    public static void main(String[] args) throws Exception {
        Map<String,String> cli = parseArgs(args);

        ContactInfo contact = ContactInfo.defaults()
            .withName(cli.get("name"))
            .withPhone(cli.get("phone"))
            .withEmail(cli.get("email"))
            .withAddress(cli.get("addr"))
            .withWebsite(cli.get("site"));

        UnifiedWallet wallet = UnifiedWallet.defaults()
            .withIBAN(cli.get("iban"))
            .withBTC(cli.get("btc"))
            .withETH(cli.get("eth"));

        MarketingPlan mk = MarketingPlan.defaultPlan();
        BuildManual manual = BuildManual.defaultDefensiv();
        VizScene scene = VizScene.defaultScene();

        ensureDir("build");
        writeText("build/Bauanleitung_DE.md", manual.renderMarkdown(contact, wallet));
        writeText("build/Marketing_Plan_DE.md", mk.renderMarkdown(contact, wallet));
        writeText("build/Kontakt.json", contact.toJson());
        writeText("build/Wallet.txt", wallet.renderQrSection(0.001, 25.00, "SecureSwarm Support"));

        MetricsRegistry metrics = new MetricsRegistry();
        LeadStore leadStore = new LeadStore("build/leads.jsonl");

        Integer port = cli.containsKey("rest") ? Integer.valueOf(cli.get("rest")) : 8080;
        String token = cli.get("token");

        RestServer rest = new RestServer(port, token, contact, wallet, mk, metrics, leadStore, scene);
        rest.start();

        System.out.println("[REST] http://localhost:"+port + "   (Schreibendpunkte erwarten X-Auth)");
        System.out.println("Telefon-CTA: " + contact.telHref());
        Thread.currentThread().join();
    }

    /* ============================== CONTACT ============================== */
    static final class ContactInfo {
        final String name, phone, email, address, website;
        ContactInfo(String n,String p,String e,String a,String w){ name=n; phone=p; email=e; address=a; website=w; }
        static ContactInfo defaults(){
            return new ContactInfo("ShariaBoots SecureSwarm","+387 67 105 2799","contact@secureswarm.local",
                    "Jezerski bb, 77241 Bosanska Krupa, BA","https://secureswarm.local");
        }
        ContactInfo withName(String v){ return v==null?this:new ContactInfo(v,phone,email,address,website); }
        ContactInfo withPhone(String v){ return v==null?this:new ContactInfo(name,v,email,address,website); }
        ContactInfo withEmail(String v){ return v==null?this:new ContactInfo(name,phone,v,address,website); }
        ContactInfo withAddress(String v){ return v==null?this:new ContactInfo(name,phone,email,v,website); }
        ContactInfo withWebsite(String v){ return v==null?this:new ContactInfo(name,phone,email,address,v); }
        String telHref(){ return "tel:" + phone.replace(" ",""); }
        String toJson(){
            return "{\n" +
                "  \"name\": \""+esc(name)+"\",\n" +
                "  \"phone\": \""+esc(phone)+"\",\n" +
                "  \"email\": \""+esc(email)+"\",\n" +
                "  \"address\": \""+esc(address)+"\",\n" +
                "  \"website\": \""+esc(website)+"\"\n" +
                "}\n";
        }
        static String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }
        static ContactInfo fromMap(ContactInfo old, Map<String,String> m){
            return new ContactInfo(
                m.getOrDefault("name", old.name),
                m.getOrDefault("phone", old.phone),
                m.getOrDefault("email", old.email),
                m.getOrDefault("address", old.address),
                m.getOrDefault("website", old.website)
            );
        }
    }

    /* ============================== WALLET ============================== */
    static final class UnifiedWallet {
        final String iban, btc, eth, receiverName;
        UnifiedWallet(String iban,String btc,String eth,String name){ this.iban=iban; this.btc=btc; this.eth=eth; this.receiverName=name; }
        static UnifiedWallet defaults(){
            return new UnifiedWallet("AT921400057010099023",
                    "bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd",
                    "0x40dEa729f32481A707917AcBBD9eaA84AcB66367",
                    "ShariaBoots SecureSwarm");
        }
        UnifiedWallet withIBAN(String v){ return v==null?this:new UnifiedWallet(v, btc, eth, receiverName); }
        UnifiedWallet withBTC(String v){  return v==null?this:new UnifiedWallet(iban, v, eth, receiverName); }
        UnifiedWallet withETH(String v){  return v==null?this:new UnifiedWallet(iban, btc, v, receiverName); }
        String summary(){
            return "{ \"iban\":\""+iban+"\", \"btc\":\""+btc+"\", \"eth\":\""+eth+"\" }";
        }
        String renderQrSection(Double btcAmount, Double sepaAmountEUR, String sepaNote){
            StringBuilder sb=new StringBuilder("== Wallet/QR ==\n");
            String btcUri = QrUtil.bitcoinUri(btc, btcAmount);
            if (!QrUtil.writePngIfZXing(btcUri, new File("build/qr_btc.png"), 640))
                sb.append("[BTC Payload]\n").append(btcUri).append("\n\n");
            String ethUri = QrUtil.ethereumUri(eth);
            if (!QrUtil.writePngIfZXing(ethUri, new File("build/qr_eth.png"), 640))
                sb.append("[ETH Payload]\n").append(ethUri).append("\n\n");
            String epc = QrUtil.epcSepaPayload(receiverName, iban, sepaAmountEUR, sepaNote==null?"Donation":sepaNote);
            if (!QrUtil.writePngIfZXing(epc, new File("build/qr_sepa.png"), 640))
                sb.append("[SEPA/EPC Payload]\n").append(epc).append("\n");
            sb.append("\n[PayPal] https://www.paypal.me/SanelCrnkic?amount=")
              .append(NumberFormat.getInstance(Locale.US).format(sepaAmountEUR))
              .append("&currency_code=EUR\n");
            return sb.toString();
        }
    }
    static final class QrUtil {
        static String bitcoinUri(String address, Double amountBtc){
            if (amountBtc==null) return "bitcoin:"+address;
            NumberFormat nf=NumberFormat.getInstance(Locale.US); nf.setGroupingUsed(false); nf.setMaximumFractionDigits(8);
            return "bitcoin:"+address+"?amount="+nf.format(amountBtc);
        }
        static String ethereumUri(String address){ return "ethereum:"+address; }
        static String epcSepaPayload(String name,String iban,Double amountEUR,String rem){
            String version="001", encoding="1", service="SCT", bic="";
            String amount = (amountEUR==null) ? "" : "EUR"+String.format(Locale.US,"%.2f",Math.max(0.01,amountEUR));
            String r=(rem==null)?"":rem;
            return String.join("\n", Arrays.asList("BCD",version,encoding,service,bic,name,iban.replace(" ",""),amount,"",r,"","") );
        }
        @SuppressWarnings("unchecked")
        static boolean writePngIfZXing(String payload, File out, int size){
            try {
                Class<?> bfClass   = Class.forName("com.google.zxing.BarcodeFormat");
                Object qrFormat    = Enum.valueOf((Class<Enum>) bfClass.asSubclass(Enum.class), "QR_CODE");
                Class<?> writerCls = Class.forName("com.google.zxing.qrcode.QRCodeWriter");
                Object writer      = writerCls.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method encode = writerCls.getMethod("encode", String.class, bfClass, int.class, int.class, Map.class);
                Object bitMatrix    = encode.invoke(writer, payload, qrFormat, size, size, null);
                Class<?> bmClass    = Class.forName("com.google.zxing.common.BitMatrix");
                int w = (int) bmClass.getMethod("getWidth").invoke(bitMatrix);
                int h = (int) bmClass.getMethod("getHeight").invoke(bitMatrix);
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.lang.reflect.Method get = bmClass.getMethod("get", int.class, int.class);
                for(int y=0;y<h;y++) for(int x=0;x<w;x++){ boolean on=(boolean)get.invoke(bitMatrix,x,y); img.setRGB(x,y,on?0x000000:0xFFFFFF); }
                javax.imageio.ImageIO.write(img,"png",out);
                System.out.println("[QR] PNG geschrieben → "+out.getAbsolutePath());
                return true;
            } catch (ClassNotFoundException e) {
                System.out.println("[QR] ZXing nicht gefunden – PNG-Export übersprungen.");
                return false;
            } catch (Throwable t) { System.out.println("[QR] Fehler: "+t.getMessage()); return false; }
        }
    }

    /* ============================== MANUAL & MARKETING ============================== */
    enum MaterialKategorie { TEXTIL, ENERGIE, SENSORIK, PNEUMATIK, THERMIK, GEHÄUSE, ELEKTRONIK, SONSTIGES }
    static final class Material { final String name; final MaterialKategorie kat; final String beschreibung; final String zertifikate;
        Material(String n,MaterialKategorie k,String d,String z){name=n;kat=k;beschreibung=d;zertifikate=z;} }
    static final class BOMItem {
        final String modul; final Material mat; final double menge; final String einheit; final boolean mustHave; final Double kosten;
        BOMItem(String modul,Material mat,double menge,String einheit,boolean mustHave,Double kosten){
            this.modul=modul; this.mat=mat; this.menge=menge; this.einheit=einheit; this.mustHave=mustHave; this.kosten=kosten;
        }
    }
    static final class BuildManual {
        final List<BOMItem> items = new ArrayList<>(); final List<String> schritte=new ArrayList<>(); final List<String> qa=new ArrayList<>(); final List<String> policy=new ArrayList<>();
        static BuildManual defaultDefensiv(){
            BuildManual b=new BuildManual();
            b.items.add(new BOMItem("Innenlage", new Material("Merino/Modacryl-Innenstoff", MaterialKategorie.TEXTIL, "hautfreundlich, flammhemmend", "REACH"), 4, "m²", true, 38.0));
            b.items.add(new BOMItem("Außenhaut", new Material("Hydrophobe Composite-Lage",  MaterialKategorie.GEHÄUSE,"abriebfest, wasserabweisend","REACH"),3,"m²",true,56.0));
            b.items.add(new BOMItem("Airbag-Matrix", new Material("CO₂-Kartuschen (zertifiziert)", MaterialKategorie.PNEUMATIK,"UN-Kartusche","UN/CE"),2,"Stk",true,24.0));
            b.items.add(new BOMItem("Ventile/Schlauch", new Material("Schnellventile + HD-Minischlauch", MaterialKategorie.PNEUMATIK,"CO₂-geeignet","CE"),6,"Stk",true,18.0));
            b.items.add(new BOMItem("Puffer", new Material("Superkondensatoren (Industrie)", MaterialKategorie.ENERGIE,"Kurzzeitleistung","CE"),6,"Stk",true,42.0));
            b.items.add(new BOMItem("Reserve", new Material("LiFePO₄-Minipack", MaterialKategorie.ENERGIE,"sichere Zellchemie","CE/UN38.3"),1,"Stk",true,35.0));
            b.items.add(new BOMItem("Solar", new Material("Flex-Solarpatch (CIGS/PERC)", MaterialKategorie.ENERGIE,"biegsam, matt","CE"),2,"Stk",false,22.0));
            b.items.add(new BOMItem("Tribo", new Material("Tribo-Faserband (PVDF)", MaterialKategorie.ENERGIE,"Reibung → Erhalteladung","REACH"),6,"m",false,9.0));
            b.items.add(new BOMItem("Sensorik", new Material("IMU + Baro + Drucksensoren", MaterialKategorie.SENSORIK,"Auslösung/Diagnose offline","CE"),1,"Satz",true,29.0));
            b.items.add(new BOMItem("Steuerung", new Material("Mikrocontroller + Treiber", MaterialKategorie.ELEKTRONIK,"deterministische Logik","CE"),1,"Satz",true,33.0));
            b.items.add(new BOMItem("Thermik", new Material("Heat-Pipes + Mikrokanäle", MaterialKategorie.THERMIK,"Hitzeableitung","CE"),1,"Satz",true,27.0));
            b.items.add(new BOMItem("Sonst.", new Material("Nähgarn, Dichtkleber, Schrumpf", MaterialKategorie.SONSTIGES,"Endmontage","—"),1,"Satz",true,12.0));
            b.schritte.addAll(Arrays.asList(
                "1. Zuschnitt/Textil – Nahtzugaben 8–12 mm.",
                "2. Airbag-Matrix – Ventile einsetzen, Dichtflächen entfetten.",
                "3. Thermik-Lage – Heat-Pipes/Mikrokanal einlegen, Drucktest 0.4–0.6 bar.",
                "4. Energiepfad – Supercaps + LiFePO₄ mit Sicherung, Kabel Zugentlastung.",
                "5. Sensorik/Steuerung – IMU/Baro/Druck an MCU, Offlinetest.",
                "6. Solar/Tribo (optional) – Patches/Bänder fixieren, Biegeradien beachten.",
                "7. Endmontage – Nähte verschließen, Trockenlauf ohne Kartusche."
            ));
            b.qa.addAll(Arrays.asList(
                "Dichtigkeits-Haltezeit 10 min, Δp ≤ 5%.",
                "CO₂-Trockenlauf (ohne Kartusche).",
                "Sensorik: Auslösezeit ≤ 100 ms; Selbsttest ok.",
                "Energiepfad: Sicherung + Temperaturanstieg ≤ 15K."
            ));
            b.policy.addAll(Arrays.asList(
                "Rein defensiv/humanitär; keine Offense-Funktion.",
                "Zonen-Policy: No-Fly/Conflict sperren; Datenschutz via Edge-Processing."
            ));
            return b;
        }
        String renderMarkdown(ContactInfo contact, UnifiedWallet wallet){
            StringBuilder sb=new StringBuilder();
            sb.append("# Bauanleitung – ShariaBoots SecureSwarm (defensiv)\n\n");
            sb.append("_Rein defensiv & humanitär. Reale Einsätze nur mit Recht/Behördenkoordination & Mensch-im-Loop._\n\n");
            sb.append("## Kontakt\n- **Name:** ").append(contact.name).append("\n- **Telefon:** ").append(contact.phone)
              .append("\n- **E-Mail:** ").append(contact.email).append("\n- **Adresse:** ").append(contact.address)
              .append("\n- **Website:** ").append(contact.website).append("\n\n");
            sb.append("## Materialliste (BOM)\n");
            sb.append("| Modul | Material | Kat. | Menge | Einheit | Zertifikate | Pflicht | ca. Kosten |\n|---|---|---|---:|:---:|---|:---:|---:|\n");
            double sum=0; for (BOMItem it: items){ double k=it.kosten==null?0:it.kosten; sum+=k;
                sb.append("| ").append(it.modul).append(" | ").append(it.mat.name).append(" | ").append(it.mat.kat)
                  .append(" | ").append(it.menge).append(" | ").append(it.einheit).append(" | ").append(it.mat.zertifikate)
                  .append(" | ").append(it.mustHave?"Ja":"Opt").append(" | ").append(k>0?String.format(Locale.US,"€%.2f",k):"–").append(" |\n"); }
            sb.append("\n**Summe (exkl. Arbeit):** ").append(String.format(Locale.US,"€%.2f",sum)).append("\n\n");
            sb.append("## Schritte\n"); for(String s:schritte) sb.append("- ").append(s).append("\n");
            sb.append("\n## QA/Compliance\n"); for(String s:qa) sb.append("- ").append(s).append("\n");
            sb.append("\n**Policy:**\n"); for(String s:policy) sb.append("- ").append(s).append("\n");
            sb.append("\n## Wallet/Spenden\n- **IBAN:** ").append(wallet.iban).append("\n- **BTC:** ").append(wallet.btc)
              .append("\n- **ETH:** ").append(wallet.eth).append("\n- QR/SEPA/BTC/ETH siehe `Wallet.txt` bzw. PNG.\n");
            return sb.toString();
        }
    }
    static final class MarketingPlan {
        static final class Offer { final String sku,name,desc; final double basePrice,minPrice; Offer(String sku,String name,String desc,double base,double min){this.sku=sku;this.name=name;this.desc=desc;this.basePrice=base;this.minPrice=min;} }
        final List<Offer> offers = new ArrayList<>();
        static MarketingPlan defaultPlan(){
            MarketingPlan m=new MarketingPlan();
            m.offers.add(new Offer("GB-KIT","GreenBoots Field Kit","Öko-Module (Smart Bins, Crop Guardian, Reef Microlab) – read-only/defensiv.", 600,450));
            m.offers.add(new Offer("SB-DEF","ShariaBoots Defensiv","Textil/CO₂-Airbag-Lage, Sensorik/MCU, Energiepuffer – humanitär.", 1500,1100));
            m.offers.add(new Offer("SWARM-BUN","Service-Schwarm Bundle","Mikro-Swarm (sim), CO₂/Plattform, Edge-Privacy – read-only.", 900,700));
            m.offers.add(new Offer("REC-READ","Neverlose (read-only)","Recovery/Fusion (GNSS/Cell/WiFi/BLE/IMU) mit Consent/Vault – REST, defensiv.", 1200,950));
            return m;
        }
        String toJson(){
            StringBuilder sb=new StringBuilder("["); int i=0;
            for (Offer o: offers){
                if(i++>0) sb.append(',');
                sb.append("{\"sku\":\"").append(o.sku).append("\",\"name\":\"").append(esc(o.name))
                  .append("\",\"desc\":\"").append(esc(o.desc)).append("\",\"base\":").append(o.basePrice)
                  .append(",\"min\":").append(o.minPrice).append('}');
            }
            return sb.append(']').toString();
        }
        String renderMarkdown(ContactInfo contact, UnifiedWallet wallet){
            StringBuilder sb = new StringBuilder();
            sb.append("# Marketing-Konzept – SecureSwarm Suite\n\n");
            sb.append("## ICPs (Ideal Customer Profiles)\n");
            sb.append("- **Kommunen/Forst/Berg**: Bärenkontakt vermeiden, Waldschutz, Lawinen-/Unfallhilfe (defensiv).\n");
            sb.append("- **Landwirtschaft/Genossenschaften**: Wildschaden konfliktarm, Bestäuber-Förderung, Wasserhilfe.\n");
            sb.append("- **NGOs/Hilfswerke**: Schutzkoordination, Wasser- & Gesundheitsmodule, faire Preisstaffel.\n");
            sb.append("- **Hafen/Marine/Naturschutz**: Reef-Microlab, Ghost-Nets Hinweise, Blau-Korridor.\n\n");
            sb.append("## Angebote/Bundles\n");
            sb.append("| SKU | Name | Beschreibung | Basis | Minimum |\n");
            sb.append("|---|---|---|---:|---:|\n");
            for (Offer o : offers) {
                sb.append("| ").append(o.sku).append(" | ").append(o.name).append(" | ").append(o.desc)
                  .append(" | €").append(String.format(Locale.US,"%.0f",o.basePrice))
                  .append(" | €").append(String.format(Locale.US,"%.0f",o.minPrice)).append(" |\n");
            }
            sb.append("\n## Fairness-Preise (Beispiel)\n");
            sb.append("- **NEEDY**: 0 € (gratis, humanitär)\n");
            sb.append("- **STANDARD**: Listenpreis\n");
            sb.append("- **WEALTHY**: Listenpreis × 1.2 (Querfinanzierung)\n\n");
            sb.append("## Funnel & Kanal\n");
            sb.append("1) Website/Landing (Angebote + Vorteile + QR/Wallet)  \n");
            sb.append("2) Kontakt/Lead (“").append(contact.telHref()).append("“ & Formular)  \n");
            sb.append("3) Qualifizierung (Use-Case, Compliance, Policy-Zonen)  \n");
            sb.append("4) Angebot (SKU + Mindestpreis + Support)  \n");
            sb.append("5) Abschluss (SEPA/PayPal/Krypto) + Onboarding  \n\n");
            sb.append("## Call-to-Action (CTA)\n");
            sb.append("- **Jetzt anrufen:** ").append(contact.phone).append("  \n");
            sb.append("- **E-Mail:** ").append(contact.email).append("  \n");
            sb.append("- **Wallet:** IBAN ").append(wallet.iban).append(" · BTC ").append(wallet.btc).append(" · ETH ").append(wallet.eth).append("\n");
            sb.append("\n## Recht/Policy (Kurz)\n");
            sb.append("- Rein defensiv; Konflikt-/No-Fly-Zonen respektieren; Betrieb nur mit Einwilligung & Recht.  \n");
            sb.append("- CE/REACH/UN38.3 für Materialien/Transport, Datenschutz (Edge-Privacy), Transparenz-Ledger empfohlen.  \n");
            return sb.toString();
        }
        static String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }
    }

    static final class VizScene {
        static final class AstroObject { final String id,name,type; final double ra,dec,distanceAU; AstroObject(String id,String name,String type,double ra,double dec,double distanceAU){this.id=id;this.name=name;this.type=type;this.ra=ra;this.dec=dec;this.distanceAU=distanceAU;} }
        static final class GeoPOI { final String name,category; final double lat,lng; GeoPOI(String name,String category,double lat,double lng){this.name=name;this.category=category;this.lat=lat;this.lng=lng;} }
        final List<AstroObject> astro = new ArrayList<>();
        final List<GeoPOI> geo = new ArrayList<>();
        static VizScene defaultScene(){
            VizScene s = new VizScene();
            s.astro.add(new AstroObject("sun","Sonne","STAR", 0.0, 0.0, 0));
            s.astro.add(new AstroObject("earth","Erde","PLANET", 0.0, 0.0, 1));
            s.astro.add(new AstroObject("mars","Mars","PLANET", 0.0, 0.0, 1.524));
            s.geo.add(new GeoPOI("Sarajevo","CITY",43.8563,18.4131));
            s.geo.add(new GeoPOI("Wien","CITY",48.2082,16.3738));
            return s;
        }
        String toJson(){
            StringBuilder sb=new StringBuilder();
            sb.append('{');
            sb.append("\"astro\":[");
            for(int i=0;i<astro.size();i++){
                AstroObject o=astro.get(i);
                if(i>0) sb.append(',');
                sb.append('{')
                  .append("\"id\":\"").append(ContactInfo.esc(o.id)).append("\",")
                  .append("\"name\":\"").append(ContactInfo.esc(o.name)).append("\",")
                  .append("\"type\":\"").append(ContactInfo.esc(o.type)).append("\",")
                  .append("\"ra\":").append(String.format(Locale.US,"%.6f",o.ra)).append(',')
                  .append("\"dec\":").append(String.format(Locale.US,"%.6f",o.dec)).append(',')
                  .append("\"distanceAU\":").append(String.format(Locale.US,"%.6f",o.distanceAU))
                  .append('}');
            }
            sb.append(']');
            sb.append(',');
            sb.append("\"geo\":[");
            for(int i=0;i<geo.size();i++){
                GeoPOI g=geo.get(i);
                if(i>0) sb.append(',');
                sb.append('{')
                  .append("\"name\":\"").append(ContactInfo.esc(g.name)).append("\",")
                  .append("\"category\":\"").append(ContactInfo.esc(g.category)).append("\",")
                  .append("\"lat\":").append(String.format(Locale.US,"%.6f",g.lat)).append(',')
                  .append("\"lng\":").append(String.format(Locale.US,"%.6f",g.lng))
                  .append('}');
            }
            sb.append(']');
            sb.append('}');
            return sb.toString();
        }
    }

    /* ============================== METRICS + LEADS ============================== */
    static final class MetricsRegistry {
        private final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, AtomicLong> errors   = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Histogram>   hists   = new ConcurrentHashMap<>();
        private final AtomicLong leads = new AtomicLong(0);

        void incReq(String path, String method){ key(counters, "requests_total{path=\""+path+"\",method=\""+method+"\"}").incrementAndGet(); }
        void incErr(String path, String method){ key(errors, "errors_total{path=\""+path+"\",method=\""+method+"\"}").incrementAndGet(); }
        void observe(String name, long ms){ hists.computeIfAbsent(name, k->Histogram.defaultMs()).observe(ms); }
        void incLead(){ leads.incrementAndGet(); }

        String prometheus(){
            StringBuilder sb=new StringBuilder();
            sb.append("# HELP requests_total Total HTTP requests\n# TYPE requests_total counter\n");
            counters.forEach((k,v)-> sb.append(k).append(' ').append(v.get()).append('\n'));
            sb.append("\n# HELP errors_total Total HTTP 5xx/4xx\n# TYPE errors_total counter\n");
            errors.forEach((k,v)-> sb.append(k).append(' ').append(v.get()).append('\n'));
            sb.append("\n# HELP http_request_duration_ms Histogram of request durations\n# TYPE http_request_duration_ms histogram\n");
            for (Map.Entry<String,Histogram> e: hists.entrySet()) sb.append(e.getValue().asProm("http_request_duration_ms", e.getKey()));
            sb.append("\n# HELP leads_total Total accepted leads\n# TYPE leads_total counter\nleads_total ").append(leads.get()).append('\n');
            return sb.toString();
        }
        private AtomicLong key(ConcurrentMap<String,AtomicLong> m,String k){ return m.computeIfAbsent(k, kk->new AtomicLong(0)); }

        static final class Histogram {
            private final double[] bounds; private final long[] buckets; private long count=0; private double sum=0;
            Histogram(double[] bounds){ this.bounds=bounds; this.buckets=new long[bounds.length+1]; }
            static Histogram defaultMs(){ return new Histogram(new double[]{50,100,250,500,1000,2500,5000}); }
            synchronized void observe(double v){ int i=0; while(i<bounds.length && v>bounds[i]) i++; buckets[i]++; count++; sum+=v; }
            synchronized String asProm(String metric, String label){
                StringBuilder sb=new StringBuilder();
                for(int i=0;i<buckets.length;i++){
                    String le = (i<bounds.length) ? String.valueOf(bounds[i]) : "+Inf";
                    sb.append(metric).append("_bucket{le=\"").append(le).append("\",name=\"").append(label).append("\"} ").append(buckets[i]).append('\n');
                }
                sb.append(metric).append("_count{name=\"").append(label).append("\"} ").append(count).append('\n');
                sb.append(metric).append("_sum{name=\"").append(label).append("\"} ").append(String.format(Locale.US,"%.3f", sum)).append('\n');
                return sb.toString();
            }
        }
    }
    static final class LeadStore {
        private final File file;
        LeadStore(String path){ this.file=new File(path); }
        synchronized String append(Map<String,Object> m){
            String id = "lead-"+System.currentTimeMillis();
            m.put("id", id); m.put("ts", new Date().toString());
            try (Writer w=new OutputStreamWriter(new FileOutputStream(file,true), StandardCharsets.UTF_8)){
                w.write(toJson(m)); w.write("\n");
            } catch(IOException e){ throw new RuntimeException(e); }
            return id;
        }
        private String toJson(Map<String,Object> m){
            StringBuilder sb=new StringBuilder("{"); int i=0;
            for (Map.Entry<String,Object> e: m.entrySet()){
                if(i++>0) sb.append(',');
                sb.append("\"").append(ContactInfo.esc(e.getKey())).append("\":");
                Object v=e.getValue();
                if (v==null) sb.append("null");
                else if (v instanceof Number || v instanceof Boolean) sb.append(String.valueOf(v));
                else sb.append("\"").append(ContactInfo.esc(String.valueOf(v))).append("\"");
            }
            return sb.append('}').toString();
        }
    }

    /* ============================== REST SERVER ============================== */
    static final class RestServer {
        private final HttpServer server;
        private final String token;
        private volatile ContactInfo contact;
        private final UnifiedWallet wallet;
        private final MarketingPlan mk;
        private final VizScene scene;
        private final MetricsRegistry metrics;
        private final LeadStore leads;

        RestServer(int port, String token, ContactInfo c, UnifiedWallet w, MarketingPlan m, MetricsRegistry met, LeadStore ls, VizScene sc) throws IOException {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            this.token = token; this.contact=c; this.wallet=w; this.mk=m; this.scene=sc; this.metrics=met; this.leads=ls;
            server.setExecutor(Executors.newCachedThreadPool());

            add("/healthz", this::health);
            add("/status",  this::status);
            add("/offers",  this::offers);
            add("/checkout",this::checkout);
            add("/contact", this::contactEndpoint);
            add("/lead",    this::lead);
            add("/call",    this::call);
            add("/scene",   this::sceneData);
            add("/metrics", this::metrics);
        }

        void start(){ server.start(); }

        private void add(String path, HttpHandler h){
            server.createContext(path, ex -> {
                long t0 = System.nanoTime();
                String method = ex.getRequestMethod().toUpperCase(Locale.ROOT);
                metrics.incReq(path, method);
                try {
                    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                    if ("OPTIONS".equals(method)) { send(ex, 204, ""); return; }
                    h.handle(ex);
                } catch (Throwable e) {
                    metrics.incErr(path, method);
                    send(ex, 500, "{\"error\":\""+ContactInfo.esc(e.getMessage())+"\"}");
                } finally {
                    long ms = (System.nanoTime()-t0)/1_000_000;
                    metrics.observe(path, ms);
                    ex.close();
                }
            });
        }

        /* -------- Handlers -------- */
        private void health(HttpExchange ex) throws IOException { send(ex, 200, "{\"ok\":true}"); }

        private void status(HttpExchange ex) throws IOException {
            String body = "{"
                +"\"contact\":"+contact.toJson().trim()+"," 
                +"\"wallet\":"+wallet.summary()+"," 
                +"\"offers\":"+mk.toJson()
                +"}";
            send(ex, 200, body);
        }

        private void offers(HttpExchange ex) throws IOException { send(ex, 200, mk.toJson()); }

        private void checkout(HttpExchange ex) throws IOException {
            Map<String,String> q = parseQuery(ex.getRequestURI().getRawQuery());
            String provider = q.getOrDefault("provider","paypal").toLowerCase(Locale.ROOT);
            double amount = parseDouble(q.getOrDefault("amount","25.0"), 25.0);
            String cur = q.getOrDefault("currency","EUR");
            String note= q.getOrDefault("note","Support");
            String resp;
            switch (provider) {
                case "paypal" -> resp = "{\"provider\":\"paypal\",\"link\":\"https://www.paypal.me/SanelCrnkic?amount="+fmt(amount)+"&currency_code="+cur+"\"}";
                case "bank"   -> {
                    String purpose="DON-"+UUID.randomUUID().toString().substring(0,8);
                    resp = "{\"provider\":\"bank\",\"sepa\":\"bank://SEPA?iban="+wallet.iban+"&amount="+fmt(amount)+"&currency="+cur+"&purpose="+purpose+"\"}";
                }
                case "btc"    -> resp = "{\"provider\":\"btc\",\"uri\":\""+ContactInfo.esc(QrUtil.bitcoinUri(wallet.btc, amount/ (double)btcEurRateGuess()))+"\"}";
                case "eth"    -> resp = "{\"provider\":\"eth\",\"uri\":\""+ContactInfo.esc(QrUtil.ethereumUri(wallet.eth))+"\"}";
                default       -> { send(ex, 400, "{\"error\":\"unknown provider\"}"); return; }
            }
            send(ex, 200, resp);
        }

        private void contactEndpoint(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod().toUpperCase(Locale.ROOT);
            if ("GET".equals(method)) {
                send(ex, 200, contact.toJson());
                return;
            }
            if ("PUT".equals(method)) {
                requireAuth(ex);
                String body = readBody(ex);
                Map<String,String> m = parseJsonFlat(body);
                contact = ContactInfo.fromMap(contact, m);
                writeText("build/Kontakt.json", contact.toJson());
                send(ex, 200, contact.toJson());
                return;
            }
            send(ex, 405, "{\"error\":\"method not allowed\"}");
        }

        private void lead(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { send(ex, 405, "{\"error\":\"method not allowed\"}"); return; }
            requireAuth(ex);
            Map<String,String> m = parseJsonFlat(readBody(ex));
            Map<String,Object> full = new LinkedHashMap<>();
            full.put("name", m.getOrDefault("name",""));
            full.put("email", m.getOrDefault("email",""));
            full.put("phone", m.getOrDefault("phone",""));
            full.put("message", m.getOrDefault("message",""));
            String id = leads.append(full);
            metrics.incLead();
            send(ex, 200, "{\"ok\":true,\"id\":\""+id+"\"}");
        }

        private void call(HttpExchange ex) throws IOException {
            String j = "{\"tel\":\""+ContactInfo.esc(contact.telHref())+"\",\"display\":\""+ContactInfo.esc(contact.phone)+"\"}";
            send(ex, 200, j);
        }

        private void sceneData(HttpExchange ex) throws IOException {
            send(ex, 200, scene.toJson());
        }

        private void metrics(HttpExchange ex) throws IOException {
            ex.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
            send(ex, 200, this.metrics.prometheus());
        }

        /* -------- Helpers -------- */
        private void requireAuth(HttpExchange ex) throws IOException {
            if (token == null || token.isBlank()) { send(ex, 401, "{\"error\":\"auth required (set --token & X-Auth)\"}"); throw new RuntimeException("unauthorized"); }
            String hdr = ex.getRequestHeaders().getFirst("X-Auth");
            if (hdr == null || !hdr.equals(token)) { send(ex, 401, "{\"error\":\"unauthorized\"}"); throw new RuntimeException("unauthorized"); }
        }
        private static Map<String,String> parseQuery(String q){
            Map<String,String> m=new LinkedHashMap<>(); if(q==null||q.isBlank()) return m;
            for (String p: q.split("&")){ int i=p.indexOf('='); if(i>0){ m.put(urlDecode(p.substring(0,i)), urlDecode(p.substring(i+1))); } }
            return m;
        }
        private static String urlDecode(String s){ try{ return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8); } catch(Exception e){ return s; } }
        private static String readBody(HttpExchange ex) throws IOException {
            try (InputStream is=ex.getRequestBody()){ return new String(is.readAllBytes(), StandardCharsets.UTF_8); }
        }
        private static void send(HttpExchange ex, int code, String body) throws IOException {
            byte[] b=body.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(code, b.length);
            try(OutputStream os=ex.getResponseBody()){ os.write(b); }
        }
        private static String fmt(double v){ return NumberFormat.getInstance(Locale.US).format(v); }
        private static int btcEurRateGuess(){ return 60000; } // simple Platzhalter für BTC/EUR Umrechnung
        private static double parseDouble(String s, double def){ try { return Double.parseDouble(s); } catch(Exception e){ return def; } }
    }

    /* ============================== UTILS ============================== */
    static Map<String,String> parseArgs(String[] a){
        Map<String,String> m=new LinkedHashMap<>();
        for (String s: a){ if (s.startsWith("--") && s.contains("=")){ int i=s.indexOf('='); m.put(s.substring(2,i).toLowerCase(Locale.ROOT), s.substring(i+1)); } }
        return m;
    }
    static void ensureDir(String path){ File dir=new File(path); if(!dir.exists() && !dir.mkdirs()) System.out.println("[WARN] mkdirs failed: "+path); }
    static void writeText(String path,String body){
        try(Writer w=new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)){ w.write(body); System.out.println("[OK] "+path); }
        catch(IOException e){ System.out.println("[ERR] "+path+": "+e.getMessage()); }
    }
    static Map<String,String> parseJsonFlat(String json){
        // sehr einfache Parser für flache JSON-Objekte: {"k":"v", "n":123}
        Map<String,String> m=new LinkedHashMap<>(); if(json==null) return m;
        String s=json.trim(); if(!s.startsWith("{")||!s.endsWith("}")) return m; s=s.substring(1,s.length()-1).trim(); if(s.isEmpty()) return m;
        int i=0; while(i<s.length()){
            // key
            while(i<s.length() && Character.isWhitespace(s.charAt(i))) i++;
            if(i>=s.length()||s.charAt(i)!='"') break; int j=s.indexOf('"', i+1); if(j<0) break; String key=s.substring(i+1,j);
            i=j+1; while(i<s.length() && (s.charAt(i)==' '||s.charAt(i)==':')) i++;
            // value (string oder number/bool)
            String val;
            if(i<s.length() && s.charAt(i)=='"'){ int k=s.indexOf('"', i+1); if(k<0) break; val=s.substring(i+1,k); i=k+1; }
            else { int k=i; while(k<s.length() && s.charAt(k)!=',' ) k++; val=s.substring(i,k).trim(); i=k; }
            m.put(key, val);
            while(i<s.length() && (s.charAt(i)==' '||s.charAt(i)==',')) i++;
        }
        return m;
    }
}
