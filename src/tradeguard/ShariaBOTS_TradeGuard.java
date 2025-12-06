package tradeguard;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

// ==================================================
// ShariaBOTS_TradeGuard – Volume 4
// Neu: Soziale Fairness
//  - Arme und Bedürftige: Gratis Schutz und Wasserhilfe
//  - Reiche: Zahlungspflicht (dynamisch, verhandelbar)
//  - Humanitarian Router (Aid), Social Equity Engine, Aid Ledger
//  - Weiterhin: Handel, Lizenzierung, Anti-Diebstahl, Bodyguard
//  - Rückspeisung nur aus verkaufter Restenergie, IBAN fix (Simulation)
// Dauerlaufend, hochgedreht
// ==================================================

public class ShariaBOTS_TradeGuard {

    // Kern
    private final CloudUeberwachungseinheit ueberwachung;
    private final CloudSicherheitsmanagement sicherheit;
    private final CloudDatenbank datenbank;
    private final EnergyManager energyManager;
    private final PredictiveImpactAnalyser analyser;

    // Umwelt und Heilung
    private final VegetationsUndBodenModul vegetation;
    private final WasserManagementModul wasser;
    private final BiodiversitaetsModul bio;
    private final GesundheitsModul gesundheit;
    private final SelbstheilungsModul selfHeal;
    private final CoolingUnit cooling;

    // Schutz
    private final PlasmaShield shield;
    private final StrahlungsNeutralisator strahlung;
    private final ThreatDetector threats;
    private final MagnetfeldKoppler koppler;
    private final MagnetfeldVerstaerker magnet;
    private final SatellitenResonanzVerbindung orbit;
    private final SwarmController swarm;
    private final Bodyguard bodyguard;

    // Handel
    private final TradeMarketplace marketplace;
    private final PricingEngine pricing;
    private final NegotiationEngine negotiation;
    private final LicenseManager license;
    private final AntiTheftGuard antiTheft;
    private final KYCVerifier kyc;

    // Soziale Fairness
    private final SocialEquityEngine equity;
    private final HumanitarianRouter aidRouter;
    private final AidLedger aidLedger;

    // Zahlung, Silos, Wallet
    private final WalletRueckspeisung wallet;
    private final SiloManager silos;
    private final PaymentProcessor payment;

    // Konstanten
    private static final String FIXE_IBAN = "AT921400057010099023";

    public ShariaBOTS_TradeGuard() {
        // Kern
        this.ueberwachung = new CloudUeberwachungseinheit();
        this.sicherheit = new CloudSicherheitsmanagement();
        this.datenbank = new CloudDatenbank();
        this.energyManager = new EnergyManager();
        this.analyser = new PredictiveImpactAnalyser();

        // Umwelt
        this.vegetation = new VegetationsUndBodenModul();
        this.wasser = new WasserManagementModul();
        this.bio = new BiodiversitaetsModul();
        this.gesundheit = new GesundheitsModul();
        this.selfHeal = new SelbstheilungsModul();
        this.cooling = new CoolingUnit();

        // Schutz
        this.shield = new PlasmaShield(energyManager);
        this.strahlung = new StrahlungsNeutralisator();
        this.threats = new ThreatDetector();
        this.koppler = new MagnetfeldKoppler();
        this.magnet = new MagnetfeldVerstaerker(energyManager, koppler);
        this.orbit = new SatellitenResonanzVerbindung();
        this.swarm = new SwarmController(140_000);
        this.bodyguard = new Bodyguard(shield, magnet, koppler);

        // Handel
        this.marketplace = new TradeMarketplace();
        this.pricing = new PricingEngine();
        this.negotiation = new NegotiationEngine();
        this.license = new LicenseManager();
        this.antiTheft = new AntiTheftGuard(license);
        this.kyc = new KYCVerifier();

        // Soziale Fairness
        this.equity = new SocialEquityEngine();
        this.aidLedger = new AidLedger();
        this.aidRouter = new HumanitarianRouter(equity, wasser, bodyguard, paymentPlaceholder());

        // Zahlung, Wallet, Silos
        this.wallet = new WalletRueckspeisung(FIXE_IBAN);
        this.silos = new SiloManager();
        this.payment = new PaymentProcessor(wallet, orbit, silos);

        // Jetzt den Router auf den echten Payment-Prozessor umschalten
        this.aidRouter.setPaymentProcessor(payment);
    }

