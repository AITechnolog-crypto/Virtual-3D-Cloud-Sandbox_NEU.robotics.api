import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

/**
 * SecureSwarmSuiteApp
 * ---------------------------------------------------------
 * - Bauanleitung (Markdown) + Materialliste (mit Zertifikaten)
 * - Gemeinsame Wallet (IBAN/BTC/ETH), SEPA-EPC, PayPal, QR-Payloads
 * - Kontakt/Telefon (editierbar via CLI), tel:-Link
 * - Marketing-Konzept (Markdown) mit ICPs, Bundles & Fairness-Preisen
 *
 * DISCLAIMER:
 *   Rein defensiv & humanitär. Keine Offense-/Waffenlogik. Reale Einsätze
 *   nur mit Recht/Behördenkoordination & Mensch-im-Loop.
 *
 * Bauen:
 *   javac SecureSwarmSuiteApp.java
 *   java SecureSwarmSuiteApp
 *
 * Beispiel mit eigenen Kontaktdaten/Wallet:
 *   java SecureSwarmSuiteApp --name="ShariaBoots SecureSwarm" \
 *       --phone="+387671052799" --email="hello@example.com" \
 *       --addr="Jezerski bb, 77241 Bosanska Krupa, BA" \
 *       --site="https://example.com" \
 *       --iban="AT921400057010099023" \
 *       --btc="bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd" \
 *       --eth="0x40dEa729f32481A707917AcBBD9eaA84AcB66367"
 */
public class SecureSwarmSuiteApp {

    /* ============================== MAIN ============================== */
    public static void main(String[] args) {
        Map<String, String> cli = parseArgs(args);

        // Kontakt – Default aus deinen Angaben, via CLI überschreibbar
        ContactInfo contact = ContactInfo.defaults()
                .withName(cli.get("name"))
                .withPhone(cli.get("phone"))
                .withEmail(cli.get("email"))
                .withAddress(cli.get("addr"))
                .withWebsite(cli.get("site"));

        // Gemeinsame Wallet – Default aus deinem Code, via CLI überschreibbar
        UnifiedWallet wallet = UnifiedWallet.defaults()
                .withIBAN(cli.get("iban"))
                .withBTC(cli.get("btc"))
                .withETH(cli.get("eth"));

        // 1) Bauanleitung + Materialliste erzeugen
        BuildManual manual = BuildManual.defaultDefensiv();
        String manualMd = manual.renderMarkdown(contact, wallet);

        // 2) Marketing-Konzept erzeugen
        MarketingPlan mk = MarketingPlan.defaultPlan();
        String mkMd = mk.renderMarkdown(contact, wallet);

        // 3) Wallet/QR-Section (Konsole + Files)
        String qrOut = wallet.renderQrSection(0.001 /*BTC demo*/, 25.00, "SecureSwarm Support");

        // 4) Dateien schreiben
        ensureDir("build");
        writeText("build/Bauanleitung_DE.md", manualMd);
        writeText("build/Marketing_Plan_DE.md", mkMd);
        writeText("build/Kontakt.json", contact.toJson());
        writeText("build/Wallet.txt", qrOut);

        // 5) Konsole – kurze Übersicht
        System.out.println("== SecureSwarm Suite ==");
        System.out.println(contact);
        System.out.println(wallet.summary());
        System.out.println("\nDateien erzeugt unter ./build/");
        System.out.println(" - Bauanleitung_DE.md");
        System.out.println(" - Marketing_Plan_DE.md");
        System.out.println(" - Kontakt.json");
        System.out.println(" - Wallet.txt");
        System.out.println("\nTelefon-CTA: " + contact.telHref());
        System.out.println("(QR-PNGs werden erzeugt, wenn ZXing im Classpath ist.)");
    }

    /* ============================== CONTACT ============================== */
    static final class ContactInfo {
        final String name, phone, email, address, website;

        ContactInfo(String n, String p, String e, String a, String w) {
            this.name = n; this.phone = p; this.email = e; this.address = a; this.website = w;
        }
        static ContactInfo defaults() {
            return new ContactInfo(
                "ShariaBoots SecureSwarm",
                "+387 67 105 2799",
                "contact@secureswarm.local",
                "Jezerski bb, 77241 Bosanska Krupa, BA",
                "https://secureswarm.local"
            );
        }
        ContactInfo withName(String v){ return v==null?this:new ContactInfo(v, phone, email, address, website); }
        ContactInfo withPhone(String v){ return v==null?this:new ContactInfo(name, v, email, address, website); }
        ContactInfo withEmail(String v){ return v==null?this:new ContactInfo(name, phone, v, address, website); }
        ContactInfo withAddress(String v){ return v==null?this:new ContactInfo(name, phone, email, v, website); }
        ContactInfo withWebsite(String v){ return v==null?this:new ContactInfo(name, phone, email, address, v); }

