package com.june.spaceai;

import java.util.ArrayList;
import java.util.List;

/**
 * Beispielhafte Implementierung des Weltraum-Energieerntesystems.
 */
public class ExampleEnergyHarvester implements ISpaceEnergyHarvester {
    @Override
    public List<ResourceData> collectEnergyData() {
        List<ResourceData> data = new ArrayList<>();
        data.add(new ResourceData(75));
        data.add(new ResourceData(80));
        return data;
    }
}