    private PaymentProcessor paymentPlaceholder() {
        // Platzhalter, wird im Konstruktor sofort ersetzt
        return new PaymentProcessor(new WalletRueckspeisung("DUMMY"), new SatellitenResonanzVerbindung(), new SiloManager());
    }

    public void init() {
        System.out.println("Init ShariaBOTS_TradeGuard");
        ueberwachung.startUeberwachung();
        sicherheit.startSicherheitsmassnahmen();
        datenbank.verbinde();

        vegetation.initialisiere();
        wasser.initialisiere();
        bio.initialisiere();
        gesundheit.initialisiere();

        orbit.aktiviere();
        orbit.sendeSynchronimpuls();

        swarm.deploySwarm();
        magnet.aktiviere();
        magnet.erzeugeFeld();
        koppler.koppelnAn();

        // Angebote
        marketplace.addOffer(new Offer("CO2-Abscheidungs-Cluster v6", 900, 700, 25));
        marketplace.addOffer(new Offer("NurFlight-Schutzmodul v4", 1500, 1100, 15));
        marketplace.addOffer(new Offer("Tarngrillen-Schwarm v1 Bundle", 600, 450, 40));
        marketplace.addOffer(new Offer("Magnetfeld-Auskoppel-Unit", 1200, 950, 12));
        marketplace.addOffer(new Offer("Strahlungsfilter Gamma-Neutron", 1800, 1400, 10));
    }

    public void start() {
        Random rnd = new Random();
        while (true) {
            double co2 = ueberwachung.getCO2Level();
            datenbank.speichereDaten("CO2", co2);

            // Öko und Schutz
            if (co2 > 1000) {
                sicherheit.aktiviereAlarm();
                swarm.absorbiereCO2();
                vegetation.absorbCO2(co2 * 0.12);
                wasser.regeneriereFluss();
                bio.reagiereAufCO2Krise();
                gesundheit.reagiereAufKrise();
                shield.activate("CO2-Krise");
                cooling.coolDown();
            }
            if (analyser.trendAnalyse(co2)) {
                vegetation.pflanzeBaeume(420);
                wasser.sammleRegenwasser();
                bio.foerdereTierwelt();
                gesundheit.foerdereWohlbefinden();
                swarm.energieVerteilung();
            }
            if (analyser.magnetInterferenzRisiko()) {
                koppler.entkoppelnFeld();
                cooling.coolDown();
            } else if (koppler.istEntkoppelt()) {
                koppler.koppelnAn();
            }

            // Bedrohungen
            List<String> bs = threats.scanEnvironment();
            for (String b : bs) {
                swarm.bedrohungserkennung();
                swarm.abwehrFormation(b);
                double e = swarm.absorbiereBedrohung(b);
                if ("Atombombe".equals(b)) e += strahlung.filterGammaNeutronen();

                if (koppler.istGekoppelt()) {
                    magnet.verstaerkeMitEnergie(e);
                } else {
                    double out = koppler.auskoppelnEnergie(e);
                    orbit.rueckspeisenOrbit(out);
                    silos.speichereInCloud(out);
                }

                double eigenverbrauch = Math.min(1700, e * 0.60);
                double rest = Math.max(0, e - eigenverbrauch);
                energyManager.add(eigenverbrauch * 0.25);
                shield.boostKurz(eigenverbrauch * 0.25);
                cooling.boostKurz(eigenverbrauch * 0.10);
                gesundheit.boostKurz(eigenverbrauch * 0.10);
                vegetation.boostKurz(eigenverbrauch * 0.10);
                wasser.boostKurz(eigenverbrauch * 0.10);
                bio.boostKurz(eigenverbrauch * 0.10);

                if (rest > 0) {
                    payment.routeSoldEnergy(rest, 0);
                }
            }

            double totalE = swarm.getTotalEnergy() + energyManager.getTotal();
            boolean gravOk = magnet.neutralisiereGravitation(totalE);
            if (gravOk && koppler.istGekoppelt()) {
                swarm.ensurePlatform();
            }

            // Handelszyklus
            List<Offer> live = marketplace.listActiveOffers();
            if (!live.isEmpty()) {
                BuyerProfile req = BuyerProfile.random(rnd);
                if (kyc.verify(req)) {
                    Offer offer = live.get(rnd.nextInt(live.size()));
                    double demandIdx = marketplace.demandIndex(offer.getId());
                    double dynamicPrice = pricing.priceFor(offer, demandIdx);
                    // Soziale Preisanpassung für Käufer
                    EquityTier tier = equity.classifyBuyer(req);
                    double socialPrice = equity.applyBuyerPricing(tier, dynamicPrice);
                    double bid = negotiation.generateBid(req, socialPrice);
                    NegotiationResult result = negotiation.negotiate(offer, bid);
                    if (result.accepted) {
                        if (antiTheft.isBlacklisted(req.buyerId)) {
                            bodyguard.blockTradeAndHarden();
                        } else {
                            double operationalCost = pricing.estimateOpsCost(offer);
                            boolean ok = marketplace.commitSale(offer.getId());
                            if (ok) {
                                String licenseKey = license.issueLicense(req.buyerId, offer.getName(), result.agreedPrice);
                                payment.routeSoldEnergy(result.agreedPrice, operationalCost);
                                silos.speichereInWallet(result.agreedPrice);
                                datenbank.speichereDaten("Sale_" + offer.getId(), result.agreedPrice);
                                System.out.println("Verkauf abgeschlossen an " + req.buyerId + " Lizenz " + licenseKey + " Preis " + result.agreedPrice + " Tier " + tier);
                            }
                        }
                    } else {
                        System.out.println("Verhandlung abgelehnt fuer " + offer.getName() + " Gebot " + bid);
                    }
                } else {
                    bodyguard.blockTradeSoft();
                }
            }

            // Humanitäre Hilfe: Requests für Wasser und Schutz
            for (int i = 0; i < 2; i++) {
                CitizenProfile cp = CitizenProfile.random(rnd);
                AidRequest ar = AidRequest.randomFor(cp, rnd);
                AidOutcome outcome = aidRouter.handle(ar, aidLedger);
                if (outcome.deliveredWaterLiters > 0 || outcome.protectionGranted) {
                    System.out.println("Aid geliefert an " + cp.id + " Tier " + equity.classifyCitizen(cp)
                            + " Wasser " + outcome.deliveredWaterLiters + "L"
                            + " Schutz " + outcome.protectionGranted
                            + " berechneter Preis " + outcome.chargedEnergy);
                }
            }

            // Lizenznutzung prüfen
            if (rnd.nextDouble() > 0.83) {
                String any = license.anyLicense();
                if (any != null) {
                    boolean ok = license.validateUse(any, BuyerProfile.random(rnd).buyerId);
                    if (!ok) {
                        antiTheft.flagMisuse(any);
                        bodyguard.respondToMisuse();
                    }
                }
            }

            // Energetik und Status
            energyManager.add(260);
            System.out.println("Energiesaldo " + energyManager.getTotal());
            vegetation.statusBericht();
            wasser.statusBericht();
            bio.statusBericht();
            gesundheit.statusBericht();
            wallet.status();
            silos.status();
            aidLedger.status();

            if (Math.random() > 0.84) {
                selfHeal.repariereSystem();
            }

            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        }
    }

