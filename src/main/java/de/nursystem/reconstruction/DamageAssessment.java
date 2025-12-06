package de.nursystem.reconstruction;

/**
 * Schadensbewertung
 */
class DamageAssessment {
    private final String siteName;
    private final double structuralDamage;
    private final String requiredMaterials;
    private final int estimatedDays;

    public DamageAssessment(String siteName, double structuralDamage,
                           String requiredMaterials, int estimatedDays) {
        this.siteName = siteName;
        this.structuralDamage = structuralDamage;
        this.requiredMaterials = requiredMaterials;
        this.estimatedDays = estimatedDays;
    }

    public String getSiteName() { return siteName; }
    public double getStructuralDamage() { return structuralDamage; }
    public String getRequiredMaterials() { return requiredMaterials; }
    public int getEstimatedDays() { return estimatedDays; }
}
