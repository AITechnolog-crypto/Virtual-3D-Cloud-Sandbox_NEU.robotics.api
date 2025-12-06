package com.june.agro;

import java.util.List;

/**
 * Schnittstelle für das Lagerungssystem (mit Generics)
 */
public interface ILagerSystem<T extends LandwirtschaftsProdukt> {
    void lagereProdukte(List<T> produkte);
    List<T> getProdukte(String produktName) throws ProduktNichtGefundenException;
    String getLagerBestand();
}