    public static void main(String[] args) {
        ShariaBOTS_TradeGuard app = new ShariaBOTS_TradeGuard();
        app.init();
        app.start();
    }
}

// ======================= Infrastruktur =======================

class CloudUeberwachungseinheit {
    private final Random r = new Random();
    public void startUeberwachung() { System.out.println("Cloud-Überwachung aktiv"); }
    public double getCO2Level() { return 800 + 650 * r.nextDouble(); }
}

class CloudSicherheitsmanagement {
    public void startSicherheitsmassnahmen() { System.out.println("Cloud-Sicherheitsmassnahmen aktiv"); }
    public void aktiviereAlarm() { System.out.println("ALARM ausgelöst"); }
}

class CloudDatenbank {
    public void verbinde() { System.out.println("Cloud-DB verbunden"); }
    public void speichereDaten(String key, double val) { System.out.println("DB " + key + "=" + val); }
}

class EnergyManager {
    private double total = 0.0;
    public void add(double e) { total += Math.max(0, e); }
    public void consume(double e) { total -= Math.max(0, e); if (total < 0) total = 0; }
    public double getTotal() { return total; }
}

class PredictiveImpactAnalyser {
    private double last = -1;
    private final Random r = new Random();
    public boolean trendAnalyse(double current) {
        boolean up = last > 0 && current > last * 1.06;
        last = current;
        if (up) System.out.println("CO2-Trend steigend erkannt");
        return up;
    }
    public boolean magnetInterferenzRisiko() {
        return r.nextDouble() > 0.7;
    }
}

// ====================== Schutz-Module =======================