        String telHref(){ return "tel:" + phone.replace(" ",""); }
        public String toString(){
            return "Kontakt: " + name + "\n  Tel: " + phone + "\n  E-Mail: " + email +
                   "\n  Adresse: " + address + "\n  Web: " + website + "\n";
        }
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
    }

    /* ============================== WALLET ============================== */
    static final class UnifiedWallet {
        final String iban, btc, eth, receiverName;
        UnifiedWallet(String iban, String btc, String eth, String name){
            this.iban = iban; this.btc = btc; this.eth = eth; this.receiverName = name;
        }
        static UnifiedWallet defaults(){
            return new UnifiedWallet(
                "AT921400057010099023",
                "bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd",
                "0x40dEa729f32481A707917AcBBD9eaA84AcB66367",
                "ShariaBoots SecureSwarm"
            );
        }
        UnifiedWallet withIBAN(String v){ return v==null?this:new UnifiedWallet(v, btc, eth, receiverName); }
        UnifiedWallet withBTC(String v){  return v==null?this:new UnifiedWallet(iban, v, eth, receiverName); }
        UnifiedWallet withETH(String v){  return v==null?this:new UnifiedWallet(iban, btc, v, receiverName); }

        String summary(){
            return "[Wallet]\n  IBAN: "+iban+"\n  BTC:  "+btc+"\n  ETH:  "+eth+"\n";
        }

        String renderQrSection(Double btcAmount, Double sepaAmountEUR, String sepaNote){
            StringBuilder sb = new StringBuilder();
            sb.append("== Wallet/QR ==\n");
            // BTC (BIP-21)
            String btcUri = QrUtil.bitcoinUri(btc, btcAmount);
            if (!QrUtil.writePngIfZXing(btcUri, new File("build/qr_btc.png"), 640))
                sb.append("[BTC Payload]\n").append(btcUri).append("\n\n");

            // ETH (URI)
            String ethUri = QrUtil.ethereumUri(eth);
            if (!QrUtil.writePngIfZXing(ethUri, new File("build/qr_eth.png"), 640))
                sb.append("[ETH Payload]\n").append(ethUri).append("\n\n");

            // SEPA/EPC (BCD)
            String epc = QrUtil.epcSepaPayload(receiverName, iban, sepaAmountEUR, sepaNote==null?"Donation":sepaNote);
            if (!QrUtil.writePngIfZXing(epc, new File("build/qr_sepa.png"), 640))
                sb.append("[SEPA/EPC Payload]\n").append(epc).append("\n");

            // PayPal Deep Link (falls genutzt)
            sb.append("\n[PayPal] https://www.paypal.me/SanelCrnkic?amount=")
              .append(NumberFormat.getInstance(Locale.US).format(sepaAmountEUR))
              .append("&currency_code=EUR\n");
            return sb.toString();
        }
    }

