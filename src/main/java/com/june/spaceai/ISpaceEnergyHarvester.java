package com.june.spaceai;

import java.util.List;

/**
 * Schnittstelle: sammelt Energiedaten aus dem Weltraum.
 */
public interface ISpaceEnergyHarvester {
    List<ResourceData> collectEnergyData();
}
