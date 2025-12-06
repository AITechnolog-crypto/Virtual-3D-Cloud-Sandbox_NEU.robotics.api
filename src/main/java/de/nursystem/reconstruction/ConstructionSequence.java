package de.nursystem.reconstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Bauablauf-Sequenz
 */
class ConstructionSequence {
    private final List<ConstructionStep> steps = new ArrayList<>();

    public void addStep(String name, int days) {
        steps.add(new ConstructionStep(name, days));
    }

    public List<ConstructionStep> getSteps() { return steps; }
}