    static final class QrUtil {
        static String bitcoinUri(String address, Double amountBtc){
            if (amountBtc==null) return "bitcoin:"+address;
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            nf.setGroupingUsed(false);
            nf.setMaximumFractionDigits(8);
            return "bitcoin:"+address+"?amount="+nf.format(amountBtc);
        }
        static String ethereumUri(String address){ return "ethereum:"+address; }
        static String epcSepaPayload(String name, String iban, Double amountEUR, String rem){
            String version="001", encoding="1", service="SCT", bic="";
            String amount = (amountEUR==null) ? "" : "EUR"+String.format(Locale.US,"%.2f", Math.max(0.01, amountEUR));
            String r = (rem==null) ? "" : rem;
            return String.join("\n", Arrays.asList("BCD",version,encoding,service,bic,name,iban.replace(" ",""),amount,"",r,"","") );
        }
        @SuppressWarnings("unchecked")
        static boolean writePngIfZXing(String payload, File out, int size){
            try {
                Class<?> bfClass = Class.forName("com.google.zxing.BarcodeFormat");
                Object qrFormat = Enum.valueOf((Class<Enum>) bfClass.asSubclass(Enum.class), "QR_CODE");
                Class<?> writerCls = Class.forName("com.google.zxing.qrcode.QRCodeWriter");
                Object writer = writerCls.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method encode = writerCls.getMethod("encode", String.class, bfClass, int.class, int.class, Map.class);
                Object bitMatrix = encode.invoke(writer, payload, qrFormat, size, size, null);
                Class<?> bmClass = Class.forName("com.google.zxing.common.BitMatrix");
                int w = (int) bmClass.getMethod("getWidth").invoke(bitMatrix);
                int h = (int) bmClass.getMethod("getHeight").invoke(bitMatrix);
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.lang.reflect.Method get = bmClass.getMethod("get", int.class, int.class);
                for (int y=0; y<h; y++) for (int x=0; x<w; x++) {
                    boolean on = (boolean) get.invoke(bitMatrix, x, y);
                    img.setRGB(x, y, on ? 0x000000 : 0xFFFFFF);
                }
                javax.imageio.ImageIO.write(img, "png", out);
                System.out.println("[QR] PNG geschrieben → "+out.getAbsolutePath());
                return true;
            } catch (ClassNotFoundException e) {
                System.out.println("[QR] ZXing nicht gefunden – PNG-Export übersprungen. Payload in Wallet.txt.");
                return false;
            } catch (Throwable t) {
                System.out.println("[QR] Fehler beim PNG-Schreiben: "+t.getMessage());
                return false;
            }
        }
    }

    /* ============================== MANUAL ============================== */
    enum MaterialKategorie { TEXTIL, ENERGIE, SENSORIK, PNEUMATIK, THERMIK, GEHÄUSE, ELEKTRONIK, SONSTIGES }
    static final class Material {
        final String name; final MaterialKategorie kat; final String beschreibung; final String zertifikate;
        Material(String n, MaterialKategorie k, String d, String z){ name=n; kat=k; beschreibung=d; zertifikate=z; }
    }
    static final class BOMItem {
        final String modul; final Material mat; final double menge; final String einheit; final boolean mustHave; final Double kosten; // optional
        BOMItem(String modul, Material mat, double menge, String einheit, boolean mustHave, Double kosten){
            this.modul=modul; this.mat=mat; this.menge=menge; this.einheit=einheit; this.mustHave=mustHave; this.kosten=kosten;
        }
    }
    static final class BuildManual {
        final List<BOMItem> items = new ArrayList<>();
        final List<String> schritte = new ArrayList<>();
        final List<String> qa = new ArrayList<>();
        final List<String> policy = new ArrayList<>();

