package com.june.spaceai;

import java.util.ArrayList;
import java.util.List;

/**
 * Beispielhafte Implementierung der Satellitendaten-Erfassung/-Verbesserung.
 */
public class ExampleSatelliteEnhancer implements ISatelliteEnhancer {
    @Override
    public List<SatelliteData> collectSatelliteData() {
        List<SatelliteData> data = new ArrayList<>();
        data.add(new SatelliteData(90));
        data.add(new SatelliteData(95));
        return data;
    }
}
