package com.june.production;

import java.util.List;

/**
 * Extrahiert vorhandene Ressourcen (z. B. aus Lagerstätten oder Sensorik).
 */
public interface IResourceExtractor {
    List<Resource> extractResources();
}