class PlasmaShield {
    private final EnergyManager em;
    public PlasmaShield(EnergyManager em) { this.em = em; }
    public void activate(String cause) { em.consume(100); System.out.println("Plasma-Schild aktiv gegen " + cause); }
    public void boostKurz(double e) { em.consume(e * 0.1); System.out.println("Plasma-Schild kurz verstärkt"); }
}

class CoolingUnit {
    private double t = 24.0;
    public void coolDown() { t = Math.max(14.0, t - 4.0); System.out.println("Kühlung aktiv T=" + t); }
    public void boostKurz(double e) { t = Math.max(14.0, t - Math.min(3.0, e * 0.005)); System.out.println("Kurz-Kühlboost T=" + t); }
}

class SelbstheilungsModul {
    public void repariereSystem() { System.out.println("Selbstheilung ausgeführt"); }
}

class MagnetfeldKoppler {
    private enum Zustand { GEKOPPELT, ENTKOPPELT }
    private Zustand z = Zustand.GEKOPPELT;
    public void koppelnAn() { z = Zustand.GEKOPPELT; System.out.println("Magnetfeld gekoppelt"); }
    public void entkoppelnFeld() { z = Zustand.ENTKOPPELT; System.out.println("Magnetfeld entkoppelt"); }
    public boolean istGekoppelt() { return z == Zustand.GEKOPPELT; }
    public boolean istEntkoppelt() { return z == Zustand.ENTKOPPELT; }
    public double auskoppelnEnergie(double e) { double x = Math.max(0, e * 0.85); System.out.println("Energie ausgekoppelt " + x); return x; }
}

class MagnetfeldVerstaerker {
    private boolean aktiv;
    private double feld = 0.0;
    private final EnergyManager em;
    private final MagnetfeldKoppler k;

    public MagnetfeldVerstaerker(EnergyManager em, MagnetfeldKoppler k) { this.em = em; this.k = k; }

    public void aktiviere() { aktiv = true; System.out.println("MagnetfeldVerstaerker aktiv"); }
    public void erzeugeFeld() { if (!aktiv) return; feld = 110.0; System.out.println("Magnetfeld aufgebaut Staerke=" + feld); }
    public void verstaerkeMitEnergie(double e) {
        if (!aktiv || !k.istGekoppelt()) return;
        feld += e * 0.22;
        em.consume(Math.min(e * 0.05, em.getTotal()));
        System.out.println("Magnetfeld verstaerkt Staerke=" + feld);
    }
    public boolean neutralisiereGravitation(double e) {
        boolean ok = aktiv && e > 520;
        System.out.println(ok ? "Gravitation neutralisiert" : "Gravitation nicht neutralisiert");
        return ok;
    }
}

class SatellitenResonanzVerbindung {
    private boolean aktiv;
    public void aktiviere() { aktiv = true; System.out.println("Satelliten-Resonanz aktiv"); }
    public void sendeSynchronimpuls() { if (aktiv) System.out.println("Synchronimpuls gesendet"); }
    public void rueckspeisenOrbit(double e) { if (aktiv) System.out.println("Orbit-Rückspeisung +" + e); }
}

class ThreatDetector {
    public List<String> scanEnvironment() {
        List<String> t = new ArrayList<>();
        t.add("Rakete");
        t.add("Laserwaffe");
        if (Math.random() > 0.7) t.add("Atombombe");
        return t;
    }
}

class StrahlungsNeutralisator {
    public double filterGammaNeutronen() {
        System.out.println("Strahlung erkannt Filter aktiv Energie umgewandelt");
        return 650;
    }
}

// Bodyguard
class Bodyguard {
    private final PlasmaShield shield;
    private final MagnetfeldVerstaerker magnet;
    private final MagnetfeldKoppler koppler;
    public Bodyguard(PlasmaShield s, MagnetfeldVerstaerker m, MagnetfeldKoppler k) { this.shield = s; this.magnet = m; this.koppler = k; }
    public void blockTradeAndHarden() {
        System.out.println("Bodyguard blockt Handel und härtet System");
        shield.activate("Handel-Blockade");
        koppler.koppelnAn();
        magnet.verstaerkeMitEnergie(120);
    }
    public void blockTradeSoft() {
        System.out.println("Bodyguard Softblock ohne Eskalation");
    }
    public void respondToMisuse() {
        System.out.println("Bodyguard reagiert auf Missbrauch");
        shield.boostKurz(80);
        if (!koppler.istGekoppelt()) koppler.koppelnAn();
    }
    public void protectCitizen(String citizenId) {
        System.out.println("Bodyguard Schutz aktiviert fuer " + citizenId);
        shield.boostKurz(60);
        if (!koppler.istGekoppelt()) koppler.koppelnAn();
        magnet.verstaerkeMitEnergie(90);
    }
}

