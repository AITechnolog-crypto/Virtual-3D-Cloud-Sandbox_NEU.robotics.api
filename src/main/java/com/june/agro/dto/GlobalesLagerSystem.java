package com.june.agro;

import java.util.*;

/**
 * Implementierung eines globalen Lagerungssystems (mit Generics)
 */
public class GlobalesLagerSystem<T extends LandwirtschaftsProdukt> implements ILagerSystem<T> {
    private final Map<String, T> lagerMap;

    public GlobalesLagerSystem() {
        this.lagerMap = new HashMap<>();
    }

    @Override
    public void lagereProdukte(List<T> produkte) {
        if (produkte == null) return;
        produkte.forEach(produkt -> {
            if (produkt == null || produkt.getName() == null) return;
            if (lagerMap.containsKey(produkt.getName())) {
                // Produkt bereits vorhanden, Menge hinzufügen
                T vorhandenes = lagerMap.get(produkt.getName());
                vorhandenes.setMenge(vorhandenes.getMenge() + produkt.getMenge());
            } else {
                // Produkt neu hinzufügen
                lagerMap.put(produkt.getName(), produkt);
            }
        });
    }

    @Override
    public List<T> getProdukte(String produktName) throws ProduktNichtGefundenException {
        if (!lagerMap.containsKey(produktName)) {
            throw new ProduktNichtGefundenException(produktName);
        }
        return Collections.singletonList(lagerMap.get(produktName));
    }

    @Override
    public String getLagerBestand() {
        StringBuilder sb = new StringBuilder("Aktueller Lagerbestand:\n");
        if (lagerMap.isEmpty()) {
            sb.append("Keine Produkte im Lager.\n");
            return sb.toString();
        }
        for (T produkt : lagerMap.values()) {
            sb.append(produkt).append("\n");
        }
        return sb.toString();
    }
}
