package de.nursystem.reconstruction;

/**
 * Verschönerungsplan
 */
class BeautificationPlan {
    private final String facadeStyle;
    private final String colorScheme;
    private final String landscaping;

    public BeautificationPlan(String facadeStyle, String colorScheme, String landscaping) {
        this.facadeStyle = facadeStyle;
        this.colorScheme = colorScheme;
        this.landscaping = landscaping;
    }

    public String getFacadeStyle() { return facadeStyle; }
    public String getColorScheme() { return colorScheme; }
    public String getLandscaping() { return landscaping; }
}
