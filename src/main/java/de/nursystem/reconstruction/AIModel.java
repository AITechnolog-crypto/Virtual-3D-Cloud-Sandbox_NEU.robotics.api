package de.nursystem.reconstruction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * KI-Modell für intelligente Wiederaufbau-Planung
 *
 * Bismillahirahmanirahim ❤️
 */
@Component
public class AIModel {

    private static final Logger logger = LoggerFactory.getLogger(AIModel.class);

    public AIModel() {
        logger.info("🧠 AI Model initialisiert");
    }

    /**
     * Analysiert und weist Aufgaben basierend auf Drohnenfähigkeiten zu
     */
    public BuildingTask analyzeAndAssignTask(ReconstructionDrone drone) {
        BuildingSite site = drone.getBuildingSite();
        double progress = site.getReconstructionProgress();

        // KI-basierte Aufgabenauswahl basierend auf Fortschritt
        if (progress < 20) {
            return new BuildingTask("Grundlagen legen", TaskType.FOUNDATION);
        } else if (progress < 50) {
            return new BuildingTask("Wände errichten", TaskType.WALLS);
        } else if (progress < 70) {
            return new BuildingTask("Dach konstruieren", TaskType.ROOF);
        } else if (progress < 90) {
            return new BuildingTask("Innenausbau", TaskType.INTERIOR);
        } else {
            return new BuildingTask("Feinarbeiten", TaskType.FINISHING);
        }
    }

    /**
     * Generiert einen Verschönerungsplan
     */
    public BeautificationPlan generateBeautificationPlan(BuildingSite site) {
        logger.debug("🎨 Generiere Verschönerungsplan für: {}", site.getName());

        // KI-basierte Stil-Auswahl basierend auf Umgebung
        String facadeStyle = determineFacadeStyle(site);
        String colorScheme = selectColorScheme(site);
        String landscaping = planLandscaping(site);

        return new BeautificationPlan(facadeStyle, colorScheme, landscaping);
    }

    /**
     * Bewertet Schäden an einem Gebäude
     */
    public DamageAssessment assessDamage(BuildingSite site) {
        logger.debug("🔍 Bewerte Schäden an: {}", site.getName());

        // Simulierte KI-Analyse
        double structuralDamage = Math.random() * 100;
        String materials = determineRequiredMaterials(structuralDamage);
        int estimatedDays = calculateReconstructionTime(site, structuralDamage);

        return new DamageAssessment(
            site.getName(),
            structuralDamage,
            materials,
            estimatedDays
        );
    }

    /**
     * Optimiert Bauablauf mit maschinellem Lernen
     */
    public ConstructionSequence optimizeConstructionSequence(BuildingSite site) {
        logger.debug("⚙️ Optimiere Bauablauf für: {}", site.getName());

        // KI-optimierte Bauabfolge
        ConstructionSequence sequence = new ConstructionSequence();
        sequence.addStep("Geländevorbereitung", 2);
        sequence.addStep("Fundamentierung", 5);
        sequence.addStep("Rohbau", 10);
        sequence.addStep("Dachkonstruktion", 7);
        sequence.addStep("Innenausbau", 12);
        sequence.addStep("Verschönerung", 4);

        return sequence;
    }

    /**
     * Vorhersage von Ressourcenbedarf
     */
    public ResourcePrediction predictResourceNeeds(BuildingSite site) {
        logger.debug("📊 Vorhersage Ressourcenbedarf für: {}", site.getName());

        double area = site.getArea();

        return new ResourcePrediction(
            (int) (area * 0.5),  // Beton in Tonnen
            (int) (area * 0.3),  // Stahl in Tonnen
            (int) (area * 2.0),  // Ziegel/Steine
            (int) (area * 0.1)   // Holz in m³
        );
    }

    // Hilfsmethoden
    private String determineFacadeStyle(BuildingSite site) {
        String[] styles = {"Modern", "Klassisch", "Minimalistisch", "Mediterran", "Rustikal"};
        return styles[(int) (Math.random() * styles.length)];
    }

    private String selectColorScheme(BuildingSite site) {
        String[] schemes = {
            "Warme Erdtöne",
            "Helle Pastellfarben",
            "Elegantes Grau-Weiß",
            "Natürliche Holztöne",
            "Kühle Blau-Grau Töne"
        };
        return schemes[(int) (Math.random() * schemes.length)];
    }

    private String planLandscaping(BuildingSite site) {
        return "Grünflächen mit einheimischen Pflanzen, Bäume zur Beschattung";
    }

    private String determineRequiredMaterials(double damage) {
        if (damage > 75) {
            return "Beton, Stahl, Ziegel, Holz, Isolierung (Vollsanierung)";
        } else if (damage > 50) {
            return "Beton, Ziegel, Isolierung (Teilsanierung)";
        } else {
            return "Reparaturmaterialien, Farbe (Kosmetisch)";
        }
    }

    private int calculateReconstructionTime(BuildingSite site, double damage) {
        double baseTime = site.getArea() / 10.0; // 10 m² pro Tag
        double damageFactor = 1.0 + (damage / 100.0);
        return (int) Math.ceil(baseTime * damageFactor);
    }
}
