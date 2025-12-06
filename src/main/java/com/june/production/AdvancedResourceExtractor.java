package com.june.production;

import java.util.ArrayList;
import java.util.List;

/**
 * Beispiel-Extraktor: liefert eine Liste Ressourcen.
 */
public class AdvancedResourceExtractor implements IResourceExtractor {
    @Override
    public List<Resource> extractResources() {
        List<Resource> resources = new ArrayList<>();
        resources.add(new Resource("Erz", 1000.0));
        resources.add(new Resource("Wasser", 5000.0));
        return resources;
    }
}