        static BuildManual defaultDefensiv(){
            BuildManual b = new BuildManual();
            // Materialien (aus deinem Stack, defensiv – mit Zertifikaten)
            b.items.add(new BOMItem("Innenlage", new Material("Merino/Modacryl-Innenstoff", MaterialKategorie.TEXTIL, "hautfreundlich, flammhemmend", "REACH"), 4, "m²", true, 38.0));
            b.items.add(new BOMItem("Außenhaut", new Material("Hydrophobe Composite-Lage", MaterialKategorie.GEHÄUSE, "abriebfest, wasserabweisend", "REACH"), 3, "m²", true, 56.0));
            b.items.add(new BOMItem("Airbag-Matrix", new Material("CO₂-Kartuschen (zertifiziert)", MaterialKategorie.PNEUMATIK, "UN zugelassene Einwegkartusche", "UN/CE"), 2, "Stk", true, 24.0));
            b.items.add(new BOMItem("Ventile/Schlauch", new Material("Schnellventile + HD-Minischlauch", MaterialKategorie.PNEUMATIK, "CO₂-geeignet, kältefest", "CE"), 6, "Stk", true, 18.0));
            b.items.add(new BOMItem("Puffer", new Material("Superkondensatoren (Industrie)", MaterialKategorie.ENERGIE, "Kurzzeitleistung, sicher", "CE"), 6, "Stk", true, 42.0));
            b.items.add(new BOMItem("Reserve", new Material("LiFePO₄-Minipack", MaterialKategorie.ENERGIE, "sichere Zellchemie", "CE/UN38.3"), 1, "Stk", true, 35.0));
            b.items.add(new BOMItem("Solar", new Material("Flex-Solarpatch (CIGS/PERC)", MaterialKategorie.ENERGIE, "biegsam, matt", "CE"), 2, "Stk", false, 22.0));
            b.items.add(new BOMItem("Tribo", new Material("Tribo-Faserband (PVDF)", MaterialKategorie.ENERGIE, "Reibung → Erhalteladung", "REACH"), 6, "m", false, 9.0));
            b.items.add(new BOMItem("Sensorik", new Material("IMU + Baro + Drucksensoren", MaterialKategorie.SENSORIK, "Auslösung/Diagnose offline", "CE"), 1, "Satz", true, 29.0));
            b.items.add(new BOMItem("Steuerung", new Material("Mikrocontroller + Treiber", MaterialKategorie.ELEKTRONIK, "deterministische Logik", "CE"), 1, "Satz", true, 33.0));
            b.items.add(new BOMItem("Thermik", new Material("Heat-Pipes (sealed) + Mikrokanäle", MaterialKategorie.THERMIK, "Hitzeableitung", "CE"), 1, "Satz", true, 27.0));
            b.items.add(new BOMItem("Sonst.", new Material("Nähgarn, Dichtkleber, Schrumpf", MaterialKategorie.SONSTIGES, "Endmontage", "—"), 1, "Satz", true, 12.0));

            // Bau-Schritte (kompakt, prüffähig)
            b.schritte.add("1. Zuschnitt/Textil: Innenlage & Außenhaut maßhaltig zuschneiden (Nahtzugaben 8–12 mm).");
            b.schritte.add("2. Airbag-Matrix: Hex-Zellen trocken montieren, Ventile einsetzen, Dichtflächen entfetten.");
            b.schritte.add("3. Thermik-Lage: Heat-Pipes/Mikrokanal einlegen, Dicht-/Druckprüfung (0.4–0.6 bar) durchführen.");
            b.schritte.add("4. Energiepfad: Supercaps + LiFePO₄ verschrauben (Sicherung), Leitungen fixieren (Zugentlastung).");
            b.schritte.add("5. Sensorik/Steuerung: IMU/Baro/Druck → MCU, deterministische Logik, Offlinetest.");
            b.schritte.add("6. Solar/Tribo (optional): Patches/Bänder fixieren, Kabelwege sichern, Biegeradien beachten.");
            b.schritte.add("7. Endmontage: Nähte verschließen, Dichtkleber gezielt, Steckerprüfung, Trockenlauf ohne Kartusche.");

            // QA/Compliance
            b.qa.add("Dichtigkeit: 10 min Haltezeit, Druckabfall ≤ 5%.");
            b.qa.add("CO₂-Trockenlauf (ohne Kartusche) – Magnetventil/Testdruck sim.");
            b.qa.add("Sensorik-Funktion: Auslösezeit ≤ 100 ms; Selbsttest ok.");
            b.qa.add("Energiepfad: Sicherung greift, keine Erwärmung > 15K im Lasttest.");
            b.policy.add("Defensive/Humanitär only; kein Offense-Einsatz.");
            b.policy.add("CE/REACH/UN38.3 beachten; Gefahrenzonen/No-Fly/Conflict via Policy-Gates sperren.");

            return b;
        }