// ====================== Umwelt-Stack =======================

class VegetationsUndBodenModul {
    private int baeume = 0;
    private double co2Gebunden = 0.0;
    public void initialisiere() { System.out.println("Vegetation init"); }
    public void absorbCO2(double m) { co2Gebunden += m; System.out.println("Vegetation bindet " + m); }
    public void pflanzeBaeume(int n) { baeume += n; System.out.println(n + " Baeume gepflanzt"); }
    public void statusBericht() { System.out.println("Vegetation Baeume=" + baeume + " CO2=" + co2Gebunden); }
    public void boostKurz(double e) { co2Gebunden += e * 0.05; }
}

class WasserManagementModul {
    private double regen = 0;
    private double entsalzt = 0;
    private int fluesse = 0;
    public void initialisiere() { System.out.println("Wasser init"); }
    public void sammleRegenwasser() { regen += 220; System.out.println("Regen +" + regen); }
    public void entsalzeMeerwasser() { entsalzt += 320; System.out.println("Entsalzt +" + entsalzt); }
    public void regeneriereFluss() { fluesse++; System.out.println("Fluss regeneriert gesamt " + fluesse); }
    public void statusBericht() { System.out.println("Wasser Regen=" + regen + " Entsalzt=" + entsalzt + " Fluesse=" + fluesse); }
    public void boostKurz(double e) { regen += e * 0.02; }
    public double allocateWaterLiters(double requested) {
        double provided = Math.min(requested, 500);
        System.out.println("Wasserhilfe geliefert " + provided + "L");
        return provided;
    }
}

class BiodiversitaetsModul {
    private int refaunierungen = 0;
    public void initialisiere() { System.out.println("Biodiversitaet init"); }
    public void reagiereAufCO2Krise() { refaunierungen++; System.out.println("Biodiv-Maßnahmen aktiviert total " + refaunierungen); }
    public void foerdereTierwelt() { refaunierungen++; System.out.println("Biodiv-Förderung +1 total " + refaunierungen); }
    public void boostKurz(double e) { /* placeholder effect */ }
    public void statusBericht() { System.out.println("Biodiv Aktionen=" + refaunierungen); }
}

class GesundheitsModul {
    private int hilfen = 0;
    public void initialisiere() { System.out.println("Gesundheit init"); }
    public void reagiereAufKrise() { hilfen++; System.out.println("Gesundheitsmaßnahmen aktiviert total " + hilfen); }
    public void foerdereWohlbefinden() { hilfen++; System.out.println("Wohlbefinden-Förderung +1 total " + hilfen); }
    public void boostKurz(double e) { /* placeholder effect */ }
    public void statusBericht() { System.out.println("Gesundheitseinsätze=" + hilfen); }
}

// ====================== Schwarm-Stack =======================

class SwarmController {
    private final List<SwarmRobot> swarm = new ArrayList<>();
    private double totalEnergy = 0.0;
    public SwarmController(int bots) { for (int i = 0; i < bots; i++) swarm.add(new SwarmRobot()); }
    public void deploySwarm() { System.out.println("Deployment " + swarm.size() + " Tarngrillen"); }
    public void energieVerteilung() { totalEnergy += 140; }
    public void bedrohungserkennung() { System.out.println("Schwarm scannt Gefahren"); }
    public void abwehrFormation(String b) { System.out.println("Schwarm formt Schild gegen " + b); }
    public void absorbiereCO2() {
        for (int i = 0; i < 6 && i < swarm.size(); i++) swarm.get(i).operateCO2();
        totalEnergy += 90;
        System.out.println("Schwarm CO2-Absorption durchgeführt");
    }
    public double absorbiereBedrohung(String b) {
        double absorbed;
        switch (b) {
            case "Rakete": absorbed = 520; break;
            case "Atombombe": absorbed = 1600; break;
            case "Laserwaffe": absorbed = 340; break;
            default: absorbed = 180;
        }
        totalEnergy += absorbed;
        System.out.println(b + " absorbiert Energie +" + absorbed);
        return absorbed;
    }
    public double getTotalEnergy() { return totalEnergy; }
    public void ensurePlatform() { System.out.println("Schwarm formt tragende Plattform Stabilisierung aktiv"); }
}

