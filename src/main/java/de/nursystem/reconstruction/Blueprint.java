package de.nursystem.reconstruction;

/**
 * Bauplan
 */
class Blueprint {
    private final String name;
    private final double area;
    private final int floors;
    private final String style;

    public Blueprint(String name, double area, int floors, String style) {
        this.name = name;
        this.area = area;
        this.floors = floors;
        this.style = style;
    }

    public String getName() { return name; }
    public double getArea() { return area; }
    public int getFloors() { return floors; }
    public String getStyle() { return style; }
}
