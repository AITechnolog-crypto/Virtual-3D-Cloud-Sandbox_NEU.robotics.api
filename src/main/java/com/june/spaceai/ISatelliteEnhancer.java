package com.june.spaceai;

import java.util.List;

/**
 * Schnittstelle: sammelt/verbessert Satellitensignaldaten.
 */
public interface ISatelliteEnhancer {
    List<SatelliteData> collectSatelliteData();
}