class SwarmRobot {
    private final SolarPanel solar = new SolarPanel();
    private final PhotocatalyticConverter converter = new PhotocatalyticConverter();
    private final SatelliteCommunicator comm = new SatelliteCommunicator();
    private final MagneticFieldGenerator field = new MagneticFieldGenerator();
    public void operateCO2() {
        solar.absorbSunlight();
        converter.convertCO2();
        comm.receiveCommands();
        field.generateField();
        System.out.println("Tarngrille im CO2-Betrieb");
    }
}

class SolarPanel { public void absorbSunlight() { System.out.println("Solarpanel lädt"); } }
class PhotocatalyticConverter { public void convertCO2() { System.out.println("CO2 umgewandelt"); } }
class SatelliteCommunicator { public void receiveCommands() { System.out.println("Sat-Befehl empfangen"); } }
class MagneticFieldGenerator { public void generateField() { System.out.println("Tarnfeld generiert"); } }

// ====================== Handels-Stack =======================

class BuyerProfile {
    public final String buyerId;
    public final int tier;            // 1,2,3 (3 tendenziell wohlhabend)
    public final double reputation;   // 0..1
    public final double incomeIndex;  // 0..1 (wohlhabend ~>0.7)
    private BuyerProfile(String id, int tier, double rep, double incomeIdx) {
        this.buyerId = id; this.tier = tier; this.reputation = rep; this.incomeIndex = incomeIdx;
    }
    public static BuyerProfile random(Random r) {
        String id = "buyer-" + Math.abs(r.nextInt());
        int t = 1 + r.nextInt(3);
        double rep = 0.4 + r.nextDouble() * 0.6;
        double income = 0.2 + r.nextDouble() * 0.8;
        return new BuyerProfile(id, t, rep, income);
    }
}

class KYCVerifier {
    public boolean verify(BuyerProfile b) {
        boolean ok = b.reputation >= 0.5;
        System.out.println(ok ? "KYC ok fuer " + b.buyerId : "KYC fail fuer " + b.buyerId);
        return ok;
    }
}

