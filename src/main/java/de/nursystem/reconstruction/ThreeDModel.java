package de.nursystem.reconstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D-Modell
 */
class ThreeDModel {
    private final String name;
    private final List<ModelLayer> layers = new ArrayList<>();

    public ThreeDModel(String name) {
        this.name = name;
    }

    public void addLayer(String layerName, double area) {
        layers.add(new ModelLayer(layerName, area));
    }

    public int getLayerCount() { return layers.size(); }
}