        String renderMarkdown(ContactInfo contact, UnifiedWallet wallet){
            StringBuilder sb = new StringBuilder();
            sb.append("# Bauanleitung – ShariaBoots SecureSwarm (defensiv)\n\n");
            sb.append("_Rein defensiv & humanitär. Reale Einsätze nur mit Recht/Behördenkoordination & Mensch-im-Loop._\n\n");
            sb.append("## Kontakt\n");
            sb.append("- **Name:** ").append(contact.name).append("\n");
            sb.append("- **Telefon:** ").append(contact.phone).append("  \n");
            sb.append("- **E-Mail:** ").append(contact.email).append("\n");
            sb.append("- **Adresse:** ").append(contact.address).append("\n");
            sb.append("- **Website:** ").append(contact.website).append("\n\n");

            sb.append("## Materialliste (BOM)\n");
            sb.append("| Modul | Material | Kat. | Menge | Einheit | Zertifikate | Pflicht | ca. Kosten |\n");
            sb.append("|---|---|---|---:|:---:|---|:---:|---:|\n");
            double sum = 0.0;
            for (BOMItem it : items) {
                double k = it.kosten==null?0.0:it.kosten;
                sum += k;
                sb.append("| ").append(it.modul).append(" | ").append(it.mat.name).append(" | ").append(it.mat.kat)
                  .append(" | ").append(it.menge).append(" | ").append(it.einheit)
                  .append(" | ").append(it.mat.zertifikate)
                  .append(" | ").append(it.mustHave?"Ja":"Optional")
                  .append(" | ").append(k>0?String.format(Locale.US,"€%.2f",k):"–")
                  .append(" |\n");
            }
            sb.append("\n**Summe (Richtwert, exkl. Arbeit):** ").append(String.format(Locale.US,"€%.2f",sum)).append("\n\n");

            sb.append("## Schritte\n");
            for (String s : schritte) sb.append("- ").append(s).append("\n");
            sb.append("\n## QA/Compliance\n");
            for (String s : qa) sb.append("- ").append(s).append("\n");
            sb.append("\n**Policy:**\n");
            for (String s : policy) sb.append("- ").append(s).append("\n");

            sb.append("\n## Wallet/Spenden (gemeinsam)\n");
            sb.append("- **IBAN:** ").append(wallet.iban).append("\n");
            sb.append("- **BTC:** ").append(wallet.btc).append("\n");
            sb.append("- **ETH:** ").append(wallet.eth).append("\n");
            sb.append("- **SEPA-EPC QR** & **BTC/ETH Payloads** liegen in `Wallet.txt` bzw. als PNG (falls ZXing da ist).\n");

            return sb.toString();
        }
    }

    /* ============================== MARKETING ============================== */
    static final class MarketingPlan {
        static final class Offer { final String sku, name, desc; final double basePrice, minPrice;
            Offer(String sku,String name,String desc,double base,double min){this.sku=sku;this.name=name;this.desc=desc;this.basePrice=base;this.minPrice=min;} }

        final List<Offer> offers = new ArrayList<>();

        static MarketingPlan defaultPlan(){
            MarketingPlan m = new MarketingPlan();
            m.offers.add(new Offer("GB-KIT", "GreenBoots Field Kit",
                    "Öko-Module (Smart Bins, Crop Guardian, Reef Microlab) – read-only/defensiv.", 600, 450));
            m.offers.add(new Offer("SB-DEF", "ShariaBoots Defensiv",
                    "Textil/CO₂-Airbag-Lage, Sensorik/MCU, Energiepuffer – humanitär.", 1500, 1100));
            m.offers.add(new Offer("SWARM-BUN", "Service-Schwarm Bundle",
                    "Mikro-Swarm (sim), CO₂/Plattform, Edge-Privacy – read-only.", 900, 700));
            m.offers.add(new Offer("REC-READ", "Neverlose (read-only)",
                    "Recovery/Fusion (GNSS/Cell/WiFi/BLE/IMU) mit Consent/Vault – REST, defensiv.", 1200, 950));
            return m;
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
    }

    /* ============================== UTILS ============================== */
    static Map<String,String> parseArgs(String[] a){
        Map<String,String> m = new LinkedHashMap<>();
        for (String s: a){
            int i = s.indexOf('=');
            if (s.startsWith("--") && i>2) {
                m.put(s.substring(2, i).toLowerCase(Locale.ROOT), s.substring(i+1));
            }
        }
        return m;
    }
    static void ensureDir(String path){
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) System.out.println("[WARN] Konnte Verzeichnis nicht erzeugen: " + path);
    }
    static void writeText(String path, String body){
        try (Writer w = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
            w.write(body);
            System.out.println("[OK] geschrieben: " + path);
        } catch (IOException e) {
            System.out.println("[ERR] "+path+": "+e.getMessage());
        }
    }
}

//
// javac SecureSwarmSuiteApp.java
// java SecureSwarmSuiteApp
// # oder mit deinen Kontaktdaten / Wallet
// java SecureSwarmSuiteApp --name="ShariaBoots SecureSwarm" \
//   --phone="+387671052799" --email="kontakt@deinprojekt.ba" \
//   --addr="Jezerski bb, 77241 Bosanska Krupa, BA" \
//   --site="https://deinprojekt.ba" \
//   --iban="AT921400057010099023" \
//   --btc="bc1qzdg8qjd42rg2sa6t4wqzchx40rjmz0xyh3gccd" \
//   --eth="0x40dEa729f32481A707917AcBBD9eaA84AcB66367"