class Offer {
    private static final AtomicLong SEQ = new AtomicLong(1);
    private final long id = SEQ.getAndIncrement();
    private final String name;
    private final double basePrice;
    private final double minPrice;
    private int stock;
    public Offer(String name, double basePrice, double minPrice, int stock) {
        this.name = name; this.basePrice = basePrice; this.minPrice = minPrice; this.stock = stock;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public double getMinPrice() { return minPrice; }
    public int getStock() { return stock; }
    public boolean decrementStock() { if (stock > 0) { stock--; return true; } return false; }
}

class TradeMarketplace {
    private final Map<Long, Offer> offers = new HashMap<>();
    private final Map<Long, Integer> demand = new HashMap<>();
    public void addOffer(Offer o) { offers.put(o.getId(), o); }
    public List<Offer> listActiveOffers() {
        List<Offer> res = new ArrayList<>();
        for (Offer o : offers.values()) if (o.getStock() > 0) res.add(o);
        return res;
    }
    public double demandIndex(long id) {
        int d = demand.getOrDefault(id, 0);
        demand.put(id, d + 1);
        return 1.0 + Math.min(1.5, d * 0.05);
    }
    public boolean commitSale(long id) {
        Offer o = offers.get(id);
        if (o == null) return false;
        return o.decrementStock();
    }
}

class PricingEngine {
    public double priceFor(Offer o, double demandIdx) {
        double p = o.getBasePrice() * demandIdx;
        return Math.max(o.getMinPrice(), p);
    }
    public double estimateOpsCost(Offer o) {
        return Math.max(50, o.getBasePrice() * 0.15);
    }
}

class NegotiationEngine {
    private final Random r = new Random();
    public double generateBid(BuyerProfile b, double targetPrice) {
        double factor = 0.85 + (1.05 - 0.85) * r.nextDouble();
        double wealthAdj = 0.95 + b.incomeIndex * 0.15;
        return targetPrice * factor * wealthAdj;
    }
    public NegotiationResult negotiate(Offer o, double bid) {
        double floor = o.getMinPrice();
        if (bid >= floor) {
            double agreed = Math.max(floor, Math.min(bid, o.getBasePrice() * 1.8));
            return new NegotiationResult(true, agreed);
        }
        return new NegotiationResult(false, 0);
    }
}

class NegotiationResult {
    public final boolean accepted;
    public final double agreedPrice;
    public NegotiationResult(boolean a, double p) { this.accepted = a; this.agreedPrice = p; }
}

class LicenseManager {
    private final Map<String, String> licenseToBuyer = new HashMap<>();
    private final Map<String, Integer> usage = new HashMap<>();
    public String issueLicense(String buyerId, String product, double price) {
        String key = "LIC-" + Math.abs(Objects.hash(buyerId, product, price, System.nanoTime()));
        licenseToBuyer.put(key, buyerId);
        usage.put(key, 0);
        return key;
    }
    public boolean validateUse(String key, String claimBuyer) {
        String owner = licenseToBuyer.get(key);
        if (owner == null) return false;
        boolean ok = owner.equals(claimBuyer);
        usage.put(key, usage.getOrDefault(key, 0) + 1);
        System.out.println(ok ? "Lizenznutzung ok" : "Lizenznutzung abgewiesen");
        return ok;
    }
    public String anyLicense() {
        for (String k : licenseToBuyer.keySet()) return k;
        return null;
    }
    public String ownerOf(String key) { return licenseToBuyer.get(key); }
}

class AntiTheftGuard {
    private final Set<String> blacklist = new HashSet<>();
    private final LicenseManager lm;
    public AntiTheftGuard(LicenseManager lm) { this.lm = lm; }
    public void flagMisuse(String licenseKey) {
        String owner = lm.ownerOf(licenseKey);
        if (owner != null) {
            blacklist.add(owner);
            System.out.println("AntiTheft blacklistet " + owner);
        }
    }
    public boolean isBlacklisted(String buyerId) { return blacklist.contains(buyerId); }
}

class PaymentProcessor {
    private final WalletRueckspeisung wallet;
    private final SatellitenResonanzVerbindung orbit;
    private final SiloManager silos;
    public PaymentProcessor(WalletRueckspeisung w, SatellitenResonanzVerbindung o, SiloManager s) {
        this.wallet = w; this.orbit = o; this.silos = s;
    }
    public void routeSoldEnergy(double soldEnergy, double operationalCost) {
        double rest = Math.max(0, soldEnergy - operationalCost);
        if (rest <= 0) {
            System.out.println("Kein Rest nach Kosten fuer Rueckspeisung");
            return;
        }
        double toWallet = rest * 0.6;
        double toOrbit = rest * 0.4;
        wallet.gutschreiben(toWallet);
        orbit.rueckspeisenOrbit(toOrbit);
        silos.speichereInWallet(toWallet);
        silos.speichereInCloud(toOrbit);
        System.out.println("Verkaufte Restenergie verteilt Wallet " + toWallet + " Orbit " + toOrbit);
    }
}

class WalletRueckspeisung {
    private final String walletAdresse;
    private double saldo = 0.0;
    public WalletRueckspeisung(String adresse) { this.walletAdresse = adresse; }
    public void gutschreiben(double energie) {
        saldo += energie;
        System.out.println("Rueckspeisung an Wallet " + walletAdresse + " +" + energie);
    }
    public void status() { System.out.println("Wallet-Saldo " + saldo); }
}

class SiloManager {
    private double cloudSilo = 0.0;
    private double walletSilo = 0.0;
    public void speichereInCloud(double e) { cloudSilo += e; System.out.println("Cloud-Silo +" + e); }
    public void speichereInWallet(double e) { walletSilo += e; System.out.println("Wallet-Silo +" + e); }
    public void status() { System.out.println("Silo Status Cloud=" + cloudSilo + " Wallet=" + walletSilo); }
}

// ====================== Soziale Fairness =======================

enum EquityTier { NEEDY, STANDARD, WEALTHY }

class SocialEquityEngine {
    public EquityTier classifyBuyer(BuyerProfile b) {
        if (b.incomeIndex >= 0.75 || b.tier == 3) return EquityTier.WEALTHY;
        if (b.incomeIndex <= 0.35) return EquityTier.NEEDY;
        return EquityTier.STANDARD;
    }
    public EquityTier classifyCitizen(CitizenProfile c) {
        if (c.needScore >= 0.7 || c.incomeIndex <= 0.35) return EquityTier.NEEDY;
        if (c.incomeIndex >= 0.75 && c.needScore < 0.5) return EquityTier.WEALTHY;
        return EquityTier.STANDARD;
    }
    public double applyBuyerPricing(EquityTier tier, double price) {
        switch (tier) {
            case NEEDY: return Math.max(0, price * 0.0);
            case WEALTHY: return price * 1.2;
            default: return price;
        }
    }
    public double serviceWaterPrice(EquityTier tier, double liters) {
        double base = liters * 0.5;
        if (tier == EquityTier.NEEDY) return 0;
        if (tier == EquityTier.WEALTHY) return base * 1.5;
        return base;
    }
    public double serviceProtectionPrice(EquityTier tier) {
        double base = 200;
        if (tier == EquityTier.NEEDY) return 0;
        if (tier == EquityTier.WEALTHY) return base * 2.0;
        return base;
    }
}

class CitizenProfile {
    public final String id;
    public final double incomeIndex;  // 0..1
    public final double needScore;    // 0..1
    public final int householdSize;
    public CitizenProfile(String id, double incomeIndex, double needScore, int householdSize) {
        this.id = id; this.incomeIndex = incomeIndex; this.needScore = needScore; this.householdSize = householdSize;
    }
    public static CitizenProfile random(Random r) {
        String id = "citizen-" + Math.abs(r.nextInt());
        double income = 0.1 + r.nextDouble() * 0.9;
        double need = 0.1 + r.nextDouble() * 0.9;
        int hh = 1 + r.nextInt(6);
        return new CitizenProfile(id, income, need, hh);
    }
}

enum AidType { WATER, PROTECTION }

class AidRequest {
    public final CitizenProfile citizen;
    public final boolean requestWater;
    public final boolean requestProtection;
    public final double requestedLiters;
    private AidRequest(CitizenProfile citizen, boolean water, boolean protection, double liters) {
        this.citizen = citizen; this.requestWater = water; this.requestProtection = protection; this.requestedLiters = liters;
    }
    public static AidRequest randomFor(CitizenProfile citizen, Random r) {
        boolean water = r.nextBoolean();
        boolean prot = r.nextBoolean() || citizen.needScore > 0.6;
        double liters = water ? (50 + r.nextInt(200)) : 0;
        return new AidRequest(citizen, water, prot, liters);
    }
}

class AidOutcome {
    public final double deliveredWaterLiters;
    public final boolean protectionGranted;
    public final double chargedEnergy;
    public AidOutcome(double liters, boolean prot, double charge) {
        this.deliveredWaterLiters = liters; this.protectionGranted = prot; this.chargedEnergy = charge;
    }
}

class AidLedger {
    private double totalWaterLiters = 0;
    private int totalProtections = 0;
    private double totalCharged = 0;
    public void record(AidOutcome out) {
        totalWaterLiters += out.deliveredWaterLiters;
        totalProtections += out.protectionGranted ? 1 : 0;
        totalCharged += out.chargedEnergy;
    }
    public void status() {
        System.out.println("Aid Ledger Wasser=" + totalWaterLiters + "L Schutz=" + totalProtections + " Abrechnung=" + totalCharged);
    }
}

class HumanitarianRouter {
    private final SocialEquityEngine equity;
    private final WasserManagementModul wasser;
    private final Bodyguard bodyguard;
    private PaymentProcessor payment; // nach Konstruktion setzen

    public HumanitarianRouter(SocialEquityEngine eq, WasserManagementModul w, Bodyguard b, PaymentProcessor p) {
        this.equity = eq; this.wasser = w; this.bodyguard = b; this.payment = p;
    }
    public void setPaymentProcessor(PaymentProcessor p) { this.payment = p; }

    public AidOutcome handle(AidRequest req, AidLedger ledger) {
        EquityTier tier = equity.classifyCitizen(req.citizen);

        double litersDelivered = 0;
        boolean protection = false;
        double charge = 0;

        if (req.requestWater) {
            litersDelivered = wasser.allocateWaterLiters(req.requestedLiters);
            charge += equity.serviceWaterPrice(tier, litersDelivered);
        }
        if (req.requestProtection) {
            bodyguard.protectCitizen(req.citizen.id);
            protection = true;
            charge += equity.serviceProtectionPrice(tier);
        }

        // Nur Reiche oder Standard zahlen, Bedürftige gratis
        if (charge > 0 && tier != EquityTier.NEEDY) {
            payment.routeSoldEnergy(charge, Math.max(0, charge * 0.15));
        } else {
            System.out.println("Aid gratis fuer " + req.citizen.id + " Tier " + tier);
        }

        AidOutcome out = new AidOutcome(litersDelivered, protection, charge);
        ledger.record(out);
        return out;
    }